package org.f8n.inject;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.f8n.inject.DependencyInfo.Cardinality;
import org.f8n.reflect.TypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;

/**
 * Dependency graph helps to track the relationships between classes and facilitates resolving dependencies correctly.
 *
 * Note that currently this class CANNOT handle cycles in the dependency graph, with the exception of a multibind
 * referencing itself.
 */
@SuppressWarnings("rawtypes")
class DependencyGraph
{
  private static final Logger LOG = LoggerFactory.getLogger(DependencyGraph.class);
  private ListMultimap<TypeInfo, Class> providers = ArrayListMultimap.create();
  private ListMultimap<TypeInfo, Class> dependers = ArrayListMultimap.create();
  private Map<Class, ComponentInfo> resolution = new HashMap<>();

  private class ComponentInfo
  {
    Class clazz;
    Map<DependencyInfo, Boolean> dependencies;
    List<TypeInfo> provides;
    boolean isSatisfied;
    boolean isPending;
    boolean hasMultibind;
    public Throwable failure;

    ComponentInfo(Class clazz, List<DependencyInfo> deps, List<TypeInfo> provides)
    {
      this.clazz = clazz;
      this.dependencies = deps.stream().collect(Collectors.toMap(Function.identity(),
                                                                 dep -> providers.containsKey(dep.getType())));
      this.provides = provides;
      this.isSatisfied = deps.isEmpty() || dependencies.values().stream().allMatch(Boolean::booleanValue);
      this.isPending = true;
      this.hasMultibind = deps.stream().anyMatch(d -> d.getCardinality() == Cardinality.MULTIPLE);
    }

    /**
     * @return true if the only unsatsified dependencies for this component are optional
     */
    boolean onlyOptionalDependenciesRemaining()
    {
      return dependencies.entrySet()
                         .stream()
                         .allMatch(e -> e.getValue() == true || e.getKey().getCardinality() == Cardinality.OPTIONAL);
    }

    /**
     * @return true if all remaining unsatisfied optional dependencies are impossible to satisfy given the registered
     *         classes
     */
    public boolean remainingOptionalAreNotPossible()
    {
      return dependencies.entrySet().stream().filter(e -> !e.getValue()).map(Entry::getKey).allMatch(dep ->
      {
        return dep.getCardinality() == Cardinality.OPTIONAL
               && resolution.values().stream().noneMatch(c -> c.provides.contains(dep.getType()));
      });
    }

    /**
     * @return true if all registered classes that could provide the multibind target(s) for this class are resolved
     */
    public boolean allMultibindAvailable()
    {
      return dependencies.keySet().stream().filter(dep -> dep.getCardinality() == Cardinality.MULTIPLE).allMatch(dep ->
      {
        ImmutableSet<Class> currentProviders = ImmutableSet.copyOf(providers.get(dep.getType()));
        Set<Class> potentialProviders = getAllPotentialProviders(dep.getType());
        SetView<Class> difference = Sets.difference(potentialProviders, currentProviders);
        return difference.isEmpty() || (difference.size() == 1 && difference.contains(this.clazz));
      });
    }
  }

  /**
   * Add a new node to the graph
   *
   * @param clazz class being added
   * @param provides all services provided by the class
   * @param deps all services required by the class, may be empty
   */
  public void addNode(Class clazz, Collection<TypeInfo> provides, Collection<DependencyInfo> deps)
  {
    LOG.info("Adding node {}", clazz.getSimpleName());
    deps.forEach(service -> dependers.put(service.getType(), clazz));
    resolution.put(clazz, new ComponentInfo(clazz, ImmutableList.copyOf(deps), ImmutableList.copyOf(provides)));
  }

  /**
   * Called when a class from the graph is resolved, so that it may be used to satisfy dependencies using the services
   * that it provides.
   *
   * @param clazz resolved class
   */
  public void onResolve(Class<?> clazz, List<String> tags)
  {
    LOG.info("Resolved {}", clazz.getSimpleName());
    ComponentInfo created = resolution.get(clazz);
    created.isPending = false;
    for (TypeInfo service : created.provides)
    {
      addProvider(clazz, service, tags);
    }
  }

  /**
   * Add a provider. This may be an external class not tracked by the graph.
   *
   * @param clazz class providing the service
   * @param service service provided
   */
  public void addProvider(Class<?> clazz, TypeInfo service, List<String> tags)
  {
    providers.put(service, clazz);
    dependers.get(service).forEach(depender ->
    {
      ComponentInfo dependerInfo = resolution.get(depender);
      dependerInfo.dependencies.keySet()
                               .stream()
                               .filter(d -> d.getType().equals(service))
                               .filter(d -> tags.containsAll(d.getTags()))
                               .forEach(d ->
                               {
                                 dependerInfo.dependencies.put(d, Boolean.TRUE);
                               });
      if (dependerInfo.dependencies.values().stream().allMatch(b -> b == Boolean.TRUE))
      {
        dependerInfo.isSatisfied = true;
      }
    });

    if (service.getRawClass() == ComponentFactory.class && service.getTypeArguments().size() == 1)
    {
      addProvider(clazz, service.getTypeArguments().get(0), tags);
    }
  }

  /**
   * @return all classes tracked by the graph that are satisfied, pending, and have no multibind dependencies
   */
  public List<Class> getSatisfiedPendingClassesWithNoMultiBind()
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.isSatisfied && dep.isPending && !dep.hasMultibind)
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toList());
  }

  /**
   * @return all classes that can provide the given service
   */
  private Set<Class> getAllPotentialProviders(TypeInfo service)
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.failure == null)
                     .filter(dep -> dep.provides.contains(service))
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toSet());
  }

  /**
   * @return stream of classes that can be resolved, but have been pending in case further classes might be added to the
   *         graph to change their resolution
   */
  public Stream<Class> remainingResolvable()
  {
    return Streams.concat(optionalWithNoProvider(), multibindWithAllPossibleAvailable());
  }

  /**
   * @return stream of classes with multibind dependencies which have not been resolved yet, due to the fact that there
   *         are still unresolved classes in the graph which provide the target service
   */
  public Stream<Class> remainingMultibind()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> c.failure == null)
                     .filter(c -> c.hasMultibind && c.isPending)
                     .filter(c -> c.isSatisfied || c.onlyOptionalDependenciesRemaining())
                     .map(c -> c.clazz);
  }

  /**
   * @return stream of classes with optional dependencies which are not satisfied, but there is at least one class in
   *         the graph that could provide the target service that has not been resolved
   */
  public Stream<Class> remainingOptional()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> !c.hasMultibind && c.isPending)
                     .filter(c -> c.onlyOptionalDependenciesRemaining())
                     .map(c -> c.clazz);
  }

  /**
   * @return stream of pending classes with multibind dependencies where every possible provider in the graph has
   *         already been resolved
   */
  private Stream<Class> multibindWithAllPossibleAvailable()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> c.hasMultibind && c.isPending)
                     .filter(c -> c.isSatisfied || c.onlyOptionalDependenciesRemaining())
                     .filter(c -> c.allMultibindAvailable() && c.remainingOptionalAreNotPossible())
                     .map(c -> c.clazz);
  }

  /**
   * @return stream of pending classes with unsatisfied optional dependencies where no potential providers are
   *         registered
   */
  private Stream<Class> optionalWithNoProvider()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> !c.hasMultibind && c.isPending)
                     .filter(ComponentInfo::onlyOptionalDependenciesRemaining)
                     .filter(ComponentInfo::remainingOptionalAreNotPossible)
                     .map(c -> c.clazz);
  }

  /**
   * Print a diagnostic report of pending classes
   *
   * @param out print stream to write the report to
   */
  public void printDiagnosticReport(PrintStream out)
  {
    out.println("Unresolved Service Report");
    resolution.values().stream().filter(c -> c.isPending || c.failure != null).forEach(c ->
    {
      if (c.failure != null)
      {
        out.printf("%s: Failed -- %s%n", c.clazz.getSimpleName(), c.failure.getMessage());
        c.failure.printStackTrace(out);
      }
      else if (c.isSatisfied)
      {
        if (c.hasMultibind)
        {
          out.printf("%s: Multibind", c.clazz.getSimpleName());
        }
        else
        {
          out.printf("%s: Pending", c.clazz.getSimpleName());
        }
      }
      else
      {
        if (c.onlyOptionalDependenciesRemaining())
        {
          out.printf("%s: Optional only", c.clazz.getSimpleName());
        }
        else
        {
          out.printf("%s: Unsatisfied [%s]",
                     c.clazz.getSimpleName(),
                     c.dependencies.entrySet()
                                   .stream()
                                   .filter(e -> !e.getValue())
                                   .map(Entry::getKey)
                                   .collect(Collectors.toList()));
        }
      }
      out.println();
    });
  }

  public void onFailure(Class<?> clazz, Throwable t)
  {
    ComponentInfo componentInfo = resolution.get(clazz);
    componentInfo.failure = t;
    componentInfo.isPending = false;
  }
}

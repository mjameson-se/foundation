package org.f8n.injector;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.f8n.cornerstone.reflection.TypeInfo;
import org.f8n.injector.DependencyInfo.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;

@SuppressWarnings("rawtypes")
public class DependencyGraph
{
  private static final Logger LOG = LoggerFactory.getLogger(DependencyGraph.class);
  private ListMultimap<TypeInfo, Class> providers = ArrayListMultimap.create();
  private ListMultimap<TypeInfo, Class> dependers = ArrayListMultimap.create();
  private Map<Class, ComponentInfo> resolution = new HashMap<>();

  private class ComponentInfo implements Comparable<ComponentInfo>
  {
    Class clazz;
    Map<DependencyInfo, Boolean> dependencies;
    List<TypeInfo> provides;
    boolean isSatisfied;
    boolean isPending;
    boolean hasMultibind;

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

    boolean onlyOptionalDependenciesRemaining()
    {
      return dependencies.entrySet()
                         .stream()
                         .allMatch(e -> e.getValue() == true || e.getKey().getCardinality() == Cardinality.OPTIONAL);
    }

    @Override
    public int compareTo(ComponentInfo other)
    {
      return ComparisonChain.start()
                            .compare(dependencies.values().stream().filter(b -> !b).count(),
                                     other.dependencies.values().stream().filter(b -> !b).count())
                            .compareFalseFirst(hasMultibind, other.hasMultibind)
                            .compare(clazz.getSimpleName(), other.clazz.getSimpleName())
                            .result();
    }

    public boolean optionalNotPossible()
    {
      return dependencies.entrySet().stream().filter(e -> !e.getValue()).map(Entry::getKey).allMatch(dep ->
      {
        return dep.getCardinality() == Cardinality.OPTIONAL
               && resolution.values().stream().noneMatch(c -> c.provides.contains(dep.getType()));
      });
    }

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

  public void addNode(Class clazz, Collection<TypeInfo> provides, Collection<DependencyInfo> deps)
  {
    LOG.info("Adding node {}", clazz.getSimpleName());
    deps.forEach(service -> dependers.put(service.getType(), clazz));
    resolution.put(clazz, new ComponentInfo(clazz, ImmutableList.copyOf(deps), ImmutableList.copyOf(provides)));
  }

  public void onCreate(Class<?> clazz)
  {
    LOG.info("Creating {}", clazz.getSimpleName());
    ComponentInfo created = resolution.get(clazz);
    created.isPending = false;
    for (TypeInfo service : created.provides)
    {
      addProvider(clazz, service);
    }
  }

  public void addProvider(Class clazz, TypeInfo service)
  {
    providers.put(service, clazz);
    dependers.get(service).forEach(depender ->
    {
      ComponentInfo dependerInfo = resolution.get(depender);
      Optional<DependencyInfo> dep = dependerInfo.dependencies.keySet()
                                                              .stream()
                                                              .filter(d -> d.getType().equals(service))
                                                              .findAny();
      dep.ifPresent(d -> dependerInfo.dependencies.put(d, Boolean.TRUE));
      if (dependerInfo.dependencies.values().stream().allMatch(b -> b == Boolean.TRUE))
      {
        dependerInfo.isSatisfied = true;
      }
    });
  }

  public List<Class> getSatisfiedPendingClassesWithNoMultiBind()
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.isSatisfied && dep.isPending && !dep.hasMultibind)
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toList());
  }

  public Set<Class> getAllPotentialProviders(TypeInfo service)
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.provides.contains(service))
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toSet());
  }

  public List<Class> getPendingClasses()
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.isPending)
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toList());
  }

  public Stream<Class> remainingResolvable()
  {
    return Streams.concat(optionalWithNoProvider(), multibindWithAllPossibleAvailable());
  }

  public Stream<Class> remainingMultibind()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> c.hasMultibind && c.isPending)
                     .filter(c -> c.isSatisfied || c.onlyOptionalDependenciesRemaining())
                     .map(c -> c.clazz);
  }

  public Stream<Class> remainingOptional()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> !c.hasMultibind && c.isPending)
                     .filter(c -> c.onlyOptionalDependenciesRemaining())
                     .map(c -> c.clazz);
  }

  private Stream<Class> multibindWithAllPossibleAvailable()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> c.hasMultibind && c.isPending)
                     .filter(c -> c.isSatisfied || c.onlyOptionalDependenciesRemaining())
                     .filter(c -> c.allMultibindAvailable() && c.optionalNotPossible())
                     .map(c -> c.clazz);
  }

  private Stream<Class> optionalWithNoProvider()
  {
    return resolution.values()
                     .stream()
                     .filter(c -> !c.hasMultibind && c.isPending)
                     .filter(ComponentInfo::onlyOptionalDependenciesRemaining)
                     .filter(ComponentInfo::optionalNotPossible)
                     .map(c -> c.clazz);
  }

  public void printDiagnosticReport(PrintStream out)
  {
    out.println("Unresolved Service Report");
    resolution.values().stream().filter(c -> c.isPending).forEach(c ->
    {
      if (c.isSatisfied)
      {
        if (c.hasMultibind)
        {
          out.printf("%s: Multibind", c.clazz.getSimpleName());
        }
        else
        {
          out.printf("%s: Deferred", c.clazz.getSimpleName());
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
}

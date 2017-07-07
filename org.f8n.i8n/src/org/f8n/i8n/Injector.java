package org.f8n.i8n;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.f8n.i8n.DependencyInfo.Cardinality;
import org.f8n.i8n.a8n.Activate;
import org.f8n.i8n.a8n.Component;
import org.f8n.i8n.a8n.Condition;
import org.f8n.i8n.a8n.Inject;
import org.f8n.r8n.ArgumentProvider;
import org.f8n.r8n.ClassStream;
import org.f8n.r8n.CombiningArgumentProvider;
import org.f8n.r8n.Invoker;
import org.f8n.r8n.MethodStream;
import org.f8n.r8n.TypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;

/**
 * Dependency Injector. See repository readme for a detailed description of features.
 *
 * Basic usage involves many invocations of {@link #addClass(Class)}, typically followed by a single call to
 * {@link #resolveRemaining()} to resolve any components whose instantiation was deferred due to optional or multibind
 * dependencies that could have changed with further added classes.
 */
@SuppressWarnings("rawtypes")
public class Injector
{
  private static final Logger LOG = LoggerFactory.getLogger(Injector.class);
  private ServiceRegistry registry;
  private Invoker invoker;
  private DependencyGraph graph = new DependencyGraph();
  private Map<TypeInfo, Class> componentProviders = new ConcurrentHashMap<>();

  private class ArgProviderImpl implements ArgumentProvider
  {
    @Override
    public Object get(int position, TypeInfo type, List<Annotation> annotations)
    {
      if (componentProviders.containsKey(type))
      {
        Object obj = invoker.buildNew(selectConstructor(type.getRawClass()));
        afterBuild(obj, type.getRawClass(), false);
        return obj;
      }
      if (type.getRawClass() == Set.class)
        return ImmutableSet.copyOf(registry.getService(type.getTypeArguments().get(0)));
      return registry.getService(type).stream().findFirst().orElse(null);
    }
  }

  /**
   * Create a new Injector
   *
   * @param registry registry for registering singletons and querying for dependencies
   */
  public Injector(ServiceRegistry registry)
  {
    this.registry = registry;
    this.invoker = new Invoker(new ArgProviderImpl());
  }

  /**
   * Create a new Injector
   *
   * @param registry registry for registering singletons and querying for dependencies
   * @param externalProvider external argument provider for providing extra dependencies
   */
  public Injector(ServiceRegistry registry, ArgumentProvider externalProvider)
  {
    this.registry = registry;
    ArgumentProvider provider = new CombiningArgumentProvider(new ArgProviderImpl(), externalProvider);
    this.invoker = new Invoker(provider);
  }

  /**
   * Register an external provider provided either by the {@link ServiceRegistry} or {@link ArgumentProvider} provided
   * to the constructor.
   *
   * @param provider class providing the service
   * @param service service provided
   */
  public void registerExternalProvider(Class<?> provider, Class<?> service)
  {
    graph.addProvider(provider, new TypeInfo(service));
  }

  /**
   * Add a class to the injector to be created when satisfied, if possible.
   *
   * @param clazz class to add
   */
  public void addClass(Class<?> clazz)
  {
    graph.addNode(clazz, getProvides(clazz).collect(Collectors.toSet()), getDeps(clazz));
    resolveSatisfied(false);
  }

  /**
   * @return true if either argument is true
   */
  private Boolean reduceOr(Boolean one, Boolean two)
  {
    return one || two;
  }

  /**
   * Resolve any remaining services which may not be fully satisfied.
   */
  public void resolveRemaining()
  {
    resolveSatisfied(true);
  }

  /**
   * @param force force services with optional or multibind dependencies that are unsatisfied to be resolved
   */
  private void resolveSatisfied(boolean force)
  {
    boolean result = true;
    while (result)
    {
      while (result)
      {
        result = graph.getSatisfiedPendingClassesWithNoMultiBind()
                      .stream()
                      .map(this::whenSatisfied)
                      .reduce(false, this::reduceOr);
      }
      if (force)
      {
        result = graph.remainingResolvable().map(this::whenSatisfied).reduce(false, this::reduceOr);
        if (!result)
        {
          result = graph.remainingMultibind().map(this::whenSatisfied).reduce(false, this::reduceOr);
        }
        if (!result)
        {
          result = graph.remainingOptional().map(this::whenSatisfied).reduce(false, this::reduceOr);
        }
      }
    }
  }

  /**
   * Called when a component class is satisfied to resolve it.
   *
   * @param clazz class that is satisfied
   * @return true if the class was resolved, false if resolution was deferred
   */
  protected boolean whenSatisfied(Class<?> clazz)
  {
    Component component = clazz.getAnnotation(Component.class);

    if (deferResolution(clazz))
    {
      LOG.info("Deferring {}", clazz.getSimpleName());
      return false;
    }
    if (component == null || component.singleton())
    {
      Object newOne = invoker.buildNew(selectConstructor(clazz));
      afterBuild(newOne, clazz, true);
    }
    else
    {
      getProvides(clazz).forEach(provided -> componentProviders.putIfAbsent(provided, clazz));
    }
    graph.onResolve(clazz);
    return true;
  }

  /**
   * Check if a class's resolution should be deferred.
   *
   * @param clazz class to evaluate
   * @return true to defer
   */
  @SuppressWarnings("unchecked")
  protected boolean deferResolution(Class clazz)
  {
    if (!clazz.isAnnotationPresent(Condition.class))
      return false;
    try
    {
      Condition condition = (Condition) clazz.getAnnotation(Condition.class);
      Class target = Optional.ofNullable(condition.target()).filter(c -> c != void.class).orElse(clazz);
      Method method = Arrays.stream(target.getMethods())
                            .filter(m -> m.getName().equals(condition.method()))
                            .findFirst()
                            .get();

      Object ret = method.invoke(null, (Object[]) condition.arguments());
      if (ret instanceof Boolean)
        return (Boolean) ret;
    }
    catch (Exception ex)
    {
      LOG.info("Failed defer check", ex);
    }
    return true;
  }

  /**
   * Called after Injector builds any component to perform method injection and activation.
   *
   * @param object newly constructed object
   * @param clazz class of object
   * @param singleton true if the object is being built as a singleton
   */
  protected void afterBuild(Object object, Class<?> clazz, boolean singleton)
  {
    new MethodStream(clazz).withAnnotation(Inject.class).publicOnly().stream().forEach(m -> invoker.invoke(object, m));
    new MethodStream(clazz).withAnnotation(Activate.class)
                           .publicOnly()
                           .stream()
                           .forEach(m -> invoker.invoke(object, m));
    if (singleton)
    {
      registry.register(object, getProvides(clazz).collect(Collectors.toSet()));
    }
  }

  /**
   * Determine the list of dependencies for the class
   *
   * @param clazz class to evaluate
   * @return list of dependencies for the class
   */
  protected List<DependencyInfo> getDeps(Class<?> clazz)
  {
    return Streams.concat(Arrays.stream(selectConstructor(clazz).getGenericParameterTypes()),
                          findBindMethods(clazz).flatMap(m -> Arrays.stream(m.getGenericParameterTypes())))
                  .map(this::mapToDependency)
                  .collect(Collectors.toList());
  }

  /**
   * Find the bind methods for this class
   *
   * @param clazz class to evaluate
   * @return stream stream of methods used for dependency injection
   */
  protected Stream<Method> findBindMethods(Class<?> clazz)
  {
    return new ClassStream(clazz).mapMethods().withAnnotation(Inject.class).publicOnly().stream();
  }

  /**
   * Map a type to a dependency -- handles determining cardinality and target type
   *
   * @param type type parameter to bind method or constructor
   * @return corresponding {@link DependencyInfo}
   */
  protected DependencyInfo mapToDependency(Type type)
  {
    TypeInfo typeInfo = new TypeInfo(type);
    if (typeInfo.getRawClass() == Optional.class)
      return new DependencyInfo(typeInfo.getTypeArguments().get(0), Cardinality.OPTIONAL);
    if (typeInfo.getRawClass() == Set.class)
      return new DependencyInfo(typeInfo.getTypeArguments().get(0), Cardinality.MULTIPLE);
    return new DependencyInfo(typeInfo, Cardinality.SINGLE);
  }

  /**
   * Select an appropriate constructor for the class
   *
   * @param clazz class to evaluate
   * @return chosen constructor
   */
  protected Constructor<?> selectConstructor(Class<?> clazz)
  {
    for (Constructor<?> ctor : clazz.getConstructors())
    {
      if (ctor.isAnnotationPresent(Inject.class))
        return ctor;
    }
    for (Constructor<?> ctor : clazz.getConstructors())
    {
      if (ctor.getParameterCount() == 0)
        return ctor;
    }
    return clazz.getConstructors()[0];
  }

  /**
   * Determine the list of services provided by the class.
   *
   * @param clazz class to evaluate
   * @return stream of services provided
   */
  protected Stream<TypeInfo> getProvides(Class<?> clazz)
  {
    Stream<TypeInfo> assignableTypes = new TypeInfo(clazz).getAssignableTypes();
    if (clazz.isAnnotationPresent(Component.class))
    {
      Component component = clazz.getAnnotation(Component.class);
      Set<Class> services = ImmutableSet.copyOf(component.service());
      if (!services.isEmpty())
      {
        assignableTypes = assignableTypes.filter(t -> services.contains(t.getRawClass()));
      }
    }
    return assignableTypes;
  }

  /**
   * Print a report of unresolved components
   *
   * @param out stream to print to
   */
  public void reportUnresolved(PrintStream out)
  {
    graph.printDiagnosticReport(out);
  }
}

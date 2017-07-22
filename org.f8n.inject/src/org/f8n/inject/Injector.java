package org.f8n.inject;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.f8n.inject.DependencyInfo.Cardinality;
import org.f8n.inject.annotate.Activate;
import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Condition;
import org.f8n.inject.annotate.Inject;
import org.f8n.inject.annotate.Target;
import org.f8n.reflect.ArgumentProvider;
import org.f8n.reflect.ClassStream;
import org.f8n.reflect.CombiningArgumentProvider;
import org.f8n.reflect.Invoker;
import org.f8n.reflect.MethodStream;
import org.f8n.reflect.TypeInfo;
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

  private class ArgProviderImpl implements ArgumentProvider
  {
    @Override
    public Object get(int position, TypeInfo type, List<Annotation> annotations)
    {
      List<String> targetTags = annotations.stream()
                                           .filter(a -> a instanceof Target)
                                           .map(a -> Arrays.asList(((Target) a).value()))
                                           .findFirst()
                                           .orElse(Collections.emptyList());

      TypeInfo serviceType = type;
      if (type.getRawClass() == Set.class && !type.getTypeArguments().isEmpty())
      {
        serviceType = type.getTypeArguments().get(0);
      }

      Stream<Object> services = Stream.concat(registry.getService(serviceType)
                                                      .stream()
                                                      .filter(provider -> hasTags(provider, targetTags)),
                                              registry.getService(new TypeInfo(ComponentFactory.class, serviceType))
                                                      .stream()
                                                      .filter(factory -> hasTags(factory, targetTags))
                                                      .map(cf -> ((ComponentFactory<?>) cf).buildComponent(annotations)));
      if (type.getRawClass() == Set.class)
        return services.collect(Collectors.toSet());
      return services.findFirst().orElse(null);
    }
  }

  private class InjectorComponentFactory implements ComponentFactory<Object>
  {
    private Class<?> componentClass;
    private Supplier<Object> factory;
    private List<String> tags;

    private InjectorComponentFactory(Class<?> componentClass, Supplier<Object> factory, List<String> tags)
    {
      this.componentClass = componentClass;
      this.factory = factory;
      this.tags = tags;
    }

    @Override
    public Object buildComponent(List<Annotation> annotations)
    {
      Object obj = factory.get();
      afterBuild(obj, componentClass, false);
      return obj;
    }
  }

  static List<String> getTags(Object obj)
  {
    if (obj instanceof InjectorComponentFactory)
      return ((InjectorComponentFactory) obj).tags;
    if (obj instanceof AnnotatedElement)
    {
      if (((AnnotatedElement) obj).isAnnotationPresent(Target.class))
        return Arrays.asList(((AnnotatedElement) obj).getAnnotation(Target.class).value());
      else
        return Collections.emptyList();
    }
    return getTags(obj.getClass());
  }

  static boolean hasTags(Object provider, List<String> tags)
  {
    if (tags.isEmpty())
      return true;
    List<String> providedTags = getTags(provider);
    LOG.info("{}: {} vs {}", provider.getClass().getSimpleName(), providedTags, tags);
    return providedTags.containsAll(tags);
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
    graph.addProvider(provider, new TypeInfo(service), Collections.emptyList());
  }

  /**
   * Add a class to the injector to be created when satisfied, if possible. When added in this way,
   * {@link Component#priority()} may not be respected.
   *
   * @param clazz class to add
   */
  public void addClass(Class<?> clazz)
  {
    graph.addNode(clazz, getProvides(clazz).collect(Collectors.toSet()), getDeps(clazz));
    resolveSatisfied(false);
  }

  /**
   * Add several classes to the injector to be created when satisfied, if possible. When added in this way,
   * {@link Component#priority()} is respected.
   *
   * @param clazz class to add
   */
  public void addClasses(Stream<Class<?>> classes)
  {
    classes.forEach(clazz -> graph.addNode(clazz, getProvides(clazz).collect(Collectors.toSet()), getDeps(clazz)));
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
        result = processSatisfied(graph.getSatisfiedPendingClassesWithNoMultiBind().stream(), Integer.MAX_VALUE);
      }
      if (force)
      {
        result = processSatisfied(graph.remainingResolvable(), Integer.MAX_VALUE);
        // For the last two cases, resolve at most one at a time
        if (!result)
        {
          result = processSatisfied(graph.remainingMultibind(), 1);
        }
        if (!result)
        {
          result = processSatisfied(graph.remainingOptional(), 1);
        }
      }
    }
  }

  /**
   * @param classesToResolve stream of classes to resolve
   * @param limit upper bound on number of classes to resolve, discounting deferred classes
   * @return true if any classes were successfully resolved
   */
  private boolean processSatisfied(Stream<Class> classesToResolve, int limit)
  {
    return classesToResolve.sorted(this::sortByPriority)
                           .map(this::whenSatisfied)
                           .filter(b -> b) // Filter deferred
                           .limit(limit) // Apply limit
                           .reduce(false, this::reduceOr);
  }

  /**
   * Comparator function to sort by {@link Component#priority())
   */
  private int sortByPriority(Class<?> one, Class<?> two)
  {
    int p1 = Optional.ofNullable(one.getAnnotation(Component.class)).map(Component::priority).orElse(1000);
    int p2 = Optional.ofNullable(two.getAnnotation(Component.class)).map(Component::priority).orElse(1000);
    return Integer.compare(p1, p2);
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
    List<String> tags = getTags(clazz);
    try
    {
      if (component == null || component.singleton())
      {
        Object newOne = invoker.buildNew(selectConstructor(clazz));
        afterBuild(newOne, clazz, true);
      }
      else
      {
        InjectorComponentFactory cf = new InjectorComponentFactory(clazz,
                                                                   () -> invoker.buildNew(selectConstructor(clazz)),
                                                                   tags);
        Set<TypeInfo> cfServices = getProvides(clazz).map(service -> new TypeInfo(ComponentFactory.class, service))
                                                     .collect(Collectors.toSet());
        LOG.info("Resolving factory for {}", clazz.getSimpleName());
        cfServices.forEach(cfService -> graph.addProvider(InjectorComponentFactory.class, cfService, tags));
        registry.register(cf, cfServices);
      }
      graph.onResolve(clazz, tags);
      return true;
    }
    catch (Throwable t)
    {
      LOG.warn("Component {} failed to resolve", clazz.getSimpleName(), t);
      graph.onFailure(clazz, t);
      return true;
    }
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
        return ((Boolean) ret).booleanValue() == condition.expectForDefer();
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
    MethodStream.allMethods(clazz).nonPublic().withAnnotation(Inject.class).stream().forEach(m ->
    {
      LOG.warn("Class {} has non-public Inject method {}", clazz.getSimpleName(), m.getName());
    });
    MethodStream.allMethods(clazz).nonPublic().withAnnotation(Activate.class).stream().forEach(m ->
    {
      LOG.warn("Class {} has non-public Activate method {}", clazz.getSimpleName(), m.getName());
    });
    new MethodStream(clazz).withAnnotation(Inject.class).stream().forEach(m -> invoker.invoke(object, m));
    new MethodStream(clazz).withAnnotation(Activate.class).stream().forEach(m -> invoker.invoke(object, m));
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
    Constructor<?> ctor = selectConstructor(clazz);
    return Streams.concat(mapToDependencies(ctor.getGenericParameterTypes(), ctor.getParameterAnnotations()),
                          findBindMethods(clazz).flatMap(m -> mapToDependencies(m.getGenericParameterTypes(),
                                                                                m.getParameterAnnotations())))
                  .collect(Collectors.toList());
  }

  protected Stream<DependencyInfo> mapToDependencies(Type[] types, Annotation[][] annotations)
  {
    return Streams.zip(Arrays.stream(types), Arrays.stream(annotations), this::mapToDependency);
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
  protected DependencyInfo mapToDependency(Type type, Annotation[] annotations)
  {
    TypeInfo typeInfo = new TypeInfo(type);
    List<String> tags = Collections.emptyList();
    for (Annotation annotation : annotations)
    {
      if (annotation instanceof Target)
      {
        tags = Arrays.asList(((Target) annotation).value());
      }
    }
    if (typeInfo.getRawClass() == Optional.class)
      return new DependencyInfo(typeInfo.getTypeArguments().get(0), Cardinality.OPTIONAL, tags);
    if (typeInfo.getRawClass() == Set.class)
      return new DependencyInfo(typeInfo.getTypeArguments().get(0), Cardinality.MULTIPLE, tags);
    return new DependencyInfo(typeInfo, Cardinality.SINGLE, tags);
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

package org.f8n.injector;

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

import org.f8n.cornerstone.reflection.ArgumentProvider;
import org.f8n.cornerstone.reflection.ClassStream;
import org.f8n.cornerstone.reflection.Invoker;
import org.f8n.cornerstone.reflection.MethodStream;
import org.f8n.cornerstone.reflection.TypeInfo;
import org.f8n.injector.DependencyInfo.Cardinality;
import org.f8n.injector.annotations.Activate;
import org.f8n.injector.annotations.Component;
import org.f8n.injector.annotations.Condition;
import org.f8n.injector.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;

@SuppressWarnings("rawtypes")
public class Injector
{
  private static final Logger LOG = LoggerFactory.getLogger(Injector.class);
  private ServiceRegistry registry;
  private Invoker invoker = new Invoker(new ArgProviderImpl());
  private DependencyGraph graph = new DependencyGraph();
  private Map<TypeInfo, Class> componentProviders = new ConcurrentHashMap<>();

  private class ArgProviderImpl implements ArgumentProvider
  {
    @Override
    public Object get(int position, TypeInfo type, List<Annotation> annotations)
    {
      if (componentProviders.containsKey(type))
        return invoker.buildNew(selectConstructor(type.getRawClass()));
      if (type.getRawClass() == Set.class)
        return ImmutableSet.copyOf(registry.getService(type.getTypeArguments().get(0)));
      return registry.getService(type).stream().findFirst().orElse(null);
    }
  }

  public Injector(ServiceRegistry registry)
  {
    this.registry = registry;
  }

  public void addClass(Class<?> clazz)
  {
    graph.addNode(clazz, getProvides(clazz).collect(Collectors.toSet()), getDeps(clazz));
    resolveSatisfied(false);
  }

  private Boolean reduceOr(Boolean one, Boolean two)
  {
    if (one || two)
      return Boolean.TRUE;
    return Boolean.FALSE;
  }

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

  protected boolean whenSatisfied(Class<?> clazz)
  {
    Component component = clazz.getAnnotation(Component.class);

    if (clazz.isAnnotationPresent(Condition.class) && deferConstruction(clazz, component))
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
    graph.onCreate(clazz);
    return true;
  }

  @SuppressWarnings("unchecked")
  protected boolean deferConstruction(Class clazz, Component component)
  {
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

  protected void afterBuild(Object object, Class<?> clazz, boolean singleton)
  {
    new MethodStream(clazz).withAnnotation(Inject.class).publicOnly().stream().forEach(m -> invoker.invoke(object, m));
    new MethodStream(clazz).withAnnotation(Activate.class)
                           .publicOnly()
                           .stream()
                           .forEach(m -> invoker.invoke(object, m));
    if (singleton)
    {
      getProvides(clazz).forEach(service -> registry.register(object, service));
    }
  }

  protected List<DependencyInfo> getDeps(Class<?> clazz)
  {
    return Streams.concat(Arrays.stream(selectConstructor(clazz).getGenericParameterTypes()),
                          findBindMethods(clazz).flatMap(m -> Arrays.stream(m.getGenericParameterTypes())))
                  .map(this::mapToDependency)
                  .collect(Collectors.toList());
  }

  protected Stream<Method> findBindMethods(Class<?> clazz)
  {
    return new ClassStream(clazz).mapMethods().withAnnotation(Inject.class).publicOnly().stream();
  }

  protected DependencyInfo mapToDependency(Type type)
  {
    TypeInfo typeInfo = new TypeInfo(type);
    if (typeInfo.getRawClass() == Optional.class)
      return new DependencyInfo(typeInfo.getTypeArguments().get(0), Cardinality.OPTIONAL);
    if (typeInfo.getRawClass() == Set.class)
      return new DependencyInfo(typeInfo.getTypeArguments().get(0), Cardinality.MULTIPLE);
    return new DependencyInfo(typeInfo, Cardinality.SINGLE);
  }

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

  public void reportUnresolved(PrintStream out)
  {
    graph.printDiagnosticReport(out);
  }
}

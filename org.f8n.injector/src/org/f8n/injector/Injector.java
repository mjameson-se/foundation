package org.f8n.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.f8n.cornerstone.reflection.ArgumentProvider;
import org.f8n.cornerstone.reflection.Invoker;
import org.f8n.cornerstone.reflection.TypeInfo;

public class Injector
{
  private ServiceRegistry registry;
  private Invoker invoker = new Invoker(new ArgProviderImpl());
  private DependencyGraph graph = new DependencyGraph();

  private class ArgProviderImpl implements ArgumentProvider
  {
    @Override
    public Object get(int position, TypeInfo type, List<Annotation> annotations)
    {
      return registry.getService(type);
    }
  }

  public Injector(ServiceRegistry registry)
  {
    this.registry = registry;
  }

  @SuppressWarnings("rawtypes")
  public void addClass(Class<?> clazz)
  {
    graph.addNode(clazz, getProvides(clazz).collect(Collectors.toSet()), getDeps(clazz));
    List<Class> satisfiedPendingClasses = graph.getSatisfiedPendingClasses();
    while (!satisfiedPendingClasses.isEmpty())
    {
      satisfiedPendingClasses.forEach(this::whenSatisfied);
      satisfiedPendingClasses = graph.getSatisfiedPendingClasses();
    }
  }

  protected void whenSatisfied(Class<?> clazz)
  {
    Object newOne = invoker.buildNew(selectConstructor(clazz));
    afterBuild(newOne, clazz);
    getProvides(clazz).forEach(service -> registry.register(newOne, service));
    graph.onCreate(clazz);
  }

  protected void afterBuild(Object object, Class<?> clazz)
  {
  }

  protected List<TypeInfo> getDeps(Class<?> clazz)
  {
    return Arrays.stream(selectConstructor(clazz).getParameterTypes()).map(TypeInfo::new).collect(Collectors.toList());
  }

  protected Constructor<?> selectConstructor(Class<?> clazz)
  {
    return Arrays.stream(clazz.getConstructors())
                 .sorted((c1, c2) -> Integer.compare(c1.getParameterCount(), c2.getParameterCount()))
                 .findFirst()
                 .get();
  }

  protected Stream<TypeInfo> getProvides(Class<?> clazz)
  {
    return new TypeInfo(clazz).getAssignableTypes();
  }

  public Object getUnresolved()
  {
    return null;
  }
}

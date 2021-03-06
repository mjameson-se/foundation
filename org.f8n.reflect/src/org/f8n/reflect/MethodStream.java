package org.f8n.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;

public class MethodStream
{
  private static LoadingCache<Class<?>, Object> instanceCache =
                                                              CacheBuilder.newBuilder()
                                                                          .build(CacheLoader.from(MethodStream::newInstance));
  private Stream<Method> methods;

  public MethodStream(Stream<Method> methods)
  {
    this.methods = methods;
  }

  public MethodStream(Class<?> clazz)
  {
    this(Arrays.stream(clazz.getMethods()));
  }

  public static MethodStream allMethods(Class<?> clazz)
  {
    Set<Class<?>> classes = new HashSet<>();
    while (clazz != null)
    {
      classes.add(clazz);
      clazz = clazz.getSuperclass();
    }
    return new ClassStream(classes.stream()).mapDeclaredMethods();
  }

  private static Object newInstance(Class<?> c)
  {
    try
    {
      return c.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e)
    {
      throw new RuntimeException(e);
    }
  }

  public MethodStream withAnnotation(Class<? extends Annotation> a)
  {
    return withFilter(m -> m.isAnnotationPresent(a));
  }

  public MethodStream withParameterTypes(Class<?>... classes)
  {
    return withFilter(m -> Arrays.deepEquals(m.getParameterTypes(), classes));
  }

  public MethodStream publicOnly()
  {
    return withFilter(m -> Modifier.isPublic(m.getModifiers()));
  }

  public MethodStream nonPublic()
  {
    return withFilter(m -> !Modifier.isPublic(m.getModifiers()));
  }

  public MethodStream withReturnType(Class<?> clazz)
  {
    return withFilter(m -> clazz == m.getReturnType());
  }

  public MethodStream withFilter(Predicate<Method> filter)
  {
    return new MethodStream(methods.filter(filter));
  }

  public MethodStream sorted()
  {
    return new MethodStream(methods.sorted((m1, m2) -> ComparisonChain.start()
                                                                      .compare(m1.getDeclaringClass().getName(),
                                                                               m2.getDeclaringClass().getName())
                                                                      .compare(m1.getName(), m2.getName())
                                                                      .result()));
  }

  public <X, Y> Stream<InterfaceWrapper<Y>> asInterface(Function<BoundMethod<X>, Y> transform)
  {
    Function<Method, BoundMethod<X>> i = (m) ->
    {
      try
      {
        return BoundMethod.<X> of(m, instanceCache.get(m.getDeclaringClass()));
      }
      catch (ExecutionException e)
      {
        throw new RuntimeException(e);
      }
    };
    return methods.map(i).map((b) -> new InterfaceWrapper<>(b.getInstance(), transform.apply(b), b.getMethod()));
  }

  public <X> Stream<BoundMethod<X>> asBoundMethod()
  {
    Function<Method, BoundMethod<X>> i = (m) ->
    {
      try
      {
        return BoundMethod.<X> of(m, instanceCache.get(m.getDeclaringClass()));
      }
      catch (ExecutionException e)
      {
        throw new RuntimeException(e);
      }
    };
    return methods.map(i);
  }

  public Stream<Method> stream()
  {
    return methods;
  }
}

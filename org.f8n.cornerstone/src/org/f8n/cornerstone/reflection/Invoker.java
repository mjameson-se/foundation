package org.f8n.cornerstone.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.ComparisonChain;

public class Invoker
{
  private ArgumentProvider provider;

  public Invoker(ArgumentProvider provider)
  {
    this.provider = new OptionalArgumentProvider(provider);
  }

  public <T> T buildNew(Class<T> clazz)
  {
    Constructor<?> ctor = Arrays.stream(clazz.getDeclaredConstructors()).sorted((one, two) ->
    {
      return ComparisonChain.start()
                            .compare(one.getParameterCount(), two.getParameterCount())
                            .compareTrueFirst(Modifier.isPublic(one.getModifiers()),
                                              Modifier.isPublic(two.getModifiers()))
                            .compare(one.toGenericString(), two.toGenericString())
                            .result();
    }).findFirst().get();
    return buildNew(ctor);
  }

  @SuppressWarnings("unchecked")
  public <T> T buildNew(Constructor<?> ctor)
  {
    return (T) invokeInternal(ctor::newInstance, ctor);
  }

  @SuppressWarnings("unchecked")
  public <T> T invoke(Object host, Method method)
  {
    return (T) invokeInternal(args -> method.invoke(host, args), method);
  }

  private interface Invokable
  {
    Object invoke(Object[] args) throws Exception;
  }

  private Object invokeInternal(Invokable method, Executable methodRef)
  {
    List<Object> args = new ArrayList<>(methodRef.getParameterCount());
    Type[] parameterTypes = methodRef.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = methodRef.getParameterAnnotations();
    for (int i = 0; i < methodRef.getParameterCount(); ++i)
    {
      args.add(provider.get(i, new TypeInfo(parameterTypes[i]), Arrays.asList(parameterAnnotations[i])));
    }
    try
    {
      return method.invoke(args.toArray());
    }
    catch (Exception ex)
    {
      Throwables.throwIfUnchecked(ex);
      throw new RuntimeException(ex);
    }
  }
}

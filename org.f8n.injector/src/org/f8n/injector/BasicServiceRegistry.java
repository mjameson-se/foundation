package org.f8n.injector;

import java.util.List;

import org.f8n.cornerstone.reflection.TypeInfo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

public class BasicServiceRegistry implements ServiceRegistry
{
  private ListMultimap<TypeInfo, Object> registry = ArrayListMultimap.create();

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> getService(TypeInfo clazz)
  {
    return (List<T>) ImmutableList.copyOf(registry.get(clazz));
  }

  @Override
  public void register(Object object, TypeInfo serviceInterface)
  {
    registry.put(serviceInterface, object);
  }
}

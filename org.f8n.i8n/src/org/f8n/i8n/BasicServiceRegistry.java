package org.f8n.i8n;

import java.util.List;

import org.f8n.r8n.TypeInfo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

/**
 * Basic implementation of {@link ServiceRegistry} backed by a simple map.
 */
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

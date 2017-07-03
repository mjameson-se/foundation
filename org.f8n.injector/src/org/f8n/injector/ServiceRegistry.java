package org.f8n.injector;

import java.util.List;

import org.f8n.cornerstone.reflection.TypeInfo;

public interface ServiceRegistry
{
  <T> List<T> getService(TypeInfo clazz);

  void register(Object object, TypeInfo serviceInterface);
}

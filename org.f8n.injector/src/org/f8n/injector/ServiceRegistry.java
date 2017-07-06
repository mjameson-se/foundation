package org.f8n.injector;

import java.util.List;

import org.f8n.cornerstone.reflection.TypeInfo;
import org.f8n.injector.annotations.Component;

/**
 * Service registry used to track registered {@link Component} singletons
 */
public interface ServiceRegistry
{
  <T> List<T> getService(TypeInfo clazz);

  void register(Object object, TypeInfo serviceInterface);
}

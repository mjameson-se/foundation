package org.f8n.i8n;

import java.util.List;

import org.f8n.i8n.a8n.Component;
import org.f8n.r8n.TypeInfo;

/**
 * Service registry used to track registered {@link Component} singletons
 */
public interface ServiceRegistry
{
  <T> List<T> getService(TypeInfo clazz);

  void register(Object object, TypeInfo serviceInterface);
}

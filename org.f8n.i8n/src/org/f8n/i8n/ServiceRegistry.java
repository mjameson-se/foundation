package org.f8n.i8n;

import java.util.List;
import java.util.Set;

import org.f8n.i8n.a8n.Component;
import org.f8n.r8n.TypeInfo;

/**
 * Service registry used to track registered {@link Component} singletons
 */
public interface ServiceRegistry
{
  /**
   * Get all services implementing the given type
   * 
   * @param type type of the service to look up
   * @return all services implementing the type
   */
  <T> List<T> getService(TypeInfo type);

  /**
   * Register an object as providing a set of services
   * 
   * @param object object providing services
   * @param services services provided by object
   */
  void register(Object object, Set<TypeInfo> services);
}

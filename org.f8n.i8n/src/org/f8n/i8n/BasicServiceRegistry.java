package org.f8n.i8n;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.f8n.i8n.a8n.Deactivate;
import org.f8n.r8n.Invoker;
import org.f8n.r8n.MethodStream;
import org.f8n.r8n.TypeInfo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

/**
 * Basic implementation of {@link ServiceRegistry} backed by a simple map.
 */
public class BasicServiceRegistry implements ServiceRegistry
{
  private SetMultimap<TypeInfo, Object> registry = HashMultimap.create();
  private List<Object> registrationOrderdList = new LinkedList<>();

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T> List<T> getService(TypeInfo clazz)
  {
    return (List<T>) ImmutableList.copyOf(registry.get(clazz));
  }

  @Override
  public synchronized void register(Object object, Set<TypeInfo> serviceInterfaces)
  {
    serviceInterfaces.forEach(iface -> registry.put(iface, object));
    registrationOrderdList.add(object);
  }

  /**
   * @return ordered list of registered services
   */
  public List<Object> getRegisteredServices()
  {
    return Collections.unmodifiableList(registrationOrderdList);
  }

  /**
   * Attempt to shut down the application cleaning by calling {@link Deactivate} methods on all registered services in
   * the reverse order from which they were registered.
   */
  public void cleanShutdown()
  {
    Lists.reverse(registrationOrderdList).forEach(this::deactivate);
  }

  private void deactivate(Object obj)
  {
    new MethodStream(obj.getClass()).withAnnotation(Deactivate.class)
                                    .stream()
                                    .forEach(m -> Invoker.invokeNoArgs(obj, m));
  }
}

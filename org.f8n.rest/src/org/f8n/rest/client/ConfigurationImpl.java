package org.f8n.rest.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ConfigurationImpl implements Configuration
{
  private Map<Class<?>, Map<Class<?>, Integer>> contracts;
  private Set<Object> instances;
  private Map<String, Object> properties;

  public ConfigurationImpl(Map<Class<?>, Map<Class<?>, Integer>> clazzes,
                           Set<Object> instances,
                           Map<String, Object> properties)
  {
    this.contracts = clazzes;
    this.instances = instances;
    this.properties = properties;
  }

  public ConfigurationImpl()
  {
    this(new HashMap<>(), new HashSet<>(), new HashMap<>());
  }

  @Override
  public Set<Class<?>> getClasses()
  {
    return Collections.unmodifiableSet(contracts.keySet());
  }

  @Override
  public Map<Class<?>, Integer> getContracts(Class<?> arg0)
  {
    return Collections.unmodifiableMap(contracts.get(arg0));
  }

  @Override
  public Set<Object> getInstances()
  {
    return Collections.unmodifiableSet(instances);
  }

  @Override
  public Map<String, Object> getProperties()
  {
    return Collections.unmodifiableMap(properties);
  }

  @Override
  public Object getProperty(String key)
  {
    return properties.get(key);
  }

  @Override
  public Collection<String> getPropertyNames()
  {
    return Collections.unmodifiableCollection(properties.keySet());
  }

  @Override
  public RuntimeType getRuntimeType()
  {
    return RuntimeType.CLIENT;
  }

  @Override
  public boolean isEnabled(Feature arg0)
  {
    return false;
  }

  @Override
  public boolean isEnabled(Class<? extends Feature> arg0)
  {
    return false;
  }

  @Override
  public boolean isRegistered(Object instance)
  {
    return instances.contains(instance);
  }

  @Override
  public boolean isRegistered(Class<?> clazz)
  {
    return contracts.containsKey(clazz);
  }

  public void property(String arg0, Object arg1)
  {
    properties.put(arg0, arg1);
  }

  public void register(Class<?> arg0)
  {
    int prio = getPriority(arg0);
    register(arg0, prio);
  }

  public void register(Object arg0)
  {
    instances.add(arg0);
  }

  private int getPriority(Class<?> clazz)
  {
    Priority prio = clazz.getAnnotation(Priority.class);
    return prio == null ? Priorities.USER : prio.value();
  }

  private Set<Class<?>> discoverContracts(Class<?> clazz)
  {
    // Check for if each of the desired types is assignable from clazz
    return Collections.emptySet();
  }

  public void register(Class<?> clazz, int prio)
  {
    contracts.put(clazz, Maps.toMap(discoverContracts(clazz), k -> prio));
  }

  public void register(Class<?> arg0, Class<?>... arg1)
  {
    ImmutableMap.Builder<Class<?>, Integer> builder = ImmutableMap.builder();
    Arrays.stream(arg1).forEach(c -> builder.put(c, Integer.MAX_VALUE));
    register(arg0, builder.build());
  }

  public void register(Class<?> arg0, Map<Class<?>, Integer> arg1)
  {
    contracts.put(arg0, arg1);
  }

  public void register(Object arg0, int arg1)
  {
    // TODO Auto-generated method stub
  }

  public void register(Object arg0, Class<?>... arg1)
  {
    // TODO Auto-generated method stub
  }

  public void register(Object arg0, Map<Class<?>, Integer> arg1)
  {
    // TODO Auto-generated method stub
  }
}

package org.f8n.rest.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class ConfigurationImpl implements Configuration
{
  private static final Set<Class<?>> CLIENT_CONTRACTS;
  private static final Set<Class<?>> SERVER_CONTRACTS;
  static
  {
    CLIENT_CONTRACTS = ImmutableSet.of(ClientRequestFilter.class,
                                       ClientResponseFilter.class,
                                       Feature.class,
                                       MessageBodyReader.class,
                                       MessageBodyWriter.class);

    SERVER_CONTRACTS = ImmutableSet.of(ContainerRequestFilter.class,
                                       ContainerResponseFilter.class,
                                       Feature.class,
                                       MessageBodyReader.class,
                                       MessageBodyWriter.class);
  }
  private Map<Class<?>, Map<Class<?>, Integer>> contracts;
  private Set<Object> instances;
  private Map<String, Object> properties;
  private RuntimeType type;

  public ConfigurationImpl(RuntimeType type)
  {
    this.type = type;
  }

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
  public Map<Class<?>, Integer> getContracts(Class<?> componentClass)
  {
    return contracts.containsKey(componentClass) ? Collections.unmodifiableMap(contracts.get(componentClass))
                                                 : Collections.emptyMap();
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
    return type;
  }

  @Override
  public boolean isEnabled(Feature feature)
  {
    return false;
  }

  @Override
  public boolean isEnabled(Class<? extends Feature> feature)
  {
    return false;
  }

  @Override
  public boolean isRegistered(Object instance)
  {
    return instances.contains(instance);
  }

  @Override
  public boolean isRegistered(Class<?> componentClass)
  {
    return contracts.containsKey(componentClass);
  }

  public void property(String key, Object value)
  {
    properties.put(key, value);
  }

  public void register(Class<?> componentClass)
  {
    int prio = getPriority(componentClass);
    register(componentClass, prio);
  }

  public void register(Object componentInstance)
  {
    instances.add(componentInstance);
  }

  private int getPriority(Class<?> componentClass)
  {
    Priority prio = componentClass.getAnnotation(Priority.class);
    return prio == null ? Priorities.USER : prio.value();
  }

  private Set<Class<?>> getPossibleContractsForType()
  {
    return type == RuntimeType.SERVER ? SERVER_CONTRACTS : CLIENT_CONTRACTS;
  }

  private Set<Class<?>> discoverContracts(Class<?> componentClass)
  {
    return getPossibleContractsForType().stream()
                                        .filter(c -> c.isAssignableFrom(componentClass))
                                        .collect(Collectors.toSet());
  }

  public void register(Class<?> componentClass, int prio)
  {
    contracts.put(componentClass, Maps.toMap(discoverContracts(componentClass), k -> prio));
  }

  public void register(Class<?> componentClass, Class<?>... contractInterfaces)
  {
    int prio = getPriority(componentClass);
    Map<Class<?>, Integer> contracts = Arrays.stream(contractInterfaces)
                                             .collect(Collectors.toMap(Function.identity(), c -> prio));
    register(componentClass, contracts);
  }

  public void register(Class<?> componentClass, Map<Class<?>, Integer> contracts)
  {
    Set<Class<?>> possibleContractsForType = getPossibleContractsForType();
    this.contracts.put(componentClass,
                       Maps.filterKeys(contracts,
                                       k -> k.isAssignableFrom(componentClass)
                                            && possibleContractsForType.contains(k)));
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

  public ConfigurationImpl copy()
  {
    return new ConfigurationImpl(new HashMap<>(contracts), new HashSet<>(instances), new HashMap<>(properties));
  }
}

package org.f8n.injector;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.f8n.cornerstone.reflection.TypeInfo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

@SuppressWarnings("rawtypes")
public class DependencyGraph
{
  private ListMultimap<TypeInfo, Class> providers = ArrayListMultimap.create();
  private ListMultimap<TypeInfo, Class> dependers = ArrayListMultimap.create();
  private Map<Class, DependencyInfo> resolution = new HashMap<>();

  private static class DependencyInfo
  {
    Class clazz;
    Map<TypeInfo, Boolean> dependencies;
    List<TypeInfo> provides;
    boolean isSatisfied;
    boolean isPending;

    DependencyInfo(Class clazz, List<TypeInfo> deps, List<TypeInfo> provides)
    {
      this.clazz = clazz;
      this.dependencies = deps.stream().collect(Collectors.toMap(Function.identity(), v -> Boolean.FALSE));
      this.provides = provides;
      this.isSatisfied = false;
      this.isPending = true;
    }
  }

  public void addNode(Class<?> clazz, Collection<TypeInfo> provides, Collection<TypeInfo> deps)
  {
    deps.forEach(service -> dependers.put(service, clazz));
    resolution.put(clazz, new DependencyInfo(clazz, ImmutableList.copyOf(deps), ImmutableList.copyOf(provides)));
  }

  public void onCreate(Class<?> clazz)
  {
    DependencyInfo created = resolution.get(clazz);
    created.isPending = false;
    for (TypeInfo service : created.provides)
    {
      addProvider(clazz, service);
    }
  }

  public void addProvider(Class clazz, TypeInfo service)
  {
    providers.put(service, clazz);
    dependers.get(service).forEach(depender ->
    {
      DependencyInfo dependerInfo = resolution.get(depender);
      dependerInfo.dependencies.put(service, Boolean.TRUE);
      if (dependerInfo.dependencies.values().stream().allMatch(b -> b == Boolean.TRUE))
      {
        dependerInfo.isSatisfied = true;
      }
    });
  }

  public List<Class> getSatisfiedPendingClasses()
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.isSatisfied && dep.isPending)
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toList());
  }

  public List<Class> getAllPotentialProviders(Class service)
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.provides.contains(service))
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toList());
  }

  public List<Class> getPendingClasses()
  {
    return resolution.values()
                     .stream()
                     .filter(dep -> dep.isPending)
                     .map(dep -> dep.clazz)
                     .collect(Collectors.toList());
  }
}

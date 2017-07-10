package org.f8n.r8n;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class ClasspathSearch
{
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathSearch.class);
  private Set<ClassInfo> ci = new HashSet<>();
  private Stream<ClassInfo> stream = ci.stream();
  private final ClassLoader cl;

  public ClasspathSearch()
  {
    this(ClasspathSearch.class.getClassLoader());
  }

  public ClasspathSearch(ClassLoader classLoader)
  {
    this.cl = classLoader;
  }

  public ClasspathSearch includePackage(String packageName) throws IOException
  {
    ci.addAll(ClassPath.from(cl).getTopLevelClasses(packageName));
    return this;
  }

  public ClasspathSearch includePackageRecursive(String pacakgeName) throws IOException
  {
    ci.addAll(ClassPath.from(cl).getTopLevelClassesRecursive(pacakgeName));
    return this;
  }

  public ClasspathSearch excludePackage(String packageName)
  {
    stream = stream.filter((ci) -> !ci.getPackageName().startsWith(packageName));
    return this;
  }

  public ClasspathSearch excludeClass(String className)
  {
    stream = stream.filter((ci) -> !ci.getName().startsWith(className));
    return this;
  }

  private Stream<Class<?>> loadClass(ClassInfo classInfo)
  {
    try
    {
      return Stream.of(classInfo.load());
    }
    catch (NoClassDefFoundError ex)
    {
      // When attempt to load classes fairly indiscriminately, it is common to fail because a dependency
      // is missing that is not necessary for program execution.
      LOG.debug("Failed to load class {}", classInfo.getName());
      return Stream.of();
    }
  }

  public ClassStream classStream()
  {
    return new ClassStream(stream.flatMap(this::loadClass));
  }
}

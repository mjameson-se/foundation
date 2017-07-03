package org.f8n.cornerstone.reflection;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Interface for providing arguments for reflected method invocation via {@link Invoker}
 */
public interface ArgumentProvider
{
  /**
   * Get an argument matching the given criteria
   *
   * @param position 0 based index of the argument in the function signature
   * @param type generic type information about the argument
   * @param annotations annotations applied to the parameter in the function signature
   * @return matching argument to provide, if present; else null.
   */
  Object get(int position, TypeInfo type, List<Annotation> annotations);
}
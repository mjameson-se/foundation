package org.f8n.inject.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to bind a dependency to a particular provider
 * via free-form tags. A dependency annotated with a set of tags will only
 * be satisfied by a provider also annotated with matching tags.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER, ElementType.TYPE })
public @interface Target
{
  /**
   * @return list of tags the provider must have to satisfy the dependency
   */
  String[] value();
}

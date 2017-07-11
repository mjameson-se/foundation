package org.f8n.inject.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating a class as a candidate for injection via Injector.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component
{
  /**
   * @return list of services provided by the component. If not specified explicitly, the component will be registered
   *         as providing all possible types that the component could be assigned to
   */
  Class<?>[] service() default {};

  /**
   * @return whether the component should be created as a singleton or if a distinct object should be created for each
   *         dependent instance; defaults to true (singleton behavior)
   */
  boolean singleton() default true;
}

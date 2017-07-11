package org.f8n.inject.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link Condition} points to a method used to determine if a component is ready to be instantiated, allowing user
 * logic to defer creation until some condition outside of the framework scope is met.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Condition
{
  /**
   * @return name of the method on the target class
   */
  String method();

  /**
   * @return target class, if unspecified then the annotated class is used
   */
  Class<?> target() default void.class;

  /**
   * @return String arguments to the method
   */
  String[] arguments() default {};
}

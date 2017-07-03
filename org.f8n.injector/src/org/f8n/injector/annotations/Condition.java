package org.f8n.injector.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link Condition} points to a method used to determine if a component is ready to be instantiated, allowing user
 * logic to defer creation until some condition outside of the framework scope is met.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {})
public @interface Condition
{
  String method();

  Class<?> target();

  String[] arguments();
}

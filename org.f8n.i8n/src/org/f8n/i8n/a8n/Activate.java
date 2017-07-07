package org.f8n.i8n.a8n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating which method should be invoked after an object is constructed and its dependencies bound.
 *
 * Note that when using method binding, it is often necessary to have such a method to finalize the construction with
 * all dependencies satisfied.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Activate
{
}

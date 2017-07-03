package org.f8n.injector.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.Set;

/**
 * Annotation indicating which constructor should be used to build an instance of a class and which methods should be
 * used to inject dependencies.
 *
 * Note that a dependency may be optional if the parameter is an {@link Optional}, and it may be multiple (one or more)
 * if the parameter is a {@link Set}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface Inject
{
}

package org.f8n.inject;

import java.lang.annotation.Annotation;
import java.util.List;

import org.f8n.inject.annotate.Component;

/**
 * Service component that acts as a factory for some other type, often useful for wrapping types from third party
 * packages which do not use this framework.
 *
 * A ComponentFactory<T> may be used to satisfy dependencies on type T. {@link Injector} will create an implicit
 * ComponentFactory for any {@link Component} that is not a {@link Component#singleton()}
 *
 * @param <T>
 */
public interface ComponentFactory<T>
{
  T buildComponent(List<Annotation> annotations);
}

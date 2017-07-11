package org.f8n.reflect;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper class for an {@link ArgumentProvider} to support optional parameters, delegating the actual lookup to another
 * provider.
 *
 * When a parameter is optional, this provider unwraps the type to look up the generic type, wrapping the result in an
 * optional.
 */
class OptionalArgumentProvider implements ArgumentProvider
{
  private ArgumentProvider delegate;

  public OptionalArgumentProvider(ArgumentProvider delegate)
  {
    this.delegate = delegate;
  }

  @Override
  public Object get(int position, TypeInfo type, List<Annotation> annotations)
  {
    if (type.getRawClass() == Optional.class)
      return Optional.ofNullable(delegate.get(position, type.getTypeArguments().get(0), annotations));
    return delegate.get(position, type, annotations);
  }
}

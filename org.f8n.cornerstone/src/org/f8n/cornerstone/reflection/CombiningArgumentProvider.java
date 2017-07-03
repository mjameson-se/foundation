package org.f8n.cornerstone.reflection;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper class for several {@link ArgumentProvider}s to support providing arguments from multiple sources, returning
 * the first non-null result provided by a delegate.
 */
class CombiningArgumentProvider implements ArgumentProvider
{
  private List<ArgumentProvider> delegates;

  public CombiningArgumentProvider(ArgumentProvider... delegates)
  {
    this.delegates = Arrays.asList(delegates);
  }

  @Override
  public Object get(int position, TypeInfo clazz, List<Annotation> annotations)
  {
    return delegates.stream()
                    .map(delegate -> delegate.get(position, clazz, annotations))
                    .filter(ret -> ret != null)
                    .findFirst()
                    .orElse(null);
  }
}

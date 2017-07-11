package org.f8n.reflect;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Simple implementation of {@link ArgumentProvider} for trivial cases (like tests) where the arguments can be provided
 * from a fixed list by position
 */
public class SimpleArgumentProvider implements ArgumentProvider
{
  private List<Object> args;

  public SimpleArgumentProvider(List<Object> args)
  {
    this.args = args;
  }

  @Override
  public Object get(int position, TypeInfo clazz, List<Annotation> annotations)
  {
    return args.get(position);
  }
}

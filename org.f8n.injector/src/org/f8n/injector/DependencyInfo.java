package org.f8n.injector;

import org.f8n.cornerstone.reflection.TypeInfo;

import com.google.common.base.MoreObjects;

/**
 * Info about a Dependency, both the type of the service required and the cardinality.
 */
class DependencyInfo
{
  private final TypeInfo type;
  private final Cardinality cardinality;

  public enum Cardinality
  {
    OPTIONAL, SINGLE, MULTIPLE
  }

  public DependencyInfo(TypeInfo type, Cardinality cardinality)
  {
    this.type = type;
    this.cardinality = cardinality;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public Cardinality getCardinality()
  {
    return cardinality;
  }

  @Override
  public String toString()
  {
    return MoreObjects.toStringHelper(getClass()).add("type", type).add("cardinality", cardinality).toString();
  }
}

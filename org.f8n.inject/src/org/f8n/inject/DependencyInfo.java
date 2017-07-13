package org.f8n.inject;

import java.util.List;

import org.f8n.reflect.TypeInfo;

import com.google.common.base.MoreObjects;

/**
 * Info about a Dependency, both the type of the service required and the cardinality.
 */
class DependencyInfo
{
  private final TypeInfo type;
  private final Cardinality cardinality;
  private final List<String> tags;

  public enum Cardinality
  {
    OPTIONAL, SINGLE, MULTIPLE
  }

  public DependencyInfo(TypeInfo type, Cardinality cardinality, List<String> tags)
  {
    this.type = type;
    this.cardinality = cardinality;
    this.tags = tags;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public Cardinality getCardinality()
  {
    return cardinality;
  }

  public List<String> getTags()
  {
    return tags;
  }

  @Override
  public String toString()
  {
    return MoreObjects.toStringHelper(getClass()).add("type", type).add("cardinality", cardinality).toString();
  }
}

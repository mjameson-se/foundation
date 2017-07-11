package org.f8n.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Streams;

/**
 * Helper class for working with generic {@link Type}s
 */
public class TypeInfo
{
  private static final Logger LOG = LoggerFactory.getLogger(TypeInfo.class);
  private final Class<?> clazz;
  private final List<TypeInfo> typeParameters;

  /**
   * Create a new TypeInfo from any {@link Type}
   *
   * @param type generic type
   */
  public TypeInfo(Type type)
  {
    this.clazz = getRawClass(type);
    if (type instanceof ParameterizedType)
    {
      typeParameters = Arrays.stream(((ParameterizedType) type).getActualTypeArguments())
                             .map(TypeInfo::new)
                             .collect(Collectors.toList());
    }
    else if (type instanceof GenericArrayType)
    {
      typeParameters = Collections.singletonList(new TypeInfo(((GenericArrayType) type).getGenericComponentType()));
    }
    else
    {
      typeParameters = Collections.emptyList();
    }
  }

  /**
   * Construct a synthetic TypeInfo that would match a generic TypeInfo with the given class and parameters
   *
   * @param clazz raw class
   * @param parameters type parameters to the class, if applicable
   */
  public TypeInfo(Class<?> clazz, Class<?>... parameters)
  {
    this.clazz = Objects.requireNonNull(clazz);
    this.typeParameters = Arrays.stream(parameters).map(TypeInfo::new).collect(Collectors.toList());
  }

  /**
   * Construct a synthetic TypeInfo to match a generic TypeInfo
   *
   * @param clazz raw class
   * @param parameters synthetic TypeInfo representing the parameter types
   */
  public TypeInfo(Class<?> clazz, TypeInfo... parameters)
  {
    this.clazz = clazz;
    this.typeParameters = Arrays.asList(parameters);
  }

  /**
   * @return raw class of this type
   */
  public Class<?> getRawClass()
  {
    return clazz;
  }

  /**
   * @return TypeInfo representing the parameters of this type
   */
  public List<TypeInfo> getTypeArguments()
  {
    return typeParameters;
  }

  /**
   * @param type generic {@link Type}
   * @return raw {@link Class} represented by this generic Type
   */
  public static Class<?> getRawClass(Type type)
  {
    LOG.trace("Type is {} {}", type.getClass().getSimpleName(), type.getTypeName());
    if (type instanceof Class)
      return (Class<?>) type;
    if (type instanceof ParameterizedType)
      return getRawClass(((ParameterizedType) type).getRawType());
    if (type instanceof GenericArrayType)
      return Array.class;
    if (type instanceof WildcardType)
      return WildcardType.class;
    // TODO: really support wildcard types
    if (type instanceof TypeVariable)
    {
      TypeVariable<?> tv = (TypeVariable<?>) type;
      // TODO: we should be able to resolve the type to the actual type in some cases
      return (Class<?>) tv.getBounds()[0];
    }
    throw new UnsupportedOperationException("Cannot handle this type: " + type.getClass().getSimpleName());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(clazz, typeParameters);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (!(obj instanceof TypeInfo))
      return false;
    TypeInfo that = (TypeInfo) obj;
    return Objects.equals(this.clazz, that.clazz)
           && Objects.equals(this.typeParameters, that.typeParameters);
  }

  @Override
  public String toString()
  {
    return MoreObjects.toStringHelper(TypeInfo.class)
                      .add("clazz", clazz.getSimpleName())
                      .add("parameters", typeParameters)
                      .toString();
  }

  /**
   * @return the super type of this class, if applicable
   */
  public Optional<TypeInfo> getSuperType()
  {
    return Optional.ofNullable(clazz.getGenericSuperclass()).map(TypeInfo::new);
  }

  /**
   * @return stream of all {@link TypeInfo}s that this type is assignable to
   */
  public Stream<TypeInfo> getAssignableTypes()
  {
    Stream<TypeInfo> provides = Stream.empty();
    TypeInfo current = this;
    while (current != null)
    {
      provides = Streams.concat(provides,
                                Stream.of(current),
                                Arrays.stream(current.clazz.getGenericInterfaces())
                                      .map(TypeInfo::new)
                                      .flatMap(TypeInfo::getAssignableTypes));
      if (!current.typeParameters.isEmpty())
      {
        // Parameterized type can also assign to raw type (without parameters)
        provides = Streams.concat(provides, Stream.of(new TypeInfo(current.clazz)));
      }
      current = current.getSuperType().orElse(null);
    }
    return provides;
  }
}

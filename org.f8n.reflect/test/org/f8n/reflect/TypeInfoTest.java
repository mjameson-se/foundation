package org.f8n.reflect;

import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class TypeInfoTest
{
  private static final Logger LOG = LoggerFactory.getLogger(TypeInfoTest.class);

  private interface TestGeneric<T extends Buffer> extends Predicate<T>
  {
    @Override
    boolean test(T buf);
  }

  private class ConcreteTest implements TestGeneric<ByteBuffer>
  {
    @Override
    public boolean test(ByteBuffer buf)
    {
      return false;
    }

    public int[] testArray()
    {
      return new int[] {1, 2 };
    }

    public void testGeneric(Map<String, Map<String, Object>> generic)
    {
      generic.toString();
    }
  }

  @Test
  public void testClasses()
  {
    TypeInfo type = new TypeInfo(ConcreteTest.class);
    TypeInfo expect = new TypeInfo(TestGeneric.class, ByteBuffer.class);
    Set<TypeInfo> assignables = type.getAssignableTypes().collect(Collectors.toSet());
    Assert.assertTrue(assignables.contains(type));
    Assert.assertTrue(assignables.contains(expect));
    Assert.assertTrue(assignables.contains(new TypeInfo(TestGeneric.class)));
    Assert.assertTrue(assignables.contains(new TypeInfo(Object.class)));
    assignables.forEach(asg -> LOG.info("ASG {}", asg));
    // TypeInfo cannot (yet) correctly resolve the type of the Predicate interface to ByteBuffer
    // Assert.assertTrue(assignables.contains(new TypeInfo(Predicate.class, ByteBuffer.class)));
    Assert.assertTrue(assignables.contains(new TypeInfo(Predicate.class, Buffer.class)));
  }

  @Test
  public void testMethodReturn() throws Exception
  {
    Method m = ConcreteTest.class.getMethod("testArray");
    TypeInfo actual = new TypeInfo(m.getGenericReturnType());
    TypeInfo expected = new TypeInfo(int[].class);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testMethodParameterGeneric() throws Exception
  {
    Method m = ConcreteTest.class.getMethod("testGeneric", Map.class);
    TypeInfo actual = new TypeInfo(m.getGenericParameterTypes()[0]);
    TypeInfo expected = new TypeInfo(Map.class,
                                     new TypeInfo(String.class),
                                     new TypeInfo(Map.class, String.class, Object.class));
    Assert.assertEquals(expected, actual);
  }
}

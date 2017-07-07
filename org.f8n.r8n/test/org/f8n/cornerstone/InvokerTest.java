package org.f8n.cornerstone;

import java.util.Collections;

import org.f8n.r8n.Invoker;
import org.f8n.r8n.SimpleArgumentProvider;
import org.junit.Assert;
import org.junit.Test;

public class InvokerTest
{
  @Test
  public void testInvokerNoArgs() throws Exception
  {
    String test = "This is a test";
    Invoker invoker = new Invoker(new SimpleArgumentProvider(Collections.emptyList()));
    int length = invoker.invoke(test, String.class.getMethod("length"));
    Assert.assertEquals(test.length(), length);
  }

  @Test
  public void testInvokerOneArg() throws Exception
  {
    String test = "This is another test";
    String argument = "Catenation is fun!";
    Invoker invoker = new Invoker(new SimpleArgumentProvider(Collections.singletonList(argument)));
    String result = invoker.invoke(test, String.class.getMethod("concat", String.class));
    Assert.assertEquals(test.concat(argument), result);
  }

  @Test
  public void testInvokerNew() throws Exception
  {
    char[] argument = { 'a', 'b', 'c' };
    Invoker invoker = new Invoker(new SimpleArgumentProvider(Collections.singletonList(argument)));
    String result = invoker.buildNew(String.class.getConstructor(char[].class));
    Assert.assertEquals(new String(argument), result);
  }
}

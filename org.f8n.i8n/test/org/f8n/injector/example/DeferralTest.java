package org.f8n.injector.example;

import org.f8n.i8n.a8n.Component;
import org.f8n.i8n.a8n.Condition;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Condition(method = "defer", arguments = { "one", "two" })
public class DeferralTest
{
  private static final Logger LOG = LoggerFactory.getLogger(DeferralTest.class);

  private static boolean defer = true;

  public boolean getDefer()
  {
    return defer;
  }

  public static void setDefer(boolean newDefer)
  {
    defer = newDefer;
  }

  public static boolean defer(String one, String two)
  {
    LOG.info("Defer: {}", defer);
    Assert.assertEquals("one", one);
    Assert.assertEquals("two", two);
    return defer;
  }
}

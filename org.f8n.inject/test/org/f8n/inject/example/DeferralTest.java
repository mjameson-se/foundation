package org.f8n.inject.example;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Condition;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Condition(method = "defer", arguments = { "one", "two" }, expectForDefer = true)
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

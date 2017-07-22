package org.f8n.inject.example;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Inject;
import org.f8n.inject.annotate.Target;
import org.f8n.inject.example.api.Database;
import org.junit.Assert;

@Component(singleton = false)
@Target("cf_example")
public class ExampleComponentTwo
{
  private static int counter = 0;

  @Inject
  public ExampleComponentTwo(Database db)
  {
    if (db != null)
    {
      ++counter;
    }
  }

  public static void test()
  {
    Assert.assertTrue(counter > 1);
  }
}

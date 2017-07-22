package org.f8n.inject.example;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Inject;
import org.f8n.inject.annotate.Target;
import org.f8n.inject.example.ExampleComponentFactory.ExampleComponent;
import org.junit.Assert;

@Component
public class ExampleComponentConsumer
{
  private ExampleComponent two;
  private ExampleComponent one;
  private ExampleComponentTwo oneTwo;
  private ExampleComponentTwo twoTwo;

  @Inject
  public ExampleComponentConsumer(ExampleComponent one,
                                  @Target("cf_example") ExampleComponent two,
                                  ExampleComponentTwo oneTwo,
                                  @Target("cf_example") ExampleComponentTwo twoTwo)
  {
    this.one = one;
    this.two = two;
    this.oneTwo = oneTwo;
    this.twoTwo = twoTwo;
  }

  public void test()
  {
    Assert.assertFalse(one == two);
    Assert.assertFalse(oneTwo == twoTwo);
  }
}

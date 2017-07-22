package org.f8n.inject.example;

import java.lang.annotation.Annotation;
import java.util.List;

import org.f8n.inject.ComponentFactory;
import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Inject;
import org.f8n.inject.annotate.Target;
import org.f8n.inject.example.ExampleComponentFactory.ExampleComponent;
import org.f8n.inject.example.api.Database;
import org.junit.Assert;

@Component
@Target("cf_example")
public class ExampleComponentFactory implements ComponentFactory<ExampleComponent>
{
  private int counter = 0;
  private Database db;

  public class ExampleComponent
  {
  }

  @Inject
  public ExampleComponentFactory(Database db)
  {
    this.db = db;
  }

  @Override
  public ExampleComponent buildComponent(List<Annotation> annotations)
  {
    ++counter;
    return new ExampleComponent();
  }

  public void test()
  {
    Assert.assertTrue(counter > 1);
    Assert.assertNotNull(db);
  }
}

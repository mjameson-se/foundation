package org.f8n.injector.example;

import java.util.Optional;

import org.f8n.injector.annotations.Component;
import org.f8n.injector.annotations.Inject;
import org.f8n.injector.example.api.Database;
import org.junit.Assert;

@Component
public class OptionalDependencyTest
{
  private Optional<Database> db;

  @Inject
  public OptionalDependencyTest(Optional<Database> db)
  {
    this.db = db;
  }

  public void test()
  {
    Assert.assertTrue(db.isPresent());
  }
}

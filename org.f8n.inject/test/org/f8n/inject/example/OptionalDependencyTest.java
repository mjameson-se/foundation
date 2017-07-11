package org.f8n.inject.example;

import java.util.Optional;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Inject;
import org.f8n.inject.example.api.Database;
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

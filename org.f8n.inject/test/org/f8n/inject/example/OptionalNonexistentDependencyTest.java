package org.f8n.inject.example;

import java.util.Optional;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Inject;
import org.junit.Assert;

@Component
public class OptionalNonexistentDependencyTest
{
  private Optional<Runtime> runtime;

  @Inject
  public OptionalNonexistentDependencyTest(Optional<Runtime> runtime)
  {
    this.runtime = runtime;
  }

  public void test()
  {
    Assert.assertFalse(runtime.isPresent());
  }
}

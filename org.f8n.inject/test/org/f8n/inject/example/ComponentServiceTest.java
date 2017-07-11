package org.f8n.inject.example;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.f8n.inject.annotate.Component;

@Component(service = Supplier.class)
public class ComponentServiceTest implements Supplier<Integer>
{
  @Override
  public Integer get()
  {
    return ThreadLocalRandom.current().nextInt();
  }
}

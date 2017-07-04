package org.f8n.injector.example;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.f8n.injector.annotations.Component;

@Component(service = Supplier.class)
public class ComponentServiceTest implements Supplier<Integer>
{
  @Override
  public Integer get()
  {
    return ThreadLocalRandom.current().nextInt();
  }
}

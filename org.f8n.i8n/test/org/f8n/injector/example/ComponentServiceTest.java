package org.f8n.injector.example;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.f8n.i8n.a8n.Component;

@Component(service = Supplier.class)
public class ComponentServiceTest implements Supplier<Integer>
{
  @Override
  public Integer get()
  {
    return ThreadLocalRandom.current().nextInt();
  }
}

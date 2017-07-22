package org.f8n.inject.example;

import java.lang.annotation.Annotation;
import java.util.List;

import org.f8n.inject.ComponentFactory;
import org.f8n.inject.annotate.Component;
import org.f8n.inject.example.api.RestHandler;

@Component
public class HandlerFactoryTest implements ComponentFactory<RestHandler>
{
  @Override
  public RestHandler buildComponent(List<Annotation> annotations)
  {
    return new RestHandler()
    {
    };
  }
}

package com.f8n.rest.jackson;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class JacksonFeature implements Feature
{
  @Override
  public boolean configure(FeatureContext ctx)
  {
    ctx.register(new JacksonBodyWriter());
    ctx.register(new JacksonBodyReader());
    return true;
  }
}

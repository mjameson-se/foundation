package org.f8n.rest.client;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.f8n.rest.common.ConfigurationImpl;

public class WebTargetImpl implements WebTarget
{
  private ConnectorProvider connector;
  private ConfigurationImpl config;
  private UriBuilder uriBuilder;

  public WebTargetImpl(UriBuilder uriBuilder, ConnectorProvider connector, ConfigurationImpl config)
  {
    this.uriBuilder = uriBuilder;
    this.connector = connector;
    this.config = config;
  }

  @Override
  public Configuration getConfiguration()
  {
    return config;
  }

  @Override
  public WebTarget property(String key, Object value)
  {
    config.property(key, value);
    return this;
  }

  @Override
  public WebTarget register(Class<?> componentClass)
  {
    config.register(componentClass);
    return this;
  }

  @Override
  public WebTarget register(Object componentInstance)
  {
    config.register(componentInstance);
    return this;
  }

  @Override
  public WebTarget register(Class<?> componentClass, int priority)
  {
    config.register(componentClass, priority);
    return this;
  }

  @Override
  public WebTarget register(Class<?> componentClass, Class<?>... contracts)
  {
    config.register(componentClass, contracts);
    return this;
  }

  @Override
  public WebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts)
  {
    config.register(componentClass, contracts);
    return this;
  }

  @Override
  public WebTarget register(Object componentInstance, int priority)
  {
    config.register(componentInstance, priority);
    return this;
  }

  @Override
  public WebTarget register(Object componentInstance, Class<?>... contracts)
  {
    config.register(componentInstance, contracts);
    return this;
  }

  @Override
  public WebTarget register(Object componentInstance, Map<Class<?>, Integer> contracts)
  {
    config.register(componentInstance, contracts);
    return this;
  }

  @Override
  public URI getUri()
  {
    return uriBuilder.build();
  }

  @Override
  public UriBuilder getUriBuilder()
  {
    return uriBuilder.clone();
  }

  @Override
  public WebTarget matrixParam(String arg0, Object... arg1)
  {
    uriBuilder.matrixParam(arg0, arg1);
    return this;
  }

  @Override
  public WebTarget path(String arg0)
  {
    uriBuilder.path(arg0);
    return this;
  }

  @Override
  public WebTarget queryParam(String arg0, Object... arg1)
  {
    uriBuilder.queryParam(arg0, arg1);
    return this;
  }

  @Override
  public Builder request()
  {
    return new InvocationBuilderImpl(config, connector, uriBuilder.build());
  }

  @Override
  public Builder request(String... arg0)
  {
    return request().accept(arg0);
  }

  @Override
  public Builder request(MediaType... arg0)
  {
    return request().accept(arg0);
  }

  @Override
  public WebTarget resolveTemplate(String arg0, Object arg1)
  {
    uriBuilder.resolveTemplate(arg0, arg1);
    return this;
  }

  @Override
  public WebTarget resolveTemplate(String arg0, Object arg1, boolean arg2)
  {
    uriBuilder.resolveTemplate(arg0, arg1, arg2);
    return this;
  }

  @Override
  public WebTarget resolveTemplateFromEncoded(String arg0, Object arg1)
  {
    uriBuilder.resolveTemplateFromEncoded(arg0, arg1);
    return this;
  }

  @Override
  public WebTarget resolveTemplates(Map<String, Object> arg0)
  {
    uriBuilder.resolveTemplates(arg0);
    return this;
  }

  @Override
  public WebTarget resolveTemplates(Map<String, Object> arg0, boolean arg1)
  {
    uriBuilder.resolveTemplates(arg0, arg1);
    return this;
  }

  @Override
  public WebTarget resolveTemplatesFromEncoded(Map<String, Object> arg0)
  {
    uriBuilder.resolveTemplatesFromEncoded(arg0);
    return this;
  }
}

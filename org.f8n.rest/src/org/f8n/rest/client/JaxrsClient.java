package org.f8n.rest.client;

import java.net.URI;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.f8n.rest.common.ConfigurationImpl;

public class JaxrsClient implements Client
{
  private ConfigurationImpl config = new ConfigurationImpl(RuntimeType.CLIENT);
  private ConnectorProvider connector;

  public JaxrsClient(ConnectorProvider connector)
  {
    this.connector = connector;
  }

  @Override
  public Configuration getConfiguration()
  {
    return config;
  }

  @Override
  public Client property(String key, Object value)
  {
    config.property(key, value);
    return this;
  }

  @Override
  public Client register(Class<?> componentClass)
  {
    config.register(componentClass);
    return this;
  }

  @Override
  public Client register(Object componentInstance)
  {
    config.register(componentInstance);
    return this;
  }

  @Override
  public Client register(Class<?> componentClass, int priority)
  {
    config.register(componentClass, priority);
    return this;
  }

  @Override
  public Client register(Class<?> componentClass, Class<?>... contracts)
  {
    config.register(componentClass, contracts);
    return this;
  }

  @Override
  public Client register(Class<?> componentClass, Map<Class<?>, Integer> contracts)
  {
    config.register(componentClass, contracts);
    return this;
  }

  @Override
  public Client register(Object componentInstance, int priority)
  {
    config.register(componentInstance, priority);
    return this;
  }

  @Override
  public Client register(Object componentInstance, Class<?>... contracts)
  {
    config.register(componentInstance, contracts);
    return this;
  }

  @Override
  public Client register(Object componentInstance, Map<Class<?>, Integer> contracts)
  {
    config.register(componentInstance, contracts);
    return this;
  }

  @Override
  public void close()
  {
    connector.close();
  }

  @Override
  public HostnameVerifier getHostnameVerifier()
  {
    return connector.getHostnameVerifier();
  }

  @Override
  public SSLContext getSslContext()
  {
    return connector.getSslContext();
  }

  @Override
  public Builder invocation(Link link)
  {
    return target(link).request();
  }

  @Override
  public WebTarget target(String uriString)
  {
    return target(URI.create(uriString));
  }

  @Override
  public WebTarget target(URI uri)
  {
    return target(UriBuilder.fromUri(uri));
  }

  @Override
  public WebTarget target(UriBuilder uriBuilder)
  {
    return new WebTargetImpl(uriBuilder, connector, config.copy());
  }

  @Override
  public WebTarget target(Link link)
  {
    return target(link.getUri());
  }
}

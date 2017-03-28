package org.f8n.rest.client;

import java.net.URI;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

public class JaxrsClient implements Client
{

  @Override
  public Configuration getConfiguration()
  {
    return null;
  }

  @Override
  public Client property(String arg0, Object arg1)
  {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public Client register(Class<?> arg0)
  {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public Client register(Object arg0)
  {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public Client register(Class<?> arg0, int arg1)
  {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public Client register(Class<?> arg0, Class<?>... arg1)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Client register(Class<?> arg0, Map<Class<?>, Integer> arg1)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Client register(Object arg0, int arg1)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Client register(Object arg0, Class<?>... arg1)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Client register(Object arg0, Map<Class<?>, Integer> arg1)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public HostnameVerifier getHostnameVerifier()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SSLContext getSslContext()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Builder invocation(Link arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WebTarget target(String uriString)
  {
    return target(URI.create(uriString));
  }

  @Override
  public WebTarget target(URI arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WebTarget target(UriBuilder uriBuilder)
  {
    return target(uriBuilder.build());
  }

  @Override
  public WebTarget target(Link link)
  {
    return target(link.getUri());
  }

}

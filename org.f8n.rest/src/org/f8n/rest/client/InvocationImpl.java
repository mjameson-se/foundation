package org.f8n.rest.client;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.google.common.base.Joiner;
import com.google.common.net.HttpHeaders;

public class InvocationImpl implements Invocation
{
  static class BuilderImpl implements Builder
  {
    private MultivaluedMap<String, String> headers;
    private Map<String, Object> properties;

    @Override
    public Response delete()
    {
      return buildDelete().invoke();
    }

    @Override
    public <T> T delete(Class<T> arg0)
    {
      return buildDelete().invoke(arg0);
    }

    @Override
    public <T> T delete(GenericType<T> arg0)
    {
      return buildDelete().invoke(arg0);
    }

    @Override
    public Response get()
    {
      return buildGet().invoke();
    }

    @Override
    public <T> T get(Class<T> arg0)
    {
      return buildGet().invoke(arg0);
    }

    @Override
    public <T> T get(GenericType<T> arg0)
    {
      return buildGet().invoke(arg0);
    }

    @Override
    public Response head()
    {
      return build("HEAD").invoke();
    }

    @Override
    public Response method(String arg0)
    {
      return build(arg0).invoke();
    }

    @Override
    public <T> T method(String arg0, Class<T> arg1)
    {
      return build(arg0).invoke(arg1);
    }

    @Override
    public <T> T method(String arg0, GenericType<T> arg1)
    {
      return build(arg0).invoke(arg1);
    }

    @Override
    public Response method(String arg0, Entity<?> arg1)
    {
      return build(arg0, arg1).invoke();
    }

    @Override
    public <T> T method(String arg0, Entity<?> arg1, Class<T> arg2)
    {
      return build(arg0, arg1).invoke(arg2);
    }

    @Override
    public <T> T method(String arg0, Entity<?> arg1, GenericType<T> arg2)
    {
      return build(arg0, arg1).invoke(arg2);
    }

    @Override
    public Response options()
    {
      return build(HttpMethod.OPTIONS).invoke();
    }

    @Override
    public <T> T options(Class<T> arg0)
    {
      return build(HttpMethod.OPTIONS).invoke(arg0);
    }

    @Override
    public <T> T options(GenericType<T> arg0)
    {
      return build(HttpMethod.OPTIONS).invoke(arg0);
    }

    @Override
    public Response post(Entity<?> arg0)
    {
      return buildPost(arg0).invoke();
    }

    @Override
    public <T> T post(Entity<?> arg0, Class<T> arg1)
    {
      return buildPost(arg0).invoke(arg1);
    }

    @Override
    public <T> T post(Entity<?> arg0, GenericType<T> arg1)
    {
      return buildPost(arg0).invoke(arg1);
    }

    @Override
    public Response put(Entity<?> arg0)
    {
      return buildPut(arg0).invoke();
    }

    @Override
    public <T> T put(Entity<?> arg0, Class<T> arg1)
    {
      return buildPut(arg0).invoke(arg1);
    }

    @Override
    public <T> T put(Entity<?> arg0, GenericType<T> arg1)
    {
      return buildPut(arg0).invoke(arg1);
    }

    @Override
    public Response trace()
    {
      return build("TRACE").invoke();
    }

    @Override
    public <T> T trace(Class<T> arg0)
    {
      return build("TRACE").invoke(arg0);
    }

    @Override
    public <T> T trace(GenericType<T> arg0)
    {
      return build("TRACE").invoke(arg0);
    }

    @Override
    public Builder accept(String... arg0)
    {
      return header(HttpHeaders.ACCEPT, Joiner.on(',').join(arg0));
    }

    @Override
    public Builder accept(MediaType... arg0)
    {
      return header(HttpHeaders.ACCEPT_ENCODING, Joiner.on(',').join(arg0));
    }

    @Override
    public Builder acceptEncoding(String... arg0)
    {
      return header(HttpHeaders.ACCEPT_ENCODING, Joiner.on(',').join(arg0));
    }

    @Override
    public Builder acceptLanguage(Locale... arg0)
    {
      return header(HttpHeaders.ACCEPT_LANGUAGE, Joiner.on(",").join(arg0));
    }

    @Override
    public Builder acceptLanguage(String... arg0)
    {
      return header(HttpHeaders.ACCEPT_LANGUAGE, Joiner.on(",").join(arg0));
    }

    @Override
    public AsyncInvoker async()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Invocation build(String arg0)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Invocation build(String arg0, Entity<?> arg1)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Invocation buildDelete()
    {
      return build(HttpMethod.DELETE);
    }

    @Override
    public Invocation buildGet()
    {
      return build(HttpMethod.GET);
    }

    @Override
    public Invocation buildPost(Entity<?> arg0)
    {
      return build(HttpMethod.POST, arg0);
    }

    @Override
    public Invocation buildPut(Entity<?> arg0)
    {
      return build(HttpMethod.PUT, arg0);
    }

    @Override
    public Builder cacheControl(CacheControl arg0)
    {
      return header(HttpHeaders.CACHE_CONTROL, arg0);
    }

    @Override
    public Builder cookie(Cookie arg0)
    {
      return header(HttpHeaders.COOKIE, arg0);
    }

    @Override
    public Builder cookie(String arg0, String arg1)
    {
      return header(HttpHeaders.COOKIE, String.format("%s=%s", arg0, arg1));
    }

    @Override
    public Builder header(String arg0, Object arg1)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Builder headers(MultivaluedMap<String, Object> arg0)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Builder property(String arg0, Object arg1)
    {
      // TODO Auto-generated method stub
      return null;
    }
  }

  @Override
  public Response invoke()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T invoke(Class<T> arg0)
  {
    return invoke().readEntity(arg0);
  }

  @Override
  public <T> T invoke(GenericType<T> arg0)
  {
    return invoke().readEntity(arg0);
  }

  @Override
  public Invocation property(String arg0, Object arg1)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Future<Response> submit()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> Future<T> submit(Class<T> arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> Future<T> submit(GenericType<T> arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> Future<T> submit(InvocationCallback<T> arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }
}

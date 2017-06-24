package org.f8n.rest.client;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.f8n.rest.common.ConfigurationImpl;
import org.f8n.rest.common.HeaderDelegates;
import org.f8n.rest.common.HttpRequest;

import com.google.common.net.HttpHeaders;

class InvocationBuilderImpl implements Builder
{
  private HttpRequest request = new HttpRequest();
  private ConfigurationImpl config;
  private ConnectorProvider connector;

  public InvocationBuilderImpl(ConfigurationImpl config, ConnectorProvider connector, URI targetUri)
  {
    request.setRequestUri(targetUri);
    this.config = config;
    this.connector = connector;
  }

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
  public Builder accept(String... mediaTypes)
  {
    for (String mediaType : mediaTypes)
    {
      header(HttpHeaders.ACCEPT, mediaType);
    }
    return this;
  }

  @Override
  public Builder accept(MediaType... mediaTypes)
  {
    for (MediaType mediaType : mediaTypes)
    {
      header(HttpHeaders.ACCEPT, mediaType);
    }
    return this;
  }

  @Override
  public Builder acceptEncoding(String... encodings)
  {
    Arrays.stream(encodings).forEach(encoding -> header(HttpHeaders.ACCEPT_ENCODING, encoding));
    return this;
  }

  @Override
  public Builder acceptLanguage(Locale... locales)
  {
    Arrays.stream(locales).forEach(locale -> header(HttpHeaders.ACCEPT_LANGUAGE, locale));
    return this;
  }

  @Override
  public Builder acceptLanguage(String... languages)
  {
    Arrays.stream(languages).forEach(language -> header(HttpHeaders.ACCEPT_LANGUAGE, language));
    return this;
  }

  @Override
  public AsyncInvoker async()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Invocation build(String method)
  {
    return build(method, null);
  }

  @Override
  public Invocation build(String method, Entity<?> entity)
  {
    HttpRequest reqCopy = request.copy();
    reqCopy.setMethod(method);
    return new InvocationImpl(config.copy(), connector, reqCopy, entity);
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
  public Builder header(String key, Object value)
  {
    request.addHeaders(key, HeaderDelegates.toString(value));
    return this;
  }

  @Override
  public Builder headers(MultivaluedMap<String, Object> headers)
  {
    request.clearHeaders();
    headers.forEach((k, lv) -> lv.forEach(v -> header(k, v)));
    return this;
  }

  @Override
  public Builder property(String key, Object value)
  {
    config.property(key, value);
    return this;
  }
}
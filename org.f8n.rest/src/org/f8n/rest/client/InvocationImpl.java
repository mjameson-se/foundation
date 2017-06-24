package org.f8n.rest.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.f8n.rest.common.ConfigurationImpl;
import org.f8n.rest.common.HttpRequest;

import com.google.common.util.concurrent.Futures;

public class InvocationImpl implements Invocation
{

  private ConfigurationImpl config;
  private ConnectorProvider connector;
  private Providers providers;
  private HttpRequest request;
  private Entity<?> entity;

  public InvocationImpl(ConfigurationImpl config, ConnectorProvider connector, HttpRequest request, Entity<?> entity)
  {
    this.config = config;
    this.connector = connector;
    this.request = request;
    this.entity = entity;
  }

  @Override
  public Response invoke()
  {
    // TODO: let configured request filters do their thing
    try (Connection connection = connector.invoke(request))
    {
      if (entity != null)
      {
        writeEntity(connection);
      }
      connection.complete();
      return transformResponse(connection);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private Response transformResponse(Connection connection)
  {
    return null;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void writeEntity(Connection connection) throws WebApplicationException, IOException
  {
    if (entity == null)
      return;
    Object realEntity = entity.getEntity();
    Class<?> entityClass;
    Type entityType;
    if (realEntity instanceof GenericEntity)
    {
      GenericEntity<?> ge = (GenericEntity<?>) realEntity;
      realEntity = ge.getEntity();
      entityType = ge.getType();
      entityClass = ge.getRawType();
    }
    else
    {
      entityClass = realEntity.getClass();
      entityType = entityClass;
    }
    MessageBodyWriter writer = providers.getMessageBodyWriter(entityClass,
                                                              entityType,
                                                              entity.getAnnotations(),
                                                              entity.getMediaType());
    writer.writeTo(realEntity,
                   entityClass,
                   entityType,
                   entity.getAnnotations(),
                   entity.getMediaType(),
                   request.getHeaders(),
                   connection.requestOutputStream());
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
  public Invocation property(String key, Object value)
  {
    config.property(key, value);
    return this;
  }

  @Override
  public Future<Response> submit()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Future<T> submit(Class<T> arg0)
  {
    return Futures.lazyTransform(submit(), r -> r.readEntity(arg0));
  }

  @Override
  public <T> Future<T> submit(GenericType<T> arg0)
  {
    return Futures.lazyTransform(submit(), r -> r.readEntity(arg0));
  }

  @Override
  public <T> Future<T> submit(InvocationCallback<T> arg0)
  {
    throw new UnsupportedOperationException();
  }
}

package org.f8n.rest.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ClosedInputStream;
import org.f8n.rest.common.HeaderDelegates;

import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;

public class ResponseImpl extends Response
{
  private MultivaluedMap<String, String> headers;
  private int statusCode;
  private Object entity;
  private Connection connection;
  private Providers providers;

  public ResponseImpl(int statusCode, MultivaluedMap<String, String> headers, Object entity, Connection connection)
  {
    this.statusCode = statusCode;
    this.headers = headers;
    this.entity = entity;
    this.connection = connection;
  }

  @Override
  public boolean bufferEntity()
  {
    if (entity instanceof InputStream)
    {
      try
      {
        entity = IOUtils.toByteArray((InputStream) entity);
        connection.close();
        return true;
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    return false;
  }

  @Override
  public void close()
  {
    try
    {
      connection.close();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Set<String> getAllowedMethods()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.ALLOW))
                   .map(s -> Arrays.stream(s.split(",")).map(String::trim).collect(Collectors.toSet()))
                   .orElse(null);
  }

  @Override
  public Map<String, NewCookie> getCookies()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getDate()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.DATE))
                   .map(s -> HeaderDelegates.fromString(Date.class, s))
                   .orElse(null);
  }

  @Override
  public Object getEntity()
  {
    return entity;
  }

  @Override
  public EntityTag getEntityTag()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.ETAG))
                   .map(s -> HeaderDelegates.fromString(EntityTag.class, s))
                   .orElse(null);
  }

  @Override
  public String getHeaderString(String key)
  {
    return headers.getFirst(key);
  }

  @Override
  public Locale getLanguage()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.CONTENT_LANGUAGE))
                   .map(s -> HeaderDelegates.fromString(Locale.class, s))
                   .orElse(null);
  }

  @Override
  public Date getLastModified()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.LAST_MODIFIED))
                   .map(s -> HeaderDelegates.fromString(Date.class, s))
                   .orElse(null);
  }

  @Override
  public int getLength()
  {
    String len = getHeaderString(HttpHeaders.CONTENT_LENGTH);
    if (len != null)
    {
      try
      {
        return Integer.parseInt(len);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return -1;
  }

  @Override
  public Link getLink(String arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Builder getLinkBuilder(String arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Link> getLinks()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI getLocation()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.LOCATION)).map(URI::create).orElse(null);
  }

  @Override
  public MediaType getMediaType()
  {
    return Optional.ofNullable(getHeaderString(HttpHeaders.CONTENT_TYPE))
                   .map(s -> HeaderDelegates.fromString(MediaType.class, s))
                   .orElse(null);
  }

  @Override
  public MultivaluedMap<String, Object> getMetadata()
  {
    return new MultivaluedHashMap<>(headers);
  }

  @Override
  public int getStatus()
  {
    return statusCode;
  }

  @Override
  public StatusType getStatusInfo()
  {
    return Status.fromStatusCode(getStatus());
  }

  @Override
  public MultivaluedMap<String, String> getStringHeaders()
  {
    return headers;
  }

  @Override
  public boolean hasEntity()
  {
    return entity != null;
  }

  @Override
  public boolean hasLink(String arg0)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public <T> T readEntity(Class<T> arg0)
  {
    return readEntity(arg0, new Annotation[0]);
  }

  @Override
  public <T> T readEntity(GenericType<T> arg0)
  {
    return readEntity(arg0, new Annotation[0]);
  }

  private InputStream entityAsStream()
  {
    if (entity == null)
      return ClosedInputStream.CLOSED_INPUT_STREAM;
    if (entity instanceof InputStream)
      return (InputStream) entity;
    if (entity instanceof byte[])
      return new ByteArrayInputStream((byte[]) entity);
    throw new IllegalStateException("Entity in unknown state: " + entity.getClass().getName());
  }

  @Override
  public <T> T readEntity(Class<T> entityClass, Annotation[] annotations)
  {
    Preconditions.checkArgument(entityClass != null && annotations != null);
    return readEntity(entityClass, entityClass, annotations);
  }

  private <T> T readEntity(Class<T> entityClass, Type type, Annotation[] annotations)
  {
    MediaType mediaType = getMediaType();
    MessageBodyReader<T> reader = providers.getMessageBodyReader(entityClass, entityClass, annotations, mediaType);
    if (reader != null)
    {
      try
      {
        return reader.readFrom(entityClass, type, annotations, mediaType, headers, entityAsStream());
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalStateException("Cannot read entity as " + entityClass.getName());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations)
  {
    Preconditions.checkArgument(entityType != null && annotations != null);
    return readEntity((Class<T>) entityType.getRawType(), entityType.getType(), annotations);
  }
}

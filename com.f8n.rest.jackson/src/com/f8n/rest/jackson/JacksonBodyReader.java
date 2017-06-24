package com.f8n.rest.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.fasterxml.jackson.databind.ObjectReader;

public class JacksonBodyReader implements MessageBodyReader<Object>
{
  private ObjectReader reader;

  @Override
  public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3)
  {
    // TODO: detect annotations on class?
    return arg3.getSubtype().contains("json");
  }

  @Override
  public Object readFrom(Class<Object> arg0,
                         Type arg1,
                         Annotation[] arg2,
                         MediaType arg3,
                         MultivaluedMap<String, String> arg4,
                         InputStream arg5)
      throws IOException, WebApplicationException
  {
    return reader.forType(arg0).readValue(arg5);
  }

}

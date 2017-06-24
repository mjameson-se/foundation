package com.f8n.rest.jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonBodyWriter implements MessageBodyWriter<Object>
{
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4)
  {
    return -1;
  }

  @Override
  public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3)
  {
    return arg3.getSubtype().contains("json");
  }

  @Override
  public void writeTo(Object arg0,
                      Class<?> arg1,
                      Type arg2,
                      Annotation[] arg3,
                      MediaType arg4,
                      MultivaluedMap<String, Object> arg5,
                      OutputStream arg6)
      throws IOException
  {
    mapper.writeValue(arg6, arg0);
  }
}

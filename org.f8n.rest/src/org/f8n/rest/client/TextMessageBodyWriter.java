package org.f8n.rest.client;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

public class TextMessageBodyWriter implements MessageBodyWriter<String>
{
  @Override
  public long getSize(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4)
  {
    return -1;
  }

  @Override
  public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3)
  {
    return arg0.equals(String.class) && arg3.getType().equals("text");
  }

  @Override
  public void writeTo(String arg0,
                      Class<?> arg1,
                      Type arg2,
                      Annotation[] arg3,
                      MediaType arg4,
                      MultivaluedMap<String, Object> arg5,
                      OutputStream arg6)
      throws IOException, WebApplicationException
  {
    arg6.write(arg0.getBytes(StandardCharsets.UTF_8));
  }
}

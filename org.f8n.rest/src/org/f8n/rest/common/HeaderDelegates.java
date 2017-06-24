package org.f8n.rest.common;

import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

public class HeaderDelegates
{

  static class MediaTypeDelegate implements HeaderDelegate<MediaType>
  {
    @Override
    public MediaType fromString(String mtString)
    {
      com.google.common.net.MediaType mediaType = com.google.common.net.MediaType.parse(mtString);
      return new MediaType(mediaType.type(), mediaType.subtype());
    }

    @Override
    public String toString(MediaType mt)
    {
      return toStringImpl(mt);
    }

    private static String toStringImpl(MediaType mt)
    {
      com.google.common.net.MediaType mediaType = com.google.common.net.MediaType.create(mt.getType(),
                                                                                         mt.getSubtype());
      for (Entry<String, String> entry : mt.getParameters().entrySet())
      {
        mediaType = mediaType.withParameter(entry.getKey(), entry.getValue());
      }
      return mediaType.toString();
    }

    public static MediaType fromStringImpl(String headerString)
    {
      com.google.common.net.MediaType mediaType = com.google.common.net.MediaType.parse(headerString);

      MediaType ret = new MediaType(mediaType.type(), mediaType.subtype());
      if (mediaType.charset().isPresent())
      {
        ret = ret.withCharset(mediaType.charset().get().name());
      }
      mediaType.parameters().forEach(ret.getParameters()::put);
      return ret;
    }
  }

  public static String toString(Object value)
  {
    if (value instanceof MediaType)
      return MediaTypeDelegate.toStringImpl((MediaType) value);
    return value.toString();
  }

  @SuppressWarnings("unchecked")
  public static <T> T fromString(Class<T> class1, String headerString)
  {
    if (class1 == MediaType.class)
      return (T) MediaTypeDelegate.fromStringImpl(headerString);
    return null;
  }
}

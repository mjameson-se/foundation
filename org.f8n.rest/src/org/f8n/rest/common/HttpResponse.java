package org.f8n.rest.common;

import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

public class HttpResponse
{
  MultivaluedMap<String, String> headers;
  int status;
  InputStream responseBody;
}

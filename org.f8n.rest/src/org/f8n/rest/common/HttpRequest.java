package org.f8n.rest.common;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class HttpRequest
{
  private URI requestUri;
  private MultivaluedMap<String, String> headers;
  private Map<String, Object> properties;
  private String method;

  public HttpRequest()
  {

  }

  private HttpRequest(URI requestUri,
                      MultivaluedMap<String, String> headers,
                      Map<String, Object> properties,
                      String method)
  {
    this.requestUri = requestUri;
    this.headers = headers;
    this.properties = properties;
    this.method = method;
  }

  public URI getRequestUri()
  {
    return requestUri;
  }

  public void setRequestUri(URI requestUri)
  {
    this.requestUri = requestUri;
  }

  public MultivaluedMap<String, String> getHeaders()
  {
    return headers;
  }

  public void addHeaders(String key, String... values)
  {
    this.headers.addAll(key, Arrays.asList(values));
  }

  public Map<String, Object> getProperties()
  {
    return Collections.unmodifiableMap(properties);
  }

  public void setProperties(Map<String, Object> properties)
  {
    this.properties = properties;
  }

  public String getMethod()
  {
    return method;
  }

  public void setMethod(String method)
  {
    this.method = method;
  }

  public HttpRequest copy()
  {
    return new HttpRequest(requestUri, new MultivaluedHashMap<>(headers), new HashMap<>(properties), method);
  }

  public void clearHeaders()
  {
    headers.clear();
  }
}

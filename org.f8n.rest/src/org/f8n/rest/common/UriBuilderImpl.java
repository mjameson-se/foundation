package org.f8n.rest.common;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("rawtypes")
public class UriBuilderImpl extends UriBuilder
{
  private static final Pattern TEMPLATE = Pattern.compile("\\{(\\w[\\w\\.-]\\}");
  private String scheme;
  private String userInfo;
  private String host;
  private int port;
  private String path;
  private MultivaluedMap<String, Object> queryParts = new MultivaluedHashMap<>();
  private String fragment;

  @Override
  public URI build(Object... arg0) throws IllegalArgumentException, UriBuilderException
  {
    return build(arg0, true);
  }

  private String urlEncodeQueryPair(Entry<String, Object> qp)
  {
    try
    {
      return String.format("%s=%s",
                           URLEncoder.encode(qp.getKey(), StandardCharsets.UTF_8.name()),
                           URLEncoder.encode(qp.getKey(), StandardCharsets.UTF_8.name()));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
  }

  private String buildQueryString()
  {
    if (queryParts.isEmpty())
      return null;
    return queryParts.entrySet()
                     .stream()
                     .flatMap(e -> e.getValue().stream().map(v -> Maps.immutableEntry(e.getKey(), v)))
                     .map(this::urlEncodeQueryPair)
                     .reduce("?", String::concat);
  }

  @Override
  public URI build(Object[] templateParams, boolean encodeSlashInPath)
      throws IllegalArgumentException, UriBuilderException
  {
    List<Object> arg0 = Arrays.asList(templateParams);
    List<String> templateParts = Lists.transform(arg0, Object::toString);
    if (encodeSlashInPath)
    {
      templateParts = Lists.transform(templateParts, s -> s.replaceAll("/", "%2f"));
    }
    Matcher matcher = TEMPLATE.matcher(path);
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < templateParts.size() && matcher.find(); ++i)
    {
      matcher.appendReplacement(buf, templateParts.get(i));
    }
    matcher.appendTail(buf);
    try
    {
      // TODO: support templates in other parts
      return new URI(scheme, userInfo, host, port, buf.toString(), buildQueryString(), fragment);
    }
    catch (URISyntaxException e)
    {
      throw new UriBuilderException(e);
    }
  }

  @Override
  public URI buildFromEncoded(Object... arg0) throws IllegalArgumentException, UriBuilderException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI buildFromEncodedMap(Map<String, ?> arg0) throws IllegalArgumentException, UriBuilderException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI buildFromMap(Map<String, ?> templateParts)
  {
    return buildFromMap(templateParts, true);
  }

  @Override
  public URI buildFromMap(Map<String, ?> templateParts, boolean encodeSlashInPath)
      throws IllegalArgumentException, UriBuilderException
  {
    Matcher matcher = TEMPLATE.matcher(path);
    StringBuffer buf = new StringBuffer();
    while (matcher.find())
    {
      String key = matcher.group(1);
      String value = templateParts.get(key).toString();
      if (encodeSlashInPath)
      {
        value = value.replaceAll("/", "%2f");
      }
      matcher.appendReplacement(buf, value);
    }
    matcher.appendTail(buf);
    try
    {
      // TODO: support templates in other parts
      return new URI(scheme, userInfo, host, port, buf.toString(), buildQueryString(), fragment);
    }
    catch (URISyntaxException e)
    {
      throw new UriBuilderException(e);
    }
  }

  @Override
  public UriBuilder clone()
  {
    return null;
  }

  @Override
  public UriBuilder fragment(String fragment)
  {
    this.fragment = fragment;
    return this;
  }

  @Override
  public UriBuilder host(String host)
  {
    this.host = host;
    return this;
  }

  @Override
  public UriBuilder matrixParam(String key, Object... values)
  {
    this.path = Arrays.stream(values)
                      .map(Object::toString)
                      .reduce(path, (i, v) -> String.format("%s;%s=%s", i, key, v));
    return this;
  }

  @Override
  public UriBuilder path(String arg0)
  {
    if (!arg0.startsWith("/"))
    {
      this.path += "/";
    }
    this.path += arg0;
    return this;
  }

  @Override
  public UriBuilder path(Class cls)
  {
    return path(((Class<?>) cls).getAnnotation(Path.class).value());
  }

  @Override
  public UriBuilder path(Method method)
  {
    return path(method.getAnnotation(Path.class).value());
  }

  @Override
  public UriBuilder path(Class cls, String methodName)
  {
    return path(Arrays.stream(cls.getMethods())
                      .filter(m -> m.getName().equals(methodName))
                      .filter(m -> m.isAnnotationPresent(Path.class))
                      .findFirst()
                      .orElseThrow(IllegalArgumentException::new));
  }

  @Override
  public UriBuilder port(int port)
  {
    this.port = port;
    return this;
  }

  @Override
  public UriBuilder queryParam(String key, Object... values)
  {
    queryParts.addAll(key, values);
    return null;
  }

  @Override
  public UriBuilder replaceMatrix(String matrix)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replaceMatrixParam(String arg0, Object... arg1)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replacePath(String path)
  {
    this.path = path;
    return this;
  }

  @Override
  public UriBuilder replaceQuery(String queryString)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replaceQueryParam(String key, Object... values)
  {
    queryParts.remove(key);
    return queryParam(key, values);
  }

  @Override
  public UriBuilder resolveTemplate(String name, Object value)
  {
    return resolveTemplate(name, value, true);
  }

  @Override
  public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath)
  {
    String templateValue = value.toString();
    if (encodeSlashInPath)
    {
      templateValue = templateValue.replaceAll("/", "%2f");
    }
    this.path = path.replaceAll("\\{" + name + "\\}", templateValue);
    // TODO: support templates in other places
    return this;
  }

  @Override
  public UriBuilder resolveTemplateFromEncoded(String arg0, Object arg1)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder resolveTemplates(Map<String, Object> arg0)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder resolveTemplates(Map<String, Object> arg0, boolean arg1) throws IllegalArgumentException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> arg0)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder scheme(String scheme)
  {
    this.scheme = scheme;
    return this;
  }

  @Override
  public UriBuilder schemeSpecificPart(String arg0)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder segment(String... segments)
  {
    for (String segment : segments)
    {
      path(segment);
    }
    return this;
  }

  @Override
  public String toTemplate()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder uri(URI uri)
  {
    Optional.ofNullable(uri.getScheme()).ifPresent(this::scheme);
    Optional.ofNullable(uri.getUserInfo()).ifPresent(this::userInfo);
    Optional.ofNullable(uri.getHost()).ifPresent(this::host);
    Optional.ofNullable(uri.getPort()).ifPresent(this::port);
    Optional.ofNullable(uri.getQuery()).ifPresent(this::replaceQuery);
    Optional.ofNullable(uri.getFragment()).ifPresent(this::fragment);
    return this;
  }

  @Override
  public UriBuilder uri(String uri)
  {
    return uri(URI.create(uri));
  }

  @Override
  public UriBuilder userInfo(String userInfo)
  {
    this.userInfo = userInfo;
    return this;
  }
}

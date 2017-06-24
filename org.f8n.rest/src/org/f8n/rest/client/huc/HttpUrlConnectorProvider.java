package org.f8n.rest.client.huc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.f8n.rest.client.Connection;
import org.f8n.rest.client.ConnectorProvider;
import org.f8n.rest.common.HttpRequest;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class HttpUrlConnectorProvider implements ConnectorProvider
{
  private static class HucConnection implements Connection
  {
    private HttpURLConnection huc;
    private boolean connected = false;

    public HucConnection(HttpURLConnection huc)
    {
      this.huc = huc;
    }

    @Override
    public OutputStream requestOutputStream() throws IOException
    {
      Preconditions.checkState(!connected);
      huc.connect();
      return huc.getOutputStream();
    }

    @Override
    public void complete() throws IOException
    {
      if (!connected)
      {
        connected = true;
        huc.connect();
      }
    }

    @Override
    public Multimap<String, String> responseHeaders()
    {
      Preconditions.checkState(connected);
      Multimap<String, String> headers = ArrayListMultimap.create();
      huc.getHeaderFields().forEach(headers::putAll);
      return headers;
    }

    @Override
    public int responseStatusCode() throws IOException
    {
      return huc.getResponseCode();
    }

    @Override
    public InputStream responseBody() throws IOException
    {
      return huc.getInputStream();
    }

    @Override
    public void close()
    {
      huc.disconnect();
    }
  }

  @Override
  public SSLContext getSslContext()
  {
    return null;
  }

  @Override
  public HostnameVerifier getHostnameVerifier()
  {
    return null;
  }

  @Override
  public Connection invoke(HttpRequest request)
  {
    try
    {
      HttpURLConnection huc = (HttpURLConnection) request.getRequestUri().toURL().openConnection();
      huc.setRequestMethod(request.getMethod());
      request.getHeaders().forEach((k, v) -> v.forEach(h -> huc.setRequestProperty(k, h)));
      return new HucConnection(huc);
    }
    catch (IOException e)
    {
    }
    return null;
  }

  @Override
  public void close()
  {
    // TODO Auto-generated method stub
  }
}

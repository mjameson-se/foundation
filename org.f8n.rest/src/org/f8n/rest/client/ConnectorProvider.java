package org.f8n.rest.client;

import java.io.Closeable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.f8n.rest.common.HttpRequest;

public interface ConnectorProvider extends Closeable
{
  SSLContext getSslContext();

  HostnameVerifier getHostnameVerifier();

  Connection invoke(HttpRequest request);

  @Override
  void close();
}

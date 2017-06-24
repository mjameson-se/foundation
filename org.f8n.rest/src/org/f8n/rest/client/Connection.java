package org.f8n.rest.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.collect.Multimap;

public interface Connection extends Closeable
{
  /**
   * @return output stream to send data in the request body. Will not be closed
   *         by the framework
   * @throws IOException
   */
  OutputStream requestOutputStream() throws IOException;

  /**
   * Called to finalize the request and receive the response.
   */
  void complete() throws IOException;

  /**
   * @return response headers, empty if none provided
   */
  Multimap<String, String> responseHeaders();

  int responseStatusCode() throws IOException;

  /**
   * @return null if no body included
   */
  InputStream responseBody() throws IOException;

  @Override
  void close() throws IOException;
}

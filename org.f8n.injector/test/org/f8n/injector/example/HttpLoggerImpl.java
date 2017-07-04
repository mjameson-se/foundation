package org.f8n.injector.example;

import java.util.Objects;

import org.f8n.injector.annotations.Inject;
import org.f8n.injector.example.api.HttpLogger;
import org.junit.Assert;
import org.slf4j.Logger;

public class HttpLoggerImpl implements HttpLogger
{
  private Logger logger;

  @Inject
  public HttpLoggerImpl(Logger logger)
  {
    this.logger = Objects.requireNonNull(logger);
    Assert.fail("This should never resolve due to lack of Logger service");
  }

  public void log(String message)
  {
    logger.info(message);
  }
}

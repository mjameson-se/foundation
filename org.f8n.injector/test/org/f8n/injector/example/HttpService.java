package org.f8n.injector.example;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.f8n.injector.annotations.Component;
import org.f8n.injector.annotations.Inject;
import org.f8n.injector.example.api.Database;
import org.f8n.injector.example.api.HttpLogger;
import org.f8n.injector.example.api.RestHandler;

@Component
public class HttpService
{
  private Set<RestHandler> handlers;
  private Optional<HttpLogger> logger;

  @Inject
  public HttpService(Set<RestHandler> handlers,
                     Optional<HttpLogger> logger,
                     Optional<Database> db)
  {
    this.handlers = handlers;
    this.logger = Objects.requireNonNull(logger);
    Objects.requireNonNull(db.orElse(null));
  }

  public Set<RestHandler> getHandlers()
  {
    return handlers;
  }

  public Optional<HttpLogger> getLogger()
  {
    return logger;
  }

}

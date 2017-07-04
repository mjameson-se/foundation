package org.f8n.injector.example;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;

import org.f8n.injector.annotations.Component;
import org.f8n.injector.annotations.Inject;
import org.f8n.injector.example.api.Database;
import org.f8n.injector.example.api.RestHandler;

@Component
public class ConfidentialInformationHandler implements RestHandler
{
  private String secret;
  private ApiUserHandler userHandler;

  @Inject
  public void bindDatabase(Database database)
  {
    secret = (String) database.read("secret");
  }

  @Inject
  public void bindUserHandler(ApiUserHandler userHandler)
  {
    this.userHandler = userHandler;
  }

  @GET
  public String getSecrets(@HeaderParam("user_id") String userId,
                           @HeaderParam("password") String password)
  {
    if (userHandler.authorize(userId, password))
      return secret;
    return UUID.randomUUID().toString();
  }
}

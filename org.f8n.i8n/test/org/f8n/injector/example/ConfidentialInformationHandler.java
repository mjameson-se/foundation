package org.f8n.injector.example;

import java.util.UUID;
import java.util.function.BiPredicate;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;

import org.f8n.i8n.a8n.Component;
import org.f8n.i8n.a8n.Inject;
import org.f8n.injector.example.api.Database;
import org.f8n.injector.example.api.RestHandler;

@Component
public class ConfidentialInformationHandler implements RestHandler
{
  private String secret;
  private BiPredicate<String, String> authorizationService;

  @Inject
  public void bindDatabase(Database database)
  {
    secret = (String) database.read("secret");
  }

  @Inject
  public void bindAuthorization(BiPredicate<String, String> userHandler)
  {
    this.authorizationService = userHandler;
  }

  @GET
  public String getSecrets(@HeaderParam("user_id") String userId,
                           @HeaderParam("password") String password)
  {
    if (authorizationService.test(userId, password))
      return secret;
    return UUID.randomUUID().toString();
  }
}
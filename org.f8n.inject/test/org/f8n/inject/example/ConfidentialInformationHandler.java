package org.f8n.inject.example;

import java.util.UUID;
import java.util.function.BiPredicate;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Inject;
import org.f8n.inject.example.api.Database;
import org.f8n.inject.example.api.RestHandler;

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

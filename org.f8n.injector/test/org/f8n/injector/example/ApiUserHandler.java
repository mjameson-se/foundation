package org.f8n.injector.example;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.f8n.injector.annotations.Component;
import org.f8n.injector.annotations.Inject;
import org.f8n.injector.example.api.Database;
import org.f8n.injector.example.api.RestHandler;

@Component
@Path("/users")
public class ApiUserHandler implements RestHandler
{
  private Database db;

  public static class User
  {
    String id;
    String password;

    public User(String id, String password)
    {
      this.id = id;
      this.password = password;
    }
  }

  @Inject
  public void bindDatabase(Database db)
  {
    this.db = db;
  }

  @GET
  @Path("{id}")
  public User getUser(@PathParam("id") String id)
  {
    return (User) db.read(id);
  }

  @POST
  public void createUser(User order)
  {
    db.write(order.id, order);
  }

  public boolean authorize(String userId, String password)
  {
    return Optional.ofNullable(db.read(userId)).map(u -> ((User) u).password.equals(password)).orElse(false);
  }
}

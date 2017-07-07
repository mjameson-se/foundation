package org.f8n.injector.example;

import java.util.Optional;
import java.util.function.BiPredicate;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.f8n.i8n.a8n.Component;
import org.f8n.i8n.a8n.Inject;
import org.f8n.injector.example.api.Database;
import org.f8n.injector.example.api.RestHandler;

@Component
@Path("/users")
public class ApiUserHandler implements RestHandler, BiPredicate<String, String>
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

  @Override
  public boolean test(String t, String u)
  {
    return authorize(t, u);
  }
}

package org.f8n.injector.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.f8n.injector.annotations.Activate;
import org.f8n.injector.annotations.Component;
import org.f8n.injector.example.api.Database;

@Component
public class DatabaseImpl implements Database
{
  private boolean activated;
  private Map<String, Object> database = new HashMap<>();

  @Activate
  public void activate()
  {
    this.activated = true;
    database.put("secret", UUID.randomUUID().toString());
  }

  public boolean isActive()
  {
    return activated;
  }

  public String secret()
  {
    return (String) database.get("secret");
  }

  @Override
  public Object read(String id)
  {
    return database.get(id);
  }

  @Override
  public void write(String id, Object obj)
  {
    database.put(id, obj);
  }
}

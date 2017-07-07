package org.f8n.injector.example.api;

public interface Database
{
  Object read(String id);

  void write(String id, Object obj);
}

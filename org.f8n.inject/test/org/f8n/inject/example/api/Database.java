package org.f8n.inject.example.api;

public interface Database
{
  Object read(String id);

  void write(String id, Object obj);
}

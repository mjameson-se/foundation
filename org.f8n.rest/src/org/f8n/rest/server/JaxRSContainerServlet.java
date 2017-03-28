package org.f8n.rest.server;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;

public class JaxRSContainerServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  private Set<Class<?>> classes;
  private Set<Object> singletons;
  private Map<String, Object> properties;

  public JaxRSContainerServlet(Application app)
  {
    this.classes = app.getClasses();
    this.singletons = app.getSingletons();
    this.properties = app.getProperties();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
  }
}

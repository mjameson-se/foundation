package org.f8n.rest.common;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

public class RuntimeDelegateImpl extends RuntimeDelegate
{
  @Override
  public <T> T createEndpoint(Application arg0, Class<T> arg1) throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> arg0) throws IllegalArgumentException
  {
    return null;
  }

  @Override
  public Builder createLinkBuilder()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseBuilder createResponseBuilder()
  {
    return null;
  }

  @Override
  public UriBuilder createUriBuilder()
  {
    return new UriBuilderImpl();
  }

  @Override
  public VariantListBuilder createVariantListBuilder()
  {
    // TODO Auto-generated method stub
    return null;
  }
}

/**
 * Copyright (c) 2017 Pelco. All rights reserved.
 *
 * This file contains trade secrets of Pelco.  No part may be reproduced or
 * transmitted in any form by any means or for any purpose without the express
 * written permission of Pelco.
 */

package org.f8n.inject.example;

import org.f8n.inject.annotate.Component;
import org.f8n.inject.annotate.Target;
import org.f8n.inject.example.api.RestHandler;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests {@link Target} annotation
 */
@Component
public class TargetTest
{
  private static final Logger LOG = LoggerFactory.getLogger(TargetTest.class);
  private RestHandler handler;

  public TargetTest(@Target("secret") RestHandler handler)
  {
    this.handler = handler;
  }

  public void test()
  {
    LOG.info("Handler is {}", handler);
    Assert.assertEquals(ConfidentialInformationHandler.class, handler.getClass());
  }
}

/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link WildflySwarmConfigurationBuilder}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildflySwarmConfigurationBuilderTest
{

   @Test
   public void testCreate()
   {
      WildflySwarmConfigurationBuilder builder = WildflySwarmConfigurationBuilder.create();
      Assert.assertNotNull(builder);
      builder.contextPath("/context").httpPort(80).portOffset(100);
      Assert.assertEquals("/context", builder.getContextPath());
      Assert.assertEquals(Integer.valueOf(80), builder.getHttpPort());
      Assert.assertEquals(Integer.valueOf(100), builder.getPortOffset());
   }

}

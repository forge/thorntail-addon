/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.fractionlist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class FractionListProviderTest
{

   static
   {
      System.setProperty("org.apache.maven.log_transfer", "true");
   }

   @Test
   public void setFractionListVersion() throws Exception
   {
      FractionListProvider provider = FractionListProvider.get();
      String version = provider.getWildflySwarmVersion();
      List<String> swarmVersions = provider.getSwarmVersions();
      assertThat(swarmVersions.isEmpty(), equalTo(false));

      provider.setFractionListVersion(swarmVersions.get(0), false);
      assertThat(provider.getWildflySwarmVersion(), equalTo(swarmVersions.get(0)));

      provider.setFractionListVersion(version, false);
      assertThat(provider.getWildflySwarmVersion(), equalTo(version));
   }

}

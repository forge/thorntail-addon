/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.fractionlist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.tools.FractionDescriptor;

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

      provider.setFractionListVersion("2016.12.1", false);
      assertThat(provider.getFractionList().getFractionDescriptors(), everyItem(instanceOf(FractionDescriptor.class)));

      assertThat(provider.getWildflySwarmVersion(), equalTo("2016.12.1"));

      provider.setFractionListVersion(version, false);
      assertThat(provider.getFractionList().getFractionDescriptors(), everyItem(instanceOf(FractionDescriptor.class)));
      assertThat(provider.getWildflySwarmVersion(), equalTo(version));
   }

}

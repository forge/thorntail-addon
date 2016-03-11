/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

import java.util.Set;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.javaee.jms.JMSFacet_2_0;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class SwarmTest
{
   private FacetFactory facetFactory;
   private Project project;

   @Before
   public void setUp()
   {
      AddonRegistry addonRegistry = Furnace.instance(getClass().getClassLoader()).getAddonRegistry();
      ProjectFactory projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
      facetFactory = addonRegistry.getServices(FacetFactory.class).get();
      project = projectFactory.createTempProject();
   }

   @Test
   public void testMessagingFraction()
   {
      facetFactory.install(project, JMSFacet_2_0.class);
      Set<FractionDescriptor> fractionDescriptors = Swarm.getFractionDescriptorsFor(project);
      Assert.assertThat(fractionDescriptors.size(), is(1));
      Assert.assertThat(fractionDescriptors,
               hasItem(Swarm.getFractionList().getFractionDescriptor(Swarm.DEFAULT_FRACTION_GROUPID, "messaging")));
   }

}

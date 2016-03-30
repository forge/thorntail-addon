/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm;

import java.util.Collection;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Swarm
{
   public static final String DEFAULT_FRACTION_GROUPID = "org.wildfly.swarm";

   public static FractionList getFractionList()
   {
      return FractionList.get();
   }

   public static Collection<FractionDescriptor> getFractionDescriptorsFor(Project project)
   {
      WildflySwarmFacet facet = project.getFacet(WildflySwarmFacet.class);
      return facet.getInstalledFractionList();
   }

}

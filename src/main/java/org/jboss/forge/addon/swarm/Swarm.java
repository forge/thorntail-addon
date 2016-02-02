/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.forge.addon.javaee.JavaEEFacet;
import org.jboss.forge.addon.javaee.cdi.CDIFacet;
import org.jboss.forge.addon.javaee.ejb.EJBFacet;
import org.jboss.forge.addon.javaee.faces.FacesFacet;
import org.jboss.forge.addon.javaee.jaxws.JAXWSFacet;
import org.jboss.forge.addon.javaee.jms.JMSFacet;
import org.jboss.forge.addon.javaee.jpa.JPAFacet;
import org.jboss.forge.addon.javaee.jta.JTAFacet;
import org.jboss.forge.addon.javaee.rest.RestFacet;
import org.jboss.forge.addon.javaee.servlet.ServletFacet;
import org.jboss.forge.addon.javaee.validation.ValidationFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.furnace.util.Sets;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Swarm
{
   private static final Function<JavaEEFacet, FractionDescriptor> specToFraction = (facet) -> {

      if (facet instanceof JPAFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "jpa");
      }
      else if (facet instanceof ValidationFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "bean-validation");
      }
      else if (facet instanceof FacesFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "jsf");
      }
      else if (facet instanceof EJBFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "ejb");
      }
      else if (facet instanceof CDIFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "weld");
      }
      else if (facet instanceof JMSFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "messaging");
      }
      else if (facet instanceof ServletFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "undertow");
      }
      else if (facet instanceof JAXWSFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "webservices");
      }
      else if (facet instanceof JTAFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "transactions");
      }
      else if (facet instanceof RestFacet)
      {
         return FractionListInstance.INSTANCE.getFractionDescriptor(FractionListInstance.DEFAULT_FRACTION_GROUPID,
                  "jaxrs");
      }
      else
      {
         return null;
      }
   };

   public static void updateFractions(Project project)
   {
      if (project.hasFacet(WildflySwarmFacet.class))
      {
         WildflySwarmFacet wildflySwarmFacet = project.getFacet(WildflySwarmFacet.class);
         Set<FractionDescriptor> fractionDescriptors = getFractionDescriptorsFor(project);
         wildflySwarmFacet.installFractions(fractionDescriptors);
      }
   }

   public static Set<FractionDescriptor> getFractionDescriptorsFor(Project project)
   {
      Set<JavaEEFacet> facets = Sets.toSet(project.getFacets(JavaEEFacet.class));
      return facets.stream().map(specToFraction).filter((desc) -> desc != null).collect(Collectors.toSet());
   }

}

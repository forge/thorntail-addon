/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.ui;

import java.io.PrintStream;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@FacetConstraint({ WildFlySwarmFacet.class, MavenFacet.class, PackagingFacet.class })
public class ListFractionsCommand extends AbstractWildFlySwarmCommand
{
   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("WildFly-Swarm: List Fractions")
               .description("List all the available fractions");
   }

   @Override
   public Result execute(UIExecutionContext executionContext) throws Exception
   {
      UIOutput output = executionContext.getUIContext().getProvider().getOutput();
      PrintStream out = output.out();
      for (FractionDescriptor fraction : FractionList.get().getFractionDescriptors())
      {
//         String msg = String.format("%s: %s (%s)", fraction.getArtifactId(), fraction.getName(),
//                  fraction.getDescription());
         String msg = fraction.toString();
         out.println(msg);
      }
      return Results.success();
   }

   @Override
   protected boolean isProjectRequired()
   {
      return false;
   }
}

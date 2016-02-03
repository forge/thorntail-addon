/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.project;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.forge.addon.facets.events.FacetInstalled;
import org.jboss.forge.addon.javaee.JavaEEFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.swarm.Swarm;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class SwarmFacetListener extends AbstractCommandExecutionListener implements EventListener
{
   private ThreadLocal<Boolean> doIt = ThreadLocal.withInitial(() -> false);

   @Override
   public void handleEvent(Object event, Annotation... qualifiers)
   {
      if (event instanceof FacetInstalled &&
               ((FacetInstalled) event).getFacet() instanceof JavaEEFacet)
      {
         doIt.set(true);
      }
   }

   @Override
   public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
   {
      try
      {
         // Fractions should only be updated if a facet is installed
         if (doIt.get())
         {
            ProjectFactory projectFactory = SimpleContainer
                     .getServices(getClass().getClassLoader(), ProjectFactory.class)
                     .get();
            Project project = Projects.getSelectedProject(projectFactory, context.getUIContext());
            Set<FractionDescriptor> newFractions = Swarm.updateFractions(project);
            UIOutput output = context.getUIContext().getProvider().getOutput();
            PrintStream out = output.out();
            for (FractionDescriptor fractionDescriptor : newFractions)
            {
               output.info(out, "Installed Wildfly Swarm Fraction: " + fractionDescriptor.getArtifactId());
            }
         }
      }
      finally
      {
         doIt.remove();
      }
   }
}

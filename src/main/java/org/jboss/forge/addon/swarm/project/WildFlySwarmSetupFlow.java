/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.project;

import org.jboss.forge.addon.swarm.ui.AddFractionCommand;
import org.jboss.forge.addon.swarm.ui.CreateRestEndpointStep;
import org.jboss.forge.addon.swarm.ui.SetupCommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildFlySwarmSetupFlow implements UIWizardStep
{

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception
   {
      NavigationResultBuilder builder = NavigationResultBuilder.create();
      builder.add(SetupCommand.class);
      builder.add(AddFractionCommand.class);
      builder.add(CreateRestEndpointStep.class);
      return builder.build();
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results.success();
   }
}

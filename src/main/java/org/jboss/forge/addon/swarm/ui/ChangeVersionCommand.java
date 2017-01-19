/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.ui;

import org.jboss.forge.addon.swarm.fractionlist.FractionListProvider;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ChangeVersionCommand extends AbstractWildFlySwarmCommand
{
   private UISelectOne<String> version;
   private UIInput<Boolean> permanent;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), ChangeVersionCommand.class)
               .name("WildFly Swarm: Change Version").description("Change the supported WildFly Version");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      InputComponentFactory inputFactory = builder.getInputComponentFactory();
      FractionListProvider fractionListProvider = FractionListProvider.get();
      version = inputFactory.createSelectOne("version", String.class)
               .setLabel("Version")
               .setRequired(true)
               .setDefaultValue(fractionListProvider.getWildflySwarmVersion())
               .setValueChoices(fractionListProvider.getSwarmVersions());
      permanent = inputFactory.createInput("permanent", Boolean.class)
               .setLabel("Permanent?")
               .setDescription("Store this value in the Forge configuration file and use it from now on");
      builder.add(version).add(permanent);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      FractionListProvider fractionListProvider = FractionListProvider.get();
      fractionListProvider.setFractionListVersion(version.getValue(), permanent.getValue());
      return Results.success("Changed to WildFly Swarm version: " + fractionListProvider.getWildflySwarmVersion());
   }

}

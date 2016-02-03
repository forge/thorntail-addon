package org.jboss.forge.addon.swarm.ui;

import java.io.PrintStream;
import java.util.Set;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.swarm.Swarm;
import org.jboss.forge.addon.swarm.config.WildflySwarmConfigurationBuilder;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 * The Wildfly-Swarm: Setup command
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class WildflySwarmSetupCommand extends AbstractWildflySwarmCommand
{
   private UIInput<Integer> httpPort;
   private UIInput<String> contextPath;
   private UIInput<Integer> portOffset;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      InputComponentFactory inputFactory = builder.getInputComponentFactory();
      httpPort = inputFactory.createInput("httpPort", Integer.class)
               .setLabel("HTTP Port").setDescription("HTTP Port Wildfly will listen to");
      contextPath = inputFactory.createInput("contextPath", String.class)
               .setLabel("Context Path").setDescription("The context path of the web application");
      portOffset = inputFactory.createInput("portOffset", Integer.class)
               .setLabel("HTTP Port Offset").setDescription("HTTP Port Offset");
      builder.add(httpPort).add(contextPath).add(portOffset);
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("Wildfly-Swarm: Setup")
               .description("Setup Wildfly Swarm in your web application");
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildflySwarmConfigurationBuilder builder = WildflySwarmConfigurationBuilder.create();
      builder.contextPath(contextPath.getValue()).httpPort(httpPort.getValue()).portOffset(portOffset.getValue());
      FacetFactory facetFactory = SimpleContainer.getServices(getClass().getClassLoader(), FacetFactory.class).get();
      WildflySwarmFacet facet = facetFactory.create(project, WildflySwarmFacet.class);
      facet.setConfiguration(builder);
      facetFactory.install(project, facet);
      // Update fractions
      Set<FractionDescriptor> newFractions = Swarm.updateFractions(project);
      UIOutput output = context.getUIContext().getProvider().getOutput();
      PrintStream out = output.out();
      for (FractionDescriptor fractionDescriptor : newFractions)
      {
         output.info(out, "Installed Wildfly Swarm Fraction: " + fractionDescriptor.getArtifactId());
      }
      return Results.success("Wildfly Swarm is now set up! Enjoy!");
   }
}
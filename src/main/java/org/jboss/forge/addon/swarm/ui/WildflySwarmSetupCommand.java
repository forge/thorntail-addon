package org.jboss.forge.addon.swarm.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.swarm.config.WildflySwarmConfigurationBuilder;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * The Wildfly-Swarm: Setup command
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class WildflySwarmSetupCommand extends AbstractProjectCommand
{

   @Inject
   private FacetFactory facetFactory;

   @Inject
   @WithAttributes(label = "Http Port", description = "HTTP Port Wildfly will listen to", defaultValue = "8080")
   private UIInput<Integer> httpPort;

   @Inject
   @WithAttributes(label = "Context Path", description = "The context path of the web application")
   private UIInput<String> contextPath;

   @Inject
   @WithAttributes(label = "Port Offset", description = "HTTP Port offset")
   private UIInput<Integer> portOffset;

   @Inject
   private ProjectFactory projectFactory;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(getClass()).name("Wildfly-Swarm: Setup").category(Categories.create("Wildfly-Swarm"));
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return projectFactory;
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildflySwarmConfigurationBuilder builder = WildflySwarmConfigurationBuilder.create();
      builder.contextPath(contextPath.getValue()).httpPort(httpPort.getValue()).portOffset(portOffset.getValue());
      WildflySwarmFacet facet = facetFactory.create(project, WildflySwarmFacet.class);
      facet.setConfiguration(builder);
      facetFactory.install(project, facet);
      return Results.success("Wildfly Swarm is now set up! Enjoy!");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(httpPort).add(contextPath).add(portOffset);
   }
}
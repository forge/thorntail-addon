package org.jboss.forge.addon.swarm.ui;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.swarm.config.WildFlySwarmConfiguration;
import org.jboss.forge.addon.swarm.config.WildFlySwarmConfigurationBuilder;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;

/**
 * The Wildfly-Swarm: Setup command
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class SetupCommand extends AbstractWildFlySwarmCommand
{
   private UIInput<Integer> httpPort;
   private UIInput<String> contextPath;
   private UIInput<Integer> portOffset;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      InputComponentFactory inputFactory = builder.getInputComponentFactory();

      httpPort = inputFactory.createInput("httpPort", Integer.class)
               .setLabel("HTTP Port").setDescription("HTTP Port Wildfly will listen to")
               .setDefaultValue(WildFlySwarmConfiguration.HTTP_PORT_DEFAULT_VALUE);

      contextPath = inputFactory.createInput("contextPath", String.class)
               .setLabel("Context Path").setDescription("The context path of the web application")
               .setDefaultValue(WildFlySwarmConfiguration.CONTEXT_PATH_DEFAULT_VALUE);

      portOffset = inputFactory.createInput("portOffset", Integer.class)
               .setLabel("HTTP Port Offset").setDescription("HTTP Port Offset")
               .setDefaultValue(WildFlySwarmConfiguration.PORT_OFFSET_DEFAULT_VALUE);

      Project project = Projects.getSelectedProject(getProjectFactory(), builder.getUIContext());
      if (project != null)
      {
         project.getFacetAsOptional(WildFlySwarmFacet.class)
                  .ifPresent((facet) -> {
                     WildFlySwarmConfiguration config = facet.getConfiguration();
                     httpPort.setDefaultValue(config.getHttpPort());
                     contextPath.setDefaultValue(config.getContextPath());
                     portOffset.setDefaultValue(config.getPortOffset());
                  });
      }
      builder.add(httpPort).add(contextPath).add(portOffset);
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("WildFly Swarm: Setup")
               .description("Setup WildFly Swarm in your web application")
               .category(Categories.create("WildFly Swarm"));
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildFlySwarmConfigurationBuilder builder = WildFlySwarmConfigurationBuilder.create();
      builder.contextPath(contextPath.getValue()).httpPort(httpPort.getValue()).portOffset(portOffset.getValue());
      FacetFactory facetFactory = SimpleContainer.getServices(getClass().getClassLoader(), FacetFactory.class).get();
      if (project.hasFacet(WildFlySwarmFacet.class))
      {
         WildFlySwarmFacet facet = project.getFacet(WildFlySwarmFacet.class);
         facet.setConfiguration(builder);
      }
      else
      {
         WildFlySwarmFacet facet = facetFactory.create(project, WildFlySwarmFacet.class);
         facet.setConfiguration(builder);
         facetFactory.install(project, facet);
      }
      return Results.success("WildFly Swarm is now set up! Enjoy!");
   }
}
package org.jboss.forge.addon.thorntail.ui;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.thorntail.config.ThorntailConfiguration;
import org.jboss.forge.addon.thorntail.config.ThorntailConfigurationBuilder;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
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
 * The Thorntail: Setup command
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class SetupCommand extends AbstractThorntailCommand
{
   private UIInput<Integer> httpPort;
   private UIInput<String> contextPath;
   private UIInput<Integer> portOffset;

   @Override
   public void initializeUI(UIBuilder builder)
   {
      InputComponentFactory inputFactory = builder.getInputComponentFactory();

      httpPort = inputFactory.createInput("httpPort", Integer.class)
               .setLabel("HTTP Port").setDescription("HTTP Port Thorntail will listen to")
               .setDefaultValue(ThorntailConfiguration.HTTP_PORT_DEFAULT_VALUE);

      contextPath = inputFactory.createInput("contextPath", String.class)
               .setLabel("Context Path").setDescription("The context path of the web application")
               .setDefaultValue(ThorntailConfiguration.CONTEXT_PATH_DEFAULT_VALUE);

      portOffset = inputFactory.createInput("portOffset", Integer.class)
               .setLabel("HTTP Port Offset").setDescription("HTTP Port Offset")
               .setDefaultValue(ThorntailConfiguration.PORT_OFFSET_DEFAULT_VALUE);

      Project project = Projects.getSelectedProject(getProjectFactory(), builder.getUIContext());
      if (project != null)
      {
         project.getFacetAsOptional(ThorntailFacet.class)
                  .ifPresent((facet) -> {
                      ThorntailConfiguration config = facet.getConfiguration();
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
      return Metadata.from(super.getMetadata(context), getClass()).name("Thorntail: Setup")
               .description("Setup Thorntail in your web application")
               .category(Categories.create("Thorntail"));
   }

   @Override
   public Result execute(UIExecutionContext context)
   {
      Project project = getSelectedProject(context);
      ThorntailConfigurationBuilder builder = ThorntailConfigurationBuilder.create();
      builder.contextPath(contextPath.getValue()).httpPort(httpPort.getValue()).portOffset(portOffset.getValue());
      FacetFactory facetFactory = SimpleContainer.getServices(getClass().getClassLoader(), FacetFactory.class).get();
      if (project.hasFacet(ThorntailFacet.class))
      {
         ThorntailFacet facet = project.getFacet(ThorntailFacet.class);
         facet.setConfiguration(builder);
      }
      else
      {
         ThorntailFacet facet = facetFactory.create(project, ThorntailFacet.class);
         facet.setConfiguration(builder);
         facetFactory.install(project, facet);
      }
      return Results.success("Thorntail is now set up! Enjoy!");
   }
}
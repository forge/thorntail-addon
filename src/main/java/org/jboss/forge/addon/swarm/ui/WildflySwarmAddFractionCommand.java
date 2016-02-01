package org.jboss.forge.addon.swarm.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.addon.ui.command.PrerequisiteCommandsProvider;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.util.Lists;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

@FacetConstraint(WildflySwarmFacet.class)
public class WildflySwarmAddFractionCommand extends AbstractWildflySwarmCommand implements PrerequisiteCommandsProvider
{
   private UISelectMany<FractionDescriptor> fractionElements;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("Wildfly-Swarm: Add Fraction")
               .description("Add one or more fractions. Installed fractions have been filtered out.");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      InputComponentFactory factory = builder.getInputComponentFactory();
      fractionElements = factory.createSelectMany("fractions", FractionDescriptor.class)
               .setRequired(true)
               .setLabel("Fraction List")
               .setDescription("Fraction list");

      Project project = getSelectedProject(builder);
      WildflySwarmFacet facet = getFacet(project);
      fractionElements.setValueChoices(facet.getFractionList());
      builder.add(fractionElements);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildflySwarmFacet facet = getFacet(project);
      List<FractionDescriptor> fractions = Lists.toList(fractionElements.getValue());
      facet.installFractions(fractions);
      List<String> artifactIds = fractions.stream().map((desc) -> desc.getArtifactId())
               .collect(Collectors.toList());
      return Results.success("Wildfly Swarm Fractions '"
               + artifactIds
               + "' were successfully added to the project descriptor");
   }

   private WildflySwarmFacet getFacet(Project project)
   {
      return project.getFacet(WildflySwarmFacet.class);
   }

   @Override
   public NavigationResult getPrerequisiteCommands(UIContext context)
   {
      NavigationResultBuilder builder = NavigationResultBuilder.create();
      Project project = getSelectedProject(context);
      if (project != null)
      {
         if (!project.hasFacet(WildflySwarmFacet.class))
         {
            builder.add(WildflySwarmSetupCommand.class);
         }
      }
      return builder.build();
   }

}

package org.jboss.forge.addon.swarm.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.util.Lists;
import org.wildfly.swarm.tools.FractionDescriptor;

@FacetConstraint(WildFlySwarmFacet.class)
public class AddFractionCommand extends AbstractWildFlySwarmCommand
{
   private UISelectMany<FractionDescriptor> fractionElements;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("WildFly-Swarm: Add Fraction")
               .description("Add one or more fractions. Installed fractions have been filtered out.");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      InputComponentFactory factory = builder.getInputComponentFactory();
      fractionElements = factory.createSelectMany("fractions", FractionDescriptor.class)
               .setLabel("Fraction List")
               .setDescription("Fraction list");

      UIContext uiContext = builder.getUIContext();
      if (uiContext.getProvider().isGUI())
      {
         fractionElements.setItemLabelConverter(FractionDescriptor::getName);
      }
      else
      {
         fractionElements.setItemLabelConverter(FractionDescriptor::getArtifactId);
      }
      Project project = Projects.getSelectedProject(getProjectFactory(), uiContext);
      final Collection<FractionDescriptor> fractions;
      if (project != null && project.hasFacet(WildFlySwarmFacet.class))
      {
         fractions = project.getFacet(WildFlySwarmFacet.class).getFractions();
      }
      else
      {
         fractions = WildFlySwarmFacet.getAllFractionDescriptors();
      }
      final List<FractionDescriptor> nonInternalfractions = fractions.stream()
               .filter(f -> !f.isInternal())
               .collect(Collectors.toList());
      fractionElements.setValueChoices(nonInternalfractions);

      builder.add(fractionElements);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildFlySwarmFacet facet = project.getFacet(WildFlySwarmFacet.class);
      if (fractionElements.hasValue())
      {
         List<FractionDescriptor> fractions = Lists.toList(fractionElements.getValue());
         facet.installFractions(fractions);
         List<String> artifactIds = fractions.stream().map(FractionDescriptor::getArtifactId)
                  .collect(Collectors.toList());
         return Results.success("Wildfly Swarm Fractions '"
                  + artifactIds
                  + "' were successfully added to the project descriptor");
      }
      return Results.success();
   }

   /**
    * @return the fractionElements
    */
   public UISelectMany<FractionDescriptor> getFractionElements()
   {
      return fractionElements;
   }
}

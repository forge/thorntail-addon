package org.jboss.forge.addon.swarm.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
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

@FacetConstraint(WildflySwarmFacet.class)
public class WildflySwarmAddFractionCommand extends AbstractWildflySwarmCommand
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
               .setDescription("Fraction list")
               .setItemLabelConverter(descriptor -> descriptor.artifactId());

      Project project = getSelectedProject(builder);
      WildflySwarmFacet facet = project.getFacet(WildflySwarmFacet.class);
      fractionElements.setValueChoices(facet.getFractionList());
      builder.add(fractionElements);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildflySwarmFacet facet = project.getFacet(WildflySwarmFacet.class);
      List<FractionDescriptor> fractions = Lists.toList(fractionElements.getValue());
      facet.installFractions(fractions);
      List<String> artifactIds = fractions.stream().map(descriptor -> descriptor.artifactId())
               .collect(Collectors.toList());
      return Results.success("Wildfly Swarm Fractions '"
               + artifactIds
               + "' were successfully added to the project descriptor");
   }
}

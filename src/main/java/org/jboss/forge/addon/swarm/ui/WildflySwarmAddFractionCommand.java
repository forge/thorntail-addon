package org.jboss.forge.addon.swarm.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.FacetNotFoundException;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class WildflySwarmAddFractionCommand extends AbstractProjectCommand {

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private InputComponentFactory factory;

   private UISelectMany<String> fractionElements;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(getClass()).name("Wildfly-Swarm: Add Fraction")
            .category(Categories.create("Wildfly-Swarm")).description("Add one or more fractions. Installed fractions have been filtered out.");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      fractionElements = factory.createSelectMany("fractions", String.class).setLabel("Fraction List")
            .setDescription("Fraction list");

      fractionElements.setValueChoices(getFacet(getSelectedProject(builder)).getFractionList());
      builder.add(fractionElements);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      getFacet(getSelectedProject(context)).installFractions(fractionElements.getValue());
      return Results.success("Command 'add fraction' successfully executed!");
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

   private WildflySwarmFacet getFacet(Project project)
   {
      WildflySwarmFacet facet;
      try {
         facet = project.getFacet(WildflySwarmFacet.class);
      } catch (FacetNotFoundException exception) {
         facet = facetFactory.create(project, WildflySwarmFacet.class);
      }
      if (!facet.isInstalled()) {
         facet.install();
      }
      return facet;
   }

}

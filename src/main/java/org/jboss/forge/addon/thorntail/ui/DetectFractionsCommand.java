/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.thorntail.ui;

import java.io.File;
import java.util.Collection;

import org.apache.maven.model.Build;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.progress.UIProgressMonitor;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.fractions.FractionUsageAnalyzer;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@FacetConstraint({ ThorntailFacet.class, MavenFacet.class, PackagingFacet.class })
public class DetectFractionsCommand extends AbstractThorntailCommand
{
   private UIInput<DirectoryResource> inputDir;
   private UIInput<Boolean> build;
   private UIInput<Boolean> depend;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("Thorntail: Detect Fractions")
               .description("Detect the needed fractions for the current project")
               .category(Categories.create("Thorntail"));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      InputComponentFactory factory = builder.getInputComponentFactory();
      Project project = getSelectedProject(builder);
      inputDir = factory.createInput("inputDir", DirectoryResource.class).setLabel("Input Directory")
               .setDescription("Directory containing the compiled project sources").setRequired(true)
               .setDefaultValue(getTargetDirectory(project));
      build = factory.createInput("build", Boolean.class).setLabel("Build Project?")
               .setDescription("Build project before attempting to auto-detect");
      depend = factory.createInput("depend", Boolean.class)
               .setLabel("Add Missing Fractions as Project Dependencies?")
               .setDescription("Add missing fractions as project dependencies");

      builder.add(inputDir).add(build).add(depend);
   }

   // TODO: Replace with JavaTargetFacet
   private DirectoryResource getTargetDirectory(Project project)
   {
      MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
      Build build = mavenFacet.getModel().getBuild();
      String targetFolderName;
      if (build != null && build.getOutputDirectory() != null)
      {
         targetFolderName = mavenFacet.resolveProperties(build.getOutputDirectory());
      }
      else
      {
         targetFolderName = "target" + File.separator + "classes";
      }
      DirectoryResource projectRoot = project.getRoot().reify(DirectoryResource.class);
      return projectRoot.getChildDirectory(targetFolderName);
   }

   @Override
   public Result execute(UIExecutionContext executionContext) throws Exception
   {
      UIProgressMonitor progressMonitor = executionContext.getProgressMonitor();
      UIOutput output = executionContext.getUIContext().getProvider().getOutput();
      FractionUsageAnalyzer analyzer = ThorntailFacet.getFractionUsageAnalyzer();
      DirectoryResource value = inputDir.getValue();
      analyzer.source(value.getUnderlyingResourceObject());
      Project project = getSelectedProject(executionContext);
      int total = 1;
      if (build.getValue())
         total++;
      if (depend.getValue())
         total++;
      progressMonitor.beginTask("Detecting fractions", total);
      if (build.getValue())
      {
         PackagingFacet packaging = project.getFacet(PackagingFacet.class);
         progressMonitor.setTaskName("Building the project...");
         FileResource<?> finalArtifact = packaging.createBuilder().build(output.out(), output.err())
                  .reify(FileResource.class);
         analyzer.source(finalArtifact.getUnderlyingResourceObject());
         progressMonitor.worked(1);
      }
      Collection<FractionDescriptor> detectedFractions = analyzer.detectNeededFractions();
      output.info(output.out(), "Detected fractions: " + detectedFractions);
      progressMonitor.worked(1);
      if (depend.getValue() && detectedFractions.size() > 0)
      {
         progressMonitor.setTaskName("Adding missing fractions as project dependencies...");
         ThorntailFacet facet = project.getFacet(ThorntailFacet.class);
         detectedFractions.removeAll(facet.getInstalledFractions());
         // detectedFractions.remove(fractionList.getFractionDescriptor(Swarm.DEFAULT_FRACTION_GROUPID, "container"));
         if (detectedFractions.isEmpty())
         {
            output.warn(output.out(), "Project already contains all the installed fractions. Doing nothing.");
         }
         else
         {
            output.info(output.out(), "Installing the following dependencies: " + detectedFractions);
            facet.installFractions(detectedFractions);
         }
         progressMonitor.worked(1);
      }
      progressMonitor.done();
      return Results.success();
   }

}

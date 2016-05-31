/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.ui;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.swarm.config.WildFlySwarmConfigurationBuilder;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@FacetConstraint(WildFlySwarmFacet.class)
public class CreateMainClassCommand extends AbstractJavaSourceCommand<JavaClassSource>
{
   @Override
   protected Class<JavaClassSource> getSourceType()
   {
      return JavaClassSource.class;
   }

   @Override
   protected String getType()
   {
      return "Main Class";
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(getClass())
               .category(Categories.create("WildFly-Swarm"))
               .name("WildFly-Swarm: New " + getType())
               .description("Creates a new " + getType() + " to run WildFly Swarm");
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return SimpleContainer.getServices(getClass().getClassLoader(), ProjectFactory.class).get();
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      super.initializeUI(builder);
      getNamed().setDefaultValue("Main");
   }

   @Override
   public JavaClassSource decorateSource(UIExecutionContext context, Project project, JavaClassSource source)
            throws Exception
   {
      source.addImport("org.wildfly.swarm.Swarm");
      MethodSource<JavaClassSource> method = source.addMethod()
               .setPublic()
               .setStatic(true)
               .setReturnTypeVoid()
               .setName("main")
               .addThrows(Exception.class);
      method.addParameter("String[]", "args");

      StringBuilder body = new StringBuilder();
      body.append("Swarm swarm = new Swarm();").append(System.lineSeparator());
      body.append("swarm.start();").append(System.lineSeparator());
      body.append("swarm.deploy();");
      method.setBody(body.toString());

      WildFlySwarmFacet facet = project.getFacet(WildFlySwarmFacet.class);
      WildFlySwarmConfigurationBuilder newConfig = WildFlySwarmConfigurationBuilder.create(facet.getConfiguration());
      newConfig.mainClass(source.getQualifiedName());
      facet.setConfiguration(newConfig);
      return source;
   }

}

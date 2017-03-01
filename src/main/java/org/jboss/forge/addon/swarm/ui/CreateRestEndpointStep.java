/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.ui;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.wildfly.swarm.fractions.FractionDescriptor;

/**
 * Creates a sample REST endpoint
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class CreateRestEndpointStep extends AbstractWildFlySwarmCommand implements UIWizardStep
{

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      WildFlySwarmFacet wildFlySwarm = project.getFacet(WildFlySwarmFacet.class);
      List<FractionDescriptor> installedFractions = wildFlySwarm.getInstalledFractions();
      if (enableJAXRS(installedFractions))
      {
         JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
         JavaClassSource restEndpoint = Roaster.create(JavaClassSource.class)
                  .setPackage(facet.getBasePackage() + ".rest")
                  .setName("HelloWorldEndpoint");
         if (hasCDI(installedFractions))
         {
            restEndpoint.addAnnotation(ApplicationScoped.class);
         }
         restEndpoint.addAnnotation(Path.class).setStringValue("/hello");
         MethodSource<JavaClassSource> method = restEndpoint.addMethod().setPublic().setReturnType(Response.class)
                  .setName("doGet")
                  .setBody("return Response.ok(\"Hello from WildFly Swarm!\").build();");
         method.addAnnotation(GET.class);
         method.addAnnotation(javax.ws.rs.Produces.class).setStringArrayValue(new String[] { MediaType.TEXT_PLAIN });
         facet.saveJavaSource(restEndpoint);
      }
      return Results.success();
   }

   private boolean enableJAXRS(List<FractionDescriptor> dependencies)
   {
      if (dependencies == null || dependencies.size() == 0)
      {
         return true;
      }
      return dependencies.stream()
               .anyMatch(d -> d.getArtifactId().contains("jaxrs") || d.getArtifactId().contains("microprofile"));
   }

   private boolean hasCDI(List<FractionDescriptor> dependencies)
   {
      if (dependencies == null || dependencies.size() == 0)
      {
         return true;
      }
      return dependencies.stream()
               .anyMatch(d -> d.getArtifactId().contains("cdi") || d.getArtifactId().contains("microprofile"));
   }

}

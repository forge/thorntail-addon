/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.thorntail.ui;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.thorntail.config.ThorntailConfiguration;
import org.jboss.forge.addon.thorntail.config.ThorntailConfigurationBuilder;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.wildfly.swarm.fractions.FractionDescriptor;

/**
 * Performs all necessary changes with the installed fractions
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class SetupFractionsStep extends AbstractThorntailCommand implements UIWizardStep
{

    @Override
    public Result execute(UIExecutionContext context)
    {
        Project project = getSelectedProject(context);
        ThorntailFacet thorntail = project.getFacet(ThorntailFacet.class);
        List<FractionDescriptor> installedFractions = thorntail.getInstalledFractions();
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
        if (hasTopologyJgroups(installedFractions))
        {
            ThorntailConfiguration config = thorntail.getConfiguration();
            Map<String, String> props = new TreeMap<>(config.getProperties());
            props.put("swarm.bind.address", "127.0.0.1");
            props.put("java.net.preferIPv4Stack", "true");
            props.put("jboss.node.name", "${project.artifactId}");
            thorntail.setConfiguration(ThorntailConfigurationBuilder.create((Configuration) config).properties(props));
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

    private boolean hasTopologyJgroups(List<FractionDescriptor> dependencies)
    {
        if (dependencies == null || dependencies.size() == 0)
        {
            return false;
        }
        return dependencies.stream()
                    .anyMatch(d -> d.getArtifactId().contains("topology-jgroups"));
    }

}

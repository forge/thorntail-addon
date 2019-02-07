/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.thorntail.ui;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
import org.jboss.forge.addon.ui.UIProvider;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.wildfly.swarm.fractions.FractionDescriptor;

import java.io.PrintStream;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@FacetConstraint({ThorntailFacet.class, MavenFacet.class, PackagingFacet.class})
public class ListFractionsCommand extends AbstractThorntailCommand {
    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), getClass()).name("Thorntail: List Fractions")
                .description("List all the available fractions")
                .category(Categories.create("Thorntail"));
    }

    @Override
    public Result execute(UIExecutionContext executionContext) {
        UIProvider provider = executionContext.getUIContext().getProvider();
        UIOutput output = provider.getOutput();
        PrintStream out = output.out();
        for (FractionDescriptor fraction : ThorntailFacet.getAllFractionDescriptors()) {
            if (!fraction.isInternal()) {
                String msg = String.format("%s: %s (%s)", fraction.getArtifactId(), fraction.getName(),
                        fraction.getDescription());
                out.println(msg);
            }
        }
        return Results.success();
    }

    @Override
    protected boolean isProjectRequired() {
        return false;
    }
}

package org.jboss.forge.addon.swarm.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * The Wildfly-Swarm: Setup command
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildflySwarmSetupCommand extends AbstractProjectCommand {

	@Inject
	private FacetFactory facetFactory;

	@Inject
	private ProjectFactory projectFactory;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(getClass()).name("Wildfly-Swarm: Setup");
	}

	@Override
	protected ProjectFactory getProjectFactory() {
		return projectFactory;
	}

	@Override
	protected boolean isProjectRequired() {
		return true;
	}

	// @Override
	// public boolean isEnabled(UIContext context) {
	// return super.isEnabled(context)
	// && !getSelectedProject(context).hasFacet(
	// WildflySwarmFacet.class);
	// }

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		Project project = getSelectedProject(context);
		facetFactory.install(project, WildflySwarmFacet.class);
		return Results.success("Wildfly Swarm is now set up! Enjoy!");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		// No inputs so far
	}
}
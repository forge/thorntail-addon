package org.jboss.forge.addon.swarm.facet.impl;

import java.util.Properties;

import org.apache.maven.model.Model;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;

public class WildflySwarmFacetImpl extends AbstractFacet<Project> implements
		WildflySwarmFacet {

	private static final String WILDFLY_SWARM_VERSION_PROPERTY = "version.wildfly-swarm";

	private static final Coordinate PLUGIN_COORDINATE = CoordinateBuilder
			.create().setGroupId("org.wildfly.swarm")
			.setArtifactId("wildfly-swarm-plugin")
			.setVersion("${version.wildfly-swarm}");

	@Override
	public boolean install() {
		addSwarmVersionProperty();
		addMavenPlugin();
		addDependencies();
		return isInstalled();
	}

	private void addDependencies() {
		DependencyBuilder jaxrsDependency = DependencyBuilder.create()
				.setGroupId("org.wildfly.swarm")
				.setArtifactId("wildfly-swarm-jaxrs")
				.setVersion("${version.wildfly-swarm}");
		DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
		facet.addDirectDependency(jaxrsDependency);

	}

	private void addMavenPlugin() {
		MavenPluginFacet pluginFacet = getFaceted().getFacet(
				MavenPluginFacet.class);
		MavenPluginBuilder plugin = MavenPluginBuilder
				.create()
				.setCoordinate(PLUGIN_COORDINATE)
				.addExecution(
						ExecutionBuilder.create().addGoal("package"));

		pluginFacet.addPlugin(plugin);
	}

	private void addSwarmVersionProperty() {
		MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
		Model pom = maven.getModel();
		Properties properties = pom.getProperties();
		// TODO: Fetch the latest version
		properties.setProperty(WILDFLY_SWARM_VERSION_PROPERTY, "1.0.0.Alpha3");
		maven.setModel(pom);
	}

	@Override
	public boolean isInstalled() {
		MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
		return facet.hasPlugin(PLUGIN_COORDINATE);
	}

}

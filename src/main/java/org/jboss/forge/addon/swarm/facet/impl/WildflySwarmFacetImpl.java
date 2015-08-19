package org.jboss.forge.addon.swarm.facet.impl;

import java.util.Properties;

import org.apache.maven.model.Model;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.swarm.config.WildflySwarmConfiguration;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.furnace.util.Strings;

/**
 * The implementation of the {@link WildflySwarmFacet}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class WildflySwarmFacetImpl extends AbstractFacet<Project>implements
         WildflySwarmFacet
{

   private WildflySwarmConfiguration configuration;

   private static final String WILDFLY_SWARM_VERSION_PROPERTY = "version.wildfly-swarm";

   public static final Coordinate PLUGIN_COORDINATE = CoordinateBuilder
            .create().setGroupId("org.wildfly.swarm")
            .setArtifactId("wildfly-swarm-plugin")
            .setVersion("${version.wildfly-swarm}");

   @Override
   public boolean install()
   {
      addSwarmVersionProperty();
      addMavenPlugin();
      addDependencies();
      return isInstalled();
   }

   private void addDependencies()
   {
      DependencyBuilder swarmDependency = DependencyBuilder.create()
               .setGroupId("org.wildfly.swarm")
               .setArtifactId("wildfly-swarm-jaxrs")
               .setVersion("${version.wildfly-swarm}");
      DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
      facet.addDirectDependency(swarmDependency);

   }

   private void addMavenPlugin()
   {
      MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);
      MavenPluginFacet pluginFacet = getFaceted().getFacet(MavenPluginFacet.class);

      MavenPluginBuilder plugin = MavenPluginBuilder
               .create()
               .setCoordinate(PLUGIN_COORDINATE)
               .addExecution(
                        ExecutionBuilder.create().addGoal("package"));

      // Plugin configuration
      ConfigurationBuilder builder = ConfigurationBuilder.create();

      if (!Strings.isNullOrEmpty(getConfiguration().getContextPath()))
      {
         builder.addConfigurationElement(ConfigurationElementBuilder.create().setName("contextPath")
                  .setText(getConfiguration().getContextPath()));
      }
      else
      {
         builder.addConfigurationElement(ConfigurationElementBuilder.create().setName("contextPath")
                  .setText(metadataFacet.getProjectName()));
      }
      if (getConfiguration().getHttpPort() != null && getConfiguration().getHttpPort() != 0)
      {
         builder.addConfigurationElement(ConfigurationElementBuilder.create().setName("httpPort")
                  .setText(getConfiguration().getHttpPort().toString()));
      }
      if (getConfiguration().getPortOffset() != null && getConfiguration().getPortOffset() != 0)
      {
         builder.addConfigurationElement(ConfigurationElementBuilder.create().setName("portOffset")
                  .setText(getConfiguration().getPortOffset().toString()));
      }
      plugin.setConfiguration(builder);

      pluginFacet.addPlugin(plugin);
   }

   private void addSwarmVersionProperty()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      Properties properties = pom.getProperties();
      // TODO: Fetch the latest version
      properties.setProperty(WILDFLY_SWARM_VERSION_PROPERTY, "1.0.0.Alpha4");
      maven.setModel(pom);
   }

   @Override
   public boolean isInstalled()
   {
      MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
      return facet.hasPlugin(PLUGIN_COORDINATE);
   }

   @Override
   public WildflySwarmConfiguration getConfiguration()
   {
      return configuration;
   }

   @Override
   public void setConfiguration(WildflySwarmConfiguration configuration)
   {
      this.configuration = configuration;
   }
}

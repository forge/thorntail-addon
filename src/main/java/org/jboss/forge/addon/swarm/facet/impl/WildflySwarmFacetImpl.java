package org.jboss.forge.addon.swarm.facet.impl;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.swarm.FractionListInstance;
import org.jboss.forge.addon.swarm.config.WildflySwarmConfiguration;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.forge.furnace.versions.Versions;
import org.wildfly.swarm.fractionlist.FractionDescriptor;
import org.wildfly.swarm.fractionlist.FractionList;

/**
 * The implementation of the {@link WildflySwarmFacet}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@FacetConstraint(MavenFacet.class)
public class WildflySwarmFacetImpl extends AbstractFacet<Project> implements
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
      return isInstalled();
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
      properties.setProperty(WILDFLY_SWARM_VERSION_PROPERTY, getWildflySwarmVersion());
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

   @Override
   public void installFractions(Iterable<FractionDescriptor> selectedFractions)
   {
      for (FractionDescriptor descriptor : selectedFractions)
      {
         Dependency dependency = DependencyBuilder.create()
                  .setGroupId(descriptor.getGroupId())
                  .setArtifactId(descriptor.getArtifactId())
                  .setVersion("${version.wildfly-swarm}");
         DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
         facet.addDirectDependency(dependency);
      }
   }

   @Override
   public List<FractionDescriptor> getFractionList()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      List<org.apache.maven.model.Dependency> dependencies = pom.getDependencies();
      return FractionListInstance.INSTANCE.getFractionDescriptors()
               .stream()
               .filter((descriptor) -> !alreadyInstalled(descriptor.getArtifactId(), dependencies))
               .collect(Collectors.toList());
   }

   @Override
   public List<FractionDescriptor> getInstalledFractionList()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      List<org.apache.maven.model.Dependency> dependencies = pom.getDependencies();
      return FractionListInstance.INSTANCE.getFractionDescriptors()
               .stream()
               .filter((descriptor) -> alreadyInstalled(descriptor.getArtifactId(), dependencies))
               .collect(Collectors.toList());
   }

   private String getWildflySwarmVersion()
   {
      return Versions.getImplementationVersionFor(FractionList.class).toString();
   }

   private boolean alreadyInstalled(String artifactId, List<org.apache.maven.model.Dependency> dependencies)
   {
      for (org.apache.maven.model.Dependency dep : dependencies)
      {
         if (dep.getArtifactId().equals(artifactId))
         {
            return true;
         }
      }
      return false;
   }

}

package org.jboss.forge.addon.swarm.facet;

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
import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.swarm.Swarm;
import org.jboss.forge.addon.swarm.config.WildflySwarmConfiguration;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.forge.furnace.versions.Versions;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.FractionDescriptor;

/**
 * The Wildfly-Swarm Facet
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@FacetConstraint(MavenFacet.class)
public class WildflySwarmFacet extends AbstractFacet<Project> implements ProjectFacet
{
   private WildflySwarmConfiguration configuration;

   private static final String WILDFLY_SWARM_VERSION_PROPERTY = "version.wildfly-swarm";

   public static final Coordinate PLUGIN_COORDINATE = CoordinateBuilder
            .create().setGroupId("org.wildfly.swarm")
            .setArtifactId("wildfly-swarm-plugin")
            .setVersion("${version.wildfly-swarm}");

   public static final Dependency BOM_DEPENDENCY = DependencyBuilder
            .create().setGroupId("org.wildfly.swarm")
            .setArtifactId("bom")
            .setVersion("${version.wildfly-swarm}")
            .setPackaging("pom")
            .setScopeType("import");

   @Override
   public boolean install()
   {
      addSwarmVersionProperty();
      addSwarmBOM();
      addMavenPlugin();
      return isInstalled();
   }

   private void addSwarmBOM()
   {
      DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      dependencyFacet.addDirectManagedDependency(BOM_DEPENDENCY);
   }

   private void addMavenPlugin()
   {
      MavenPluginFacet pluginFacet = getFaceted().getFacet(MavenPluginFacet.class);

      MavenPluginBuilder plugin = MavenPluginBuilder
               .create()
               .setCoordinate(PLUGIN_COORDINATE)
               .addExecution(
                        ExecutionBuilder.create().addGoal("package"));

      // Plugin configuration
      ConfigurationElementBuilder properties = ConfigurationElementBuilder.create().setName("properties");
      WildflySwarmConfiguration swarmConfig = getConfiguration();
      if (!Strings.isNullOrEmpty(swarmConfig.getContextPath()))
      {
         properties.addChild("swarm.context.path").setText(swarmConfig.getContextPath());
      }
      if (swarmConfig.getHttpPort() != null && swarmConfig.getHttpPort() != 0)
      {
         properties.addChild("swarm.http.port").setText(swarmConfig.getHttpPort().toString());
      }
      if (swarmConfig.getPortOffset() != null && swarmConfig.getPortOffset() != 0)
      {
         properties.addChild("swarm.port.offset").setText(swarmConfig.getPortOffset().toString());
      }
      Configuration builder = ConfigurationBuilder.create().addConfigurationElement(properties);
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

   public WildflySwarmConfiguration getConfiguration()
   {
      return configuration;
   }

   public void setConfiguration(WildflySwarmConfiguration configuration)
   {
      this.configuration = configuration;
   }

   public void installFractions(Iterable<FractionDescriptor> selectedFractions)
   {
      addSwarmBOM();
      for (FractionDescriptor descriptor : selectedFractions)
      {
         Dependency dependency = DependencyBuilder.create()
                  .setGroupId(descriptor.groupId())
                  .setArtifactId(descriptor.artifactId());
         DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
         facet.addDirectDependency(dependency);
      }
   }

   public List<FractionDescriptor> getFractionList()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      List<org.apache.maven.model.Dependency> dependencies = pom.getDependencies();
      return Swarm.getFractionList().getFractionDescriptors()
               .stream()
               .filter((descriptor) -> !alreadyInstalled(descriptor.artifactId(), dependencies))
               .sorted((o1, o2) -> o1.artifactId().compareTo(o2.artifactId()))
               .collect(Collectors.toList());
   }

   public List<FractionDescriptor> getInstalledFractionList()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      List<org.apache.maven.model.Dependency> dependencies = pom.getDependencies();
      return Swarm.getFractionList().getFractionDescriptors()
               .stream()
               .filter((descriptor) -> alreadyInstalled(descriptor.artifactId(), dependencies))
               .collect(Collectors.toList());
   }

   private String getWildflySwarmVersion()
   {
      return Versions.getImplementationVersionFor(FractionList.class).toString();
   }

   private boolean alreadyInstalled(String artifactId, List<org.apache.maven.model.Dependency> dependencies)
   {
      return dependencies.stream().anyMatch((dep) -> dep.getArtifactId().equals(artifactId));
   }
}

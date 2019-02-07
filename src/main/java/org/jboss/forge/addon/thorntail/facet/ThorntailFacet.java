package org.jboss.forge.addon.thorntail.facet;

import java.util.Collection;
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
import org.jboss.forge.addon.javaee.facets.JavaEE7Facet;
import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPlugin;
import org.jboss.forge.addon.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

import org.jboss.forge.addon.thorntail.config.ThorntailConfiguration;
import org.jboss.forge.addon.thorntail.config.ThorntailConfigurationBuilder;
import org.jboss.forge.furnace.versions.Versions;
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.fractions.FractionList;
import org.wildfly.swarm.fractions.FractionUsageAnalyzer;

/**
 * The Thorntail Facet
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@FacetConstraint(MavenFacet.class)
@FacetConstraint(JavaEE7Facet.class)
public class ThorntailFacet extends AbstractFacet<Project> implements ProjectFacet
{
   private ThorntailConfiguration configuration = ThorntailConfigurationBuilder.create();

   public static final String DEFAULT_FRACTION_GROUPID = "io.thorntail";

   public static final Coordinate PLUGIN_COORDINATE = CoordinateBuilder
            .create().setGroupId("io.thorntail")
            .setArtifactId("thorntail-maven-plugin")
            .setVersion("${version.thorntail}");

   public static final Dependency BOM_DEPENDENCY = DependencyBuilder
            .create().setGroupId("io.thorntail")
            .setArtifactId("bom-all")
            .setVersion("${version.thorntail}")
            .setPackaging("pom")
            .setScopeType("import");

   private static final String THORNTAIL_VERSION_PROPERTY = "version.thorntail";

   @Override
   public boolean install()
   {
      addSwarmVersionProperty();
      addSwarmBOM();
      addMavenPlugin();
      return isInstalled();
   }

   @Override
   public boolean isInstalled()
   {
      MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
      return facet.hasPlugin(PLUGIN_COORDINATE);
   }

   public ThorntailConfiguration getConfiguration()
   {
      if (configuration == null)
      {
         MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
         MavenPlugin plugin = facet.getPlugin(PLUGIN_COORDINATE);
         configuration = ThorntailConfigurationBuilder.create(plugin.getConfig());
      }
      return configuration;
   }

   public ThorntailFacet setConfiguration(ThorntailConfiguration configuration)
   {
      this.configuration = configuration;
      if (isInstalled())
      {
         updatePluginConfiguration(configuration);
      }
      return this;
   }

   public void installFractions(Iterable<FractionDescriptor> selectedFractions)
   {
      DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
      addSwarmBOM();
      for (FractionDescriptor descriptor : selectedFractions)
      {
         Dependency dependency = DependencyBuilder.create()
                  .setGroupId(descriptor.getGroupId())
                  .setArtifactId(descriptor.getArtifactId());
         if (!facet.hasEffectiveDependency(dependency))
         {
            facet.addDirectDependency(dependency);
         }
      }
   }

   public List<FractionDescriptor> getFractions()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      List<org.apache.maven.model.Dependency> dependencies = pom.getDependencies();
      return FractionList.get().getFractionDescriptors()
               .stream()
               .filter(d -> !d.isInternal() && !alreadyInstalled(d.getArtifactId(), dependencies))
               .sorted((o1, o2) -> o1.getArtifactId().compareTo(o2.getArtifactId()))
               .collect(Collectors.toList());
   }

   public List<FractionDescriptor> getInstalledFractions()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      List<org.apache.maven.model.Dependency> dependencies = pom.getDependencies();
      return FractionList.get().getFractionDescriptors()
               .stream()
               .filter(d -> alreadyInstalled(d.getArtifactId(), dependencies))
               .collect(Collectors.toList());
   }

   public static Collection<FractionDescriptor> getAllFractionDescriptors()
   {
      return FractionList.get().getFractionDescriptors();
   }

   public static FractionUsageAnalyzer getFractionUsageAnalyzer()
   {
      return new FractionUsageAnalyzer(FractionList.get());
   }

   private void addSwarmVersionProperty()
   {
      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      Properties properties = pom.getProperties();
      properties.setProperty(THORNTAIL_VERSION_PROPERTY, getWildflySwarmVersion());
      maven.setModel(pom);
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
      Configuration builder = ConfigurationBuilder.create();
      getConfiguration().apply(builder);
      if (builder.hasConfigurationElements())
      {
         plugin.setConfiguration(builder);
      }
      pluginFacet.addPlugin(plugin);
   }

   private void updatePluginConfiguration(ThorntailConfiguration configuration)
   {
      MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
      MavenPlugin plugin = facet.getPlugin(PLUGIN_COORDINATE);

      MavenPluginAdapter adapter = new MavenPluginAdapter(plugin);
      Configuration config = adapter.getConfig();
      configuration.apply(config);
      adapter.setConfig(config);
      facet.updatePlugin(adapter);
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

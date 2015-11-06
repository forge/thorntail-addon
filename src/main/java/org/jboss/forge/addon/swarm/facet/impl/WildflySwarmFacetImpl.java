package org.jboss.forge.addon.swarm.facet.impl;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyException;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
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
   @Inject
   private DependencyResolver dependencyResolver;

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
      properties.setProperty(WILDFLY_SWARM_VERSION_PROPERTY, "1.0.0.Alpha6");
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
   public void installFractions(Iterable<String> selectedFractions)
   {
      Iterator<String> iterator = selectedFractions.iterator();
      while (iterator.hasNext()) {
         String[] parts = iterator.next().split(":");
         Dependency dependency = DependencyBuilder.create(parts[0]).setArtifactId(parts[1])
               .setVersion("${version.wildfly-swarm}");
         DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
         facet.addDirectDependency(dependency);
      }
   }
   
   @Override
   public List<String> getFractionList()
   {
      Dependency dependency = dependencyResolver.resolveArtifact(DependencyQueryBuilder.create(
            CoordinateBuilder.create().setGroupId("org.wildfly.swarm").setArtifactId("wildfly-swarm-fraction-list")
                  .setVersion("1.0.0.Alpha6-SNAPSHOT").setPackaging("txt")));

      MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
      Model pom = maven.getModel();
      Scanner s;
      List<String> list = new ArrayList<String>();
      try {
         s = new Scanner(dependency.getArtifact().getUnderlyingResourceObject());
         while (s.hasNextLine()) {
            String currentFraction = s.nextLine();
            if (!alreadyInstalled(currentFraction.split(":")[1], pom.getDependencies())) {
               list.add(currentFraction);
            }

         }
         s.close();
      } catch (FileNotFoundException | DependencyException e) {
         // TODO do some proper error handling
         e.printStackTrace();
      }
      return list;
   }

   private boolean alreadyInstalled(String artifactId, List<org.apache.maven.model.Dependency> dependencies)
   {
      Iterator<org.apache.maven.model.Dependency> iterator = dependencies.iterator();
      while (iterator.hasNext()) {
         if (iterator.next().getArtifactId().equals(artifactId)) {
            return true;
         }
      }
      return false;
   }

}

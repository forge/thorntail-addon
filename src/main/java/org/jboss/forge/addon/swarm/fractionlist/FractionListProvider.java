/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.fractionlist;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.configuration.ConfigurationFactory;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyException;
import org.jboss.forge.addon.dependencies.DependencyQuery;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.furnace.container.simple.AbstractEventListener;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.versions.Versions;
import org.wildfly.swarm.tools.FractionList;

/**
 * Holds a reference to a {@link FractionList} instance
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FractionListProvider extends AbstractEventListener
{
   private static final String SWARM_VERSION_PROPERTY = "swarm.version";
   private static final Coordinate FRACTION_LIST_COORDINATE = CoordinateBuilder.create().setGroupId("org.wildfly.swarm")
            .setArtifactId("fraction-list");

   private FractionList fractionList;

   public static FractionListProvider get()
   {
      return SimpleContainer
               .getServices(WildFlySwarmFacet.class.getClassLoader(), FractionListProvider.class).get();
   }

   @Override
   protected void handleThisPostStartup()
   {
      Configuration userConfiguration = getConfiguration();
      String swarmVersion = userConfiguration.getString("swarm.version");
      if (swarmVersion != null)
      {
         setFractionListVersion(swarmVersion, false);
      }
   }

   @Override
   protected void handleThisPreShutdown()
   {
      if (fractionList instanceof DynamicFractionList)
      {
         try
         {
            ((DynamicFractionList) fractionList).close();
         }
         catch (IOException ignore)
         {
            // Ignore this exception
         }
      }
   }

   public void setFractionListVersion(String version, boolean permanent)
   {
      if (this.fractionList instanceof DynamicFractionList)
      {
         try
         {
            ((DynamicFractionList) this.fractionList).close();
         }
         catch (IOException ignored)
         {
         }
      }
      if (permanent)
      {
         getConfiguration().setProperty(SWARM_VERSION_PROPERTY, version);
      }
      DependencyResolver resolver = getDependencyResolver();
      DependencyQuery query = DependencyQueryBuilder
               .create(CoordinateBuilder.create(FRACTION_LIST_COORDINATE).setVersion(version));
      Dependency artifact = resolver.resolveArtifact(query);
      Set<Dependency> dependencies = resolver.resolveDependencies(query);
      List<URL> urls = new ArrayList<>();
      try
      {
         urls.add(toURL(artifact));
         dependencies.stream()
                  .map(this::toURL)
                  .filter(Objects::nonNull)
                  .forEach(urls::add);
         this.fractionList = new DynamicFractionList(version, urls.toArray(new URL[urls.size()]));
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private URL toURL(Dependency dep)
   {
      try
      {
         return dep.getArtifact().getUnderlyingResourceObject().toURI().toURL();
      }
      catch (MalformedURLException | DependencyException e)
      {
         return null;
      }
   }

   public List<String> getSwarmVersions()
   {
      DependencyResolver resolver = getDependencyResolver();
      return resolver.resolveVersions(DependencyQueryBuilder.create(FRACTION_LIST_COORDINATE)).stream()
               .map(Coordinate::getVersion).collect(Collectors.toList());

   }

   public FractionList getFractionList()
   {
      if (this.fractionList == null)
      {
         this.fractionList = org.wildfly.swarm.fractionlist.FractionList.get();
      }
      return this.fractionList;
   }

   public String getWildflySwarmVersion()
   {
      if (this.fractionList instanceof DynamicFractionList)
      {
         return ((DynamicFractionList) this.fractionList).getWildFlySwarmVersion();
      }
      return Versions.getImplementationVersionFor(getFractionList().getClass()).toString();
   }

   private Configuration getConfiguration()
   {
      ConfigurationFactory configFactory = SimpleContainer
               .getServices(getClass().getClassLoader(), ConfigurationFactory.class).get();
      Configuration userConfiguration = configFactory.getUserConfiguration();
      return userConfiguration;
   }

   private DependencyResolver getDependencyResolver()
   {
      return SimpleContainer.getServices(getClass().getClassLoader(), DependencyResolver.class)
               .get();
   }
}
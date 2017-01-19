/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.fractionlist;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;

import org.jboss.forge.furnace.proxy.ClassLoaderAdapterBuilder;
import org.wildfly.swarm.tools.FractionDescriptor;
import org.wildfly.swarm.tools.FractionList;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class DynamicFractionList implements FractionList
{
   private final URLClassLoader urlClassLoader;
   private final FractionList fractionList;
   private final String version;

   public DynamicFractionList(String version, URL... jars) throws Exception
   {
      this.version = version;
      this.urlClassLoader = new URLClassLoader(jars, null);
      Class<?> targetFractionListClass = this.urlClassLoader.loadClass("org.wildfly.swarm.fractionlist.FractionList");
      Object targetFractionList = targetFractionListClass.getMethod("get").invoke(null);

      // That won't work for FractionDescriptor class, since it is not proxyable
      this.fractionList = (FractionList) ClassLoaderAdapterBuilder
               .callingLoader(getClass().getClassLoader())
               .delegateLoader(this.urlClassLoader)
               .enhance(targetFractionList, FractionList.class);
   }

   @Override
   public Collection<FractionDescriptor> getFractionDescriptors()
   {
      return fractionList.getFractionDescriptors();
   }

   @Override
   public FractionDescriptor getFractionDescriptor(String groupId, String artifactId)
   {
      return fractionList.getFractionDescriptor(groupId, artifactId);
   }

   @Override
   public Map<String, FractionDescriptor> getPackageSpecs()
   {
      return fractionList.getPackageSpecs();
   }

   public String getWildFlySwarmVersion()
   {
      return this.version;
   }

   public void close() throws IOException
   {
      if (this.urlClassLoader != null)
      {
         this.urlClassLoader.close();
      }
   }
}

/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.facet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.wildfly.swarm.tools.FractionDescriptor;
import org.wildfly.swarm.tools.FractionList;
import org.wildfly.swarm.tools.PropertiesUtil;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/**
 * Temporary class until the next Swarm release is performed. Fixes SWARM-456
 * 
 * TODO: Remove when upgrade and replace by {@link org.wildfly.swarm.fractionlist.FractionList#get()}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class TempFractionList implements FractionList
{
   private final Map<String, FractionDescriptor> descriptors = new TreeMap<>();

   private static final AtomicReference<FractionList> INSTANCE = new AtomicReference<>();

   public static FractionList get()
   {
      return INSTANCE.updateAndGet(old -> old != null ? old : new TempFractionList());
   }

   private TempFractionList()
   {
      try (InputStreamReader reader = new InputStreamReader(
               getClass().getClassLoader().getResourceAsStream("fraction-list.json")))
      {
         Json.parse(reader).asArray().forEach(entry -> {
            JsonObject fraction = entry.asObject();
            String groupId = fraction.getString("groupId", null);
            String artifactId = fraction.getString("artifactId", null);
            String version = fraction.getString("version", null);
            String name = fraction.getString("name", null);
            String description = fraction.getString("description", null);
            String tags = fraction.getString("tags", null);
            boolean internal = fraction.getBoolean("internal", false);
            FractionDescriptor fd = new FractionDescriptor(groupId, artifactId, version, name, description, tags,
                     internal);
            descriptors.put(groupId + ":" + artifactId, fd);
         });
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      // Set up dependencies
      try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(getClass().getClassLoader().getResourceAsStream("fraction-list.txt"))))
      {

         String line = null;

         while ((line = reader.readLine()) != null)
         {
            line = line.trim();
            if (line.isEmpty())
            {
               continue;
            }

            String[] sides = line.split("=");

            String lhs = sides[0].trim();
            String lhsKey = toKey(lhs);
            FractionDescriptor desc = this.descriptors.get(lhsKey);
            if (desc == null)
            {
               String[] gavParts = lhs.split(":");
               desc = new FractionDescriptor(gavParts[0], gavParts[1], gavParts[2]);
               this.descriptors.put(lhsKey, desc);
            }

            if (sides.length > 1)
            {
               String rhs = sides[1].trim();
               String[] deps = rhs.split(",");

               for (String dep : deps)
               {
                  dep = dep.trim();
                  if (dep.isEmpty())
                  {
                     continue;
                  }
                  String depKey = toKey(dep);
                  FractionDescriptor depDesc = this.descriptors.get(depKey);
                  if (depDesc == null)
                  {
                     String[] gavParts = dep.split(":");
                     depDesc = new FractionDescriptor(gavParts[0], gavParts[1], gavParts[2]);
                     this.descriptors.put(depKey, depDesc);
                  }
                  desc.addDependency(depDesc);
               }
            }
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @param a groupId:artifactId:version string
    * @return the groupId:artifactId
    */
   private String toKey(String gav)
   {
      return gav.substring(0, gav.lastIndexOf(':'));
   }

   @Override
   public Collection<FractionDescriptor> getFractionDescriptors()
   {
      return Collections.unmodifiableCollection(this.descriptors.values());
   }

   @Override
   public FractionDescriptor getFractionDescriptor(final String groupId, final String artifactId)
   {
      return this.descriptors.get(groupId + ":" + artifactId);
   }

   @Override
   public Map<String, FractionDescriptor> getPackageSpecs()
   {
      final Map<String, String> packageSpecs = loadPackageSpecs();

      return this.descriptors.values().stream()
               .filter(fd -> packageSpecs.containsKey(fd.artifactId()))
               .collect(Collectors.toMap(fd -> packageSpecs.get(fd.artifactId()),
                        fd -> fd));
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private static Map<String, String> loadPackageSpecs()
   {
      try
      {
         final InputStream in = FractionList.class.getClassLoader()
                  .getResourceAsStream("org/wildfly/swarm/fractionlist/fraction-packages.properties");

         return new HashMap<>((Map) PropertiesUtil.loadProperties(in));
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to load fraction-packages.properties", e);
      }

   }
}

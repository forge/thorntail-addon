/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.convert;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.swarm.FractionListInstance;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FractionDescriptorConverter implements Converter<String, FractionDescriptor>
{
   @Override
   public FractionDescriptor convert(String coordinate)
   {
      CoordinateBuilder c = CoordinateBuilder.create(coordinate);
      return FractionListInstance.INSTANCE.getFractionDescriptor(c.getGroupId(), c.getArtifactId());
   }
}

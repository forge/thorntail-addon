/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.convert;

import org.jboss.forge.addon.convert.Converter;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FractionDescriptorToStringConverter implements Converter<FractionDescriptor, String>
{
   @Override
   public String convert(FractionDescriptor source)
   {
      return source.getGroupId() + ":" + source.getArtifactId();
   }
}

/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.convert;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.convert.ConverterGenerator;
import org.wildfly.swarm.fractionlist.FractionDescriptor;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FractionDescriptorToStringConverterGenerator implements ConverterGenerator
{
   @Override
   public Converter<?, ?> generateConverter(Class<?> source, Class<?> target)
   {
      return new FractionDescriptorToStringConverter();
   }

   @Override
   public Class<? extends Converter<?, ?>> getConverterType()
   {
      return FractionDescriptorToStringConverter.class;
   }

   @Override
   public boolean handles(Class<?> source, Class<?> target)
   {
      return target == String.class && source == FractionDescriptor.class;
   }
}

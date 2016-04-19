/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.furnace.util.Strings;

/**
 * The configuration for the Wildfly Swarm plugin
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface WildFlySwarmConfiguration
{
   public static final String HTTP_PORT_PROPERTY = "swarm.http.port";
   public static final Integer HTTP_PORT_DEFAULT_VALUE = 8080;

   public static final String CONTEXT_PATH_PROPERTY = "swarm.context.path";
   public static final String CONTEXT_PATH_DEFAULT_VALUE = "";

   public static final String PORT_OFFSET_PROPERTY = "swarm.port.offset";
   public static final Integer PORT_OFFSET_DEFAULT_VALUE = 0;

   /**
    * @return the HTTP port for this microservice
    */
   Integer getHttpPort();

   /**
    * @return the context path for this microservice
    */
   String getContextPath();

   /**
    * @return the port offset for this microservice
    */
   Integer getPortOffset();

   /**
    * @param properties
    */
   public default ConfigurationElementBuilder toConfigurationElementBuilder()
   {
      ConfigurationElementBuilder properties = ConfigurationElementBuilder.create().setName("properties");
      if (!Strings.isNullOrEmpty(getContextPath()) && !"/".equals(getContextPath())
               && !WildFlySwarmConfiguration.CONTEXT_PATH_DEFAULT_VALUE.equals(getContextPath()))
      {
         properties.addChild(WildFlySwarmConfiguration.CONTEXT_PATH_PROPERTY).setText(getContextPath());
      }
      if (getHttpPort() != null && getHttpPort() != 0
               && getHttpPort() != WildFlySwarmConfiguration.HTTP_PORT_DEFAULT_VALUE)
      {
         properties.addChild(WildFlySwarmConfiguration.HTTP_PORT_PROPERTY)
                  .setText(getHttpPort().toString());
      }
      if (getPortOffset() != null
               && getPortOffset() != WildFlySwarmConfiguration.PORT_OFFSET_DEFAULT_VALUE)
      {
         properties.addChild(WildFlySwarmConfiguration.PORT_OFFSET_PROPERTY)
                  .setText(getPortOffset().toString());
      }
      return properties;
   }

}

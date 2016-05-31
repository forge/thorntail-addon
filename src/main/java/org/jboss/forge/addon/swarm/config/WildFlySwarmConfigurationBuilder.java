/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementNotFoundException;
import org.jboss.forge.furnace.util.Strings;

/**
 * A builder for {@link WildFlySwarmConfiguration}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildFlySwarmConfigurationBuilder implements WildFlySwarmConfiguration
{
   private Integer httpPort = WildFlySwarmConfigurationBuilder.HTTP_PORT_DEFAULT_VALUE;
   private Integer portOffset = WildFlySwarmConfiguration.PORT_OFFSET_DEFAULT_VALUE;
   private String contextPath = WildFlySwarmConfiguration.CONTEXT_PATH_DEFAULT_VALUE;
   private String mainClass = WildFlySwarmConfiguration.MAIN_CLASS_DEFAULT_VALUE;

   public static WildFlySwarmConfigurationBuilder create()
   {
      return new WildFlySwarmConfigurationBuilder();
   }

   public static WildFlySwarmConfigurationBuilder create(WildFlySwarmConfiguration config)
   {
      WildFlySwarmConfigurationBuilder builder = new WildFlySwarmConfigurationBuilder();
      builder.contextPath(config.getContextPath()).httpPort(config.getHttpPort()).portOffset(config.getPortOffset())
               .mainClass(config.getMainClass());
      return builder;
   }

   /**
    * Create a {@link WildFlySwarmConfigurationBuilder} from a {@link Configuration} object
    */
   public static WildFlySwarmConfigurationBuilder create(Configuration config)
   {
      WildFlySwarmConfigurationBuilder builder = new WildFlySwarmConfigurationBuilder();
      ConfigurationElement mainClassElem = null;
      try
      {
         mainClassElem = config.getConfigurationElement(MAIN_CLASS_CONFIGURATION_ELEMENT);
      }
      catch (RuntimeException e)
      {
         // Do nothing
      }
      if (mainClassElem != null)
      {
         builder.mainClass(mainClassElem.getText());
      }
      ConfigurationElement propertiesElem = null;
      try
      {
         propertiesElem = config.getConfigurationElement("properties");
      }
      catch (RuntimeException e)
      {
         // Do nothing
      }
      if (propertiesElem != null)
      {
         try
         {
            builder.contextPath(propertiesElem.getChildByName(CONTEXT_PATH_PROPERTY).getText());
         }
         catch (ConfigurationElementNotFoundException ignored)
         {
            // Do nothing
         }
         try
         {
            builder.httpPort(Integer.valueOf(propertiesElem.getChildByName(HTTP_PORT_PROPERTY).getText()));
         }
         catch (ConfigurationElementNotFoundException ignored)
         {
            // Do nothing
         }
         try
         {
            builder.portOffset(Integer.valueOf(propertiesElem.getChildByName(PORT_OFFSET_PROPERTY).getText()));
         }
         catch (ConfigurationElementNotFoundException ignored)
         {
            // Do nothing
         }
      }
      return builder;
   }

   private WildFlySwarmConfigurationBuilder()
   {
   }

   public WildFlySwarmConfigurationBuilder httpPort(Integer httpPort)
   {
      this.httpPort = httpPort;
      return this;
   }

   public WildFlySwarmConfigurationBuilder portOffset(Integer portOffset)
   {
      this.portOffset = portOffset;
      return this;
   }

   public WildFlySwarmConfigurationBuilder contextPath(String contextPath)
   {
      this.contextPath = contextPath;
      return this;
   }

   public WildFlySwarmConfigurationBuilder mainClass(String mainClass)
   {
      this.mainClass = mainClass;
      return this;
   }

   @Override
   public Integer getHttpPort()
   {
      return this.httpPort;
   }

   @Override
   public String getContextPath()
   {
      return this.contextPath;
   }

   @Override
   public Integer getPortOffset()
   {
      return this.portOffset;
   }

   @Override
   public String getMainClass()
   {
      return this.mainClass;
   }

   @Override
   public void apply(Configuration config)
   {
      // Main Class
      config.removeConfigurationElement(MAIN_CLASS_CONFIGURATION_ELEMENT);
      if (!Strings.isNullOrEmpty(getMainClass()) && !MAIN_CLASS_DEFAULT_VALUE.equals(getMainClass()))
      {
         config.addConfigurationElement(ConfigurationElementBuilder.create()
                  .setName(MAIN_CLASS_CONFIGURATION_ELEMENT).setText(getMainClass()));
      }

      // Properties
      ConfigurationElementBuilder properties = ConfigurationElementBuilder.create().setName("properties");
      if (!Strings.isNullOrEmpty(getContextPath()) && !"/".equals(getContextPath())
               && !CONTEXT_PATH_DEFAULT_VALUE.equals(getContextPath()))
      {
         properties.addChild(CONTEXT_PATH_PROPERTY).setText(getContextPath());
      }
      if (getHttpPort() != null && getHttpPort() != 0
               && !getHttpPort().equals(HTTP_PORT_DEFAULT_VALUE))
      {
         properties.addChild(HTTP_PORT_PROPERTY)
                  .setText(getHttpPort().toString());
      }
      if (getPortOffset() != null
               && !getPortOffset().equals(PORT_OFFSET_DEFAULT_VALUE))
      {
         properties.addChild(PORT_OFFSET_PROPERTY)
                  .setText(getPortOffset().toString());
      }
      config.removeConfigurationElement("properties");
      if (properties.hasChildren())
      {
         config.addConfigurationElement(properties);
      }
   }
}

/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link WildFlySwarmConfigurationBuilder}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildFlySwarmConfigurationBuilderTest
{
   @Test
   public void testCreate()
   {
      WildFlySwarmConfigurationBuilder builder = WildFlySwarmConfigurationBuilder.create();
      Assert.assertNotNull(builder);
      builder.contextPath("/context").httpPort(80).portOffset(100);
      Assert.assertEquals("/context", builder.getContextPath());
      Assert.assertEquals(Integer.valueOf(80), builder.getHttpPort());
      Assert.assertEquals(Integer.valueOf(100), builder.getPortOffset());
   }

   @Test
   public void testConfigurationReadMainClass() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      config.addConfigurationElement(
               ConfigurationElementBuilder.create().setName("mainClass").setText("org.foo.Class"));
      WildFlySwarmConfigurationBuilder swarmConfig = WildFlySwarmConfigurationBuilder.create(config);
      assertThat(swarmConfig.getMainClass()).isEqualTo("org.foo.Class");
   }

   @Test
   public void testConfigurationWriteMainClass() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      WildFlySwarmConfigurationBuilder swarmConfig = WildFlySwarmConfigurationBuilder.create();
      swarmConfig.mainClass("org.foo.Class");
      swarmConfig.apply(config);
      assertThat(swarmConfig.getMainClass()).isEqualTo("org.foo.Class");
      assertThat(config.getConfigurationElement(WildFlySwarmConfiguration.MAIN_CLASS_CONFIGURATION_ELEMENT))
               .isNotNull();
      assertThat(config.getConfigurationElement(WildFlySwarmConfiguration.MAIN_CLASS_CONFIGURATION_ELEMENT).getText())
               .isEqualTo("org.foo.Class");
   }

   @Test
   public void testConfigurationReadProperties() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      ConfigurationElementBuilder properties = ConfigurationElementBuilder.create().setName("properties");
      properties.addChild(WildFlySwarmConfiguration.CONTEXT_PATH_PROPERTY).setText("/myapp");
      properties.addChild(WildFlySwarmConfiguration.HTTP_PORT_PROPERTY).setText("8089");
      properties.addChild(WildFlySwarmConfiguration.PORT_OFFSET_PROPERTY).setText("500");
      config.addConfigurationElement(properties);
      WildFlySwarmConfigurationBuilder swarmConfig = WildFlySwarmConfigurationBuilder.create(config);
      assertThat(swarmConfig).hasFieldOrPropertyWithValue("contextPath", "/myapp");
      assertThat(swarmConfig).hasFieldOrPropertyWithValue("httpPort", 8089);
      assertThat(swarmConfig).hasFieldOrPropertyWithValue("portOffset", 500);
   }

   @Test
   public void testConfigurationWriteProperties() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      WildFlySwarmConfigurationBuilder swarmConfig = WildFlySwarmConfigurationBuilder.create();
      swarmConfig.contextPath("/myapp").httpPort(8089).portOffset(500);
      swarmConfig.apply(config);

      List<ConfigurationElement> configElements = config.listConfigurationElements();
      assertThat(configElements.size()).isEqualTo(1);
      assertThat(configElements.get(0).getChildByName(WildFlySwarmConfiguration.CONTEXT_PATH_PROPERTY).getText())
               .isEqualTo("/myapp");
      assertThat(configElements.get(0).getChildByName(WildFlySwarmConfiguration.HTTP_PORT_PROPERTY).getText())
               .isEqualTo("8089");
      assertThat(configElements.get(0).getChildByName(WildFlySwarmConfiguration.PORT_OFFSET_PROPERTY).getText())
               .isEqualTo("500");

   }

}

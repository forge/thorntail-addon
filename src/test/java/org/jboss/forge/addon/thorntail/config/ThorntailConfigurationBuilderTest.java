/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.thorntail.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link ThorntailConfigurationBuilder}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ThorntailConfigurationBuilderTest
{
   @Test
   public void testCreate()
   {
      ThorntailConfigurationBuilder builder = ThorntailConfigurationBuilder.create();
      Assert.assertNotNull(builder);
      builder.contextPath("/context").httpPort(80).portOffset(100);
      Assert.assertEquals("/context", builder.getContextPath());
      Assert.assertEquals(Integer.valueOf(80), builder.getHttpPort());
      Assert.assertEquals(Integer.valueOf(100), builder.getPortOffset());
   }

   @Test
   public void testConfigurationReadProperties() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      ConfigurationElementBuilder properties = ConfigurationElementBuilder.create().setName("properties");
      properties.addChild(ThorntailConfiguration.CONTEXT_PATH_PROPERTY).setText("/myapp");
      properties.addChild(ThorntailConfiguration.HTTP_PORT_PROPERTY).setText("8089");
      properties.addChild(ThorntailConfiguration.PORT_OFFSET_PROPERTY).setText("500");
      config.addConfigurationElement(properties);
      ThorntailConfigurationBuilder thorntailConfig = ThorntailConfigurationBuilder.create(config);
      assertThat(thorntailConfig).hasFieldOrPropertyWithValue("contextPath", "/myapp");
      assertThat(thorntailConfig).hasFieldOrPropertyWithValue("httpPort", 8089);
      assertThat(thorntailConfig).hasFieldOrPropertyWithValue("portOffset", 500);
   }

   @Test
   public void testConfigurationWriteProperties() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      ThorntailConfigurationBuilder thorntailConfig = ThorntailConfigurationBuilder.create();
      thorntailConfig.contextPath("/myapp").httpPort(8089).portOffset(500);
      thorntailConfig.apply(config);

      List<ConfigurationElement> configElements = config.listConfigurationElements();
      assertThat(configElements.size()).isEqualTo(1);
      assertThat(configElements.get(0).getChildByName(ThorntailConfiguration.CONTEXT_PATH_PROPERTY).getText())
               .isEqualTo("/myapp");
      assertThat(configElements.get(0).getChildByName(ThorntailConfiguration.HTTP_PORT_PROPERTY).getText())
               .isEqualTo("8089");
      assertThat(configElements.get(0).getChildByName(ThorntailConfiguration.PORT_OFFSET_PROPERTY).getText())
               .isEqualTo("500");

   }

}

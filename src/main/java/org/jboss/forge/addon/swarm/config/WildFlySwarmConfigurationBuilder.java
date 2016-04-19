/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

/**
 * A builder for {@link WildFlySwarmConfiguration}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildFlySwarmConfigurationBuilder implements WildFlySwarmConfiguration
{
   private Integer httpPort;
   private Integer portOffset;
   private String contextPath;

   public static WildFlySwarmConfigurationBuilder create()
   {
      return new WildFlySwarmConfigurationBuilder();
   }

   public static WildFlySwarmConfigurationBuilder create(WildFlySwarmConfiguration config)
   {
      WildFlySwarmConfigurationBuilder builder = new WildFlySwarmConfigurationBuilder();
      builder.contextPath(config.getContextPath()).httpPort(config.getHttpPort()).portOffset(config.getPortOffset());
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

}

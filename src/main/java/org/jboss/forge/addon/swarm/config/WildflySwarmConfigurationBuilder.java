/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

/**
 * A builder for {@link WildflySwarmConfiguration}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildflySwarmConfigurationBuilder implements WildflySwarmConfiguration
{
   private int httpPort;
   private int portOffset;
   private String contextPath;

   public static WildflySwarmConfigurationBuilder create()
   {
      return new WildflySwarmConfigurationBuilder();
   }

   public static WildflySwarmConfigurationBuilder create(WildflySwarmConfiguration config)
   {
      WildflySwarmConfigurationBuilder builder = new WildflySwarmConfigurationBuilder();
      builder.contextPath(config.getContextPath()).httpPort(config.getHttpPort()).portOffset(config.getPortOffset());
      return builder;
   }

   private WildflySwarmConfigurationBuilder()
   {

   }

   public WildflySwarmConfigurationBuilder httpPort(int httpPort)
   {
      this.httpPort = httpPort;
      return this;
   }

   public WildflySwarmConfigurationBuilder portOffset(int portOffset)
   {
      this.portOffset = portOffset;
      return this;
   }

   public WildflySwarmConfigurationBuilder contextPath(String contextPath)
   {
      this.contextPath = contextPath;
      return this;
   }

   @Override
   public int getHttpPort()
   {
      return this.httpPort;
   }

   @Override
   public String getContextPath()
   {
      return this.contextPath;
   }

   @Override
   public int getPortOffset()
   {
      return this.portOffset;
   }

}

/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

/**
 * The configuration for the Wildfly Swarm plugin
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface WildflySwarmConfiguration
{
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
}

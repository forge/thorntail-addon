/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.thorntail.config;

import java.util.Map;

import org.jboss.forge.addon.maven.plugins.Configuration;

/**
 * The configuration for the Thorntail plugin
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ThorntailConfiguration
{
    String HTTP_PORT_PROPERTY = "swarm.http.port";
    Integer HTTP_PORT_DEFAULT_VALUE = 8080;

    String CONTEXT_PATH_PROPERTY = "swarm.context.path";
    String CONTEXT_PATH_DEFAULT_VALUE = "";

    String PORT_OFFSET_PROPERTY = "swarm.port.offset";
    Integer PORT_OFFSET_DEFAULT_VALUE = 0;

    String TEST_NAME_CONFIGURATION_ELEMENT = "testName";

    String TEST_AS_CLIENT_FLAG_CONFIGURATION_ELEMENT = "asClient";

    String TEST_TYPE_CONFIGURATION_ELEMENT = "archiveType";
    String TEST_TYPE_DEFAULT_VALUE = "JAR";

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
     * @return an unmodifiable {@link Map} of system properties for this microservice
     */
    Map<String, String> getProperties();

    /**
     * @param configuration apply this configuration to the given {@link Configuration}
     */
    void apply(Configuration configuration);
}

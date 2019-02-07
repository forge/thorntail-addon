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
    public static final String HTTP_PORT_PROPERTY = "swarm.http.port";
    public static final Integer HTTP_PORT_DEFAULT_VALUE = 8080;

    public static final String CONTEXT_PATH_PROPERTY = "swarm.context.path";
    public static final String CONTEXT_PATH_DEFAULT_VALUE = "";

    public static final String PORT_OFFSET_PROPERTY = "swarm.port.offset";
    public static final Integer PORT_OFFSET_DEFAULT_VALUE = 0;

    public static final String TEST_NAME_CONFIGURATION_ELEMENT = "testName";

    public static final String TEST_AS_CLIENT_FLAG_CONFIGURATION_ELEMENT = "asClient";

    public static final String TEST_TYPE_CONFIGURATION_ELEMENT = "archiveType";
    public static final String TEST_TYPE_DEFAULT_VALUE = "JAR";

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

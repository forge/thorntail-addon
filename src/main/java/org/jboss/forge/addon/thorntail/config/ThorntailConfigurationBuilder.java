/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.thorntail.config;

import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A builder for {@link ThorntailConfiguration}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ThorntailConfigurationBuilder implements ThorntailConfiguration
{

    private Map<String, String> properties = new TreeMap<>();

    public static ThorntailConfigurationBuilder create()
    {
        return new ThorntailConfigurationBuilder();
    }

    public static ThorntailConfigurationBuilder create(ThorntailConfiguration config)
    {
        ThorntailConfigurationBuilder builder = new ThorntailConfigurationBuilder();
        builder.properties(config.getProperties());
        return builder;
    }

    /**
     * Create a {@link ThorntailConfigurationBuilder} from a {@link Configuration} object
     */
    public static ThorntailConfigurationBuilder create(Configuration config)
    {
        ThorntailConfigurationBuilder builder = new ThorntailConfigurationBuilder();

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
            Map<String, String> properties = new TreeMap<>();
            propertiesElem.getChildren().stream()
                        .filter(ConfigurationElement.class::isInstance)
                        .map(ConfigurationElement.class::cast)
                        .forEach(elem -> properties.put(elem.getName(), elem.getText()));
            builder.properties(properties);
        }
        return builder;
    }

    private ThorntailConfigurationBuilder()
    {
    }

    public ThorntailConfigurationBuilder httpPort(Integer httpPort)
    {
        Objects.requireNonNull(httpPort, "HTTP Port should not be null");
        if (HTTP_PORT_DEFAULT_VALUE.equals(httpPort) || httpPort.intValue() == 0)
        {
            this.properties.remove(HTTP_PORT_PROPERTY);
        }
        else
        {
            this.properties.put(HTTP_PORT_PROPERTY, httpPort.toString());
        }
        return this;
    }

    public ThorntailConfigurationBuilder portOffset(Integer portOffset)
    {
        Objects.requireNonNull(portOffset, "Port offset should not be null");
        if (PORT_OFFSET_DEFAULT_VALUE.equals(portOffset))
        {
            this.properties.remove(PORT_OFFSET_PROPERTY);
        }
        else
        {
            this.properties.put(PORT_OFFSET_PROPERTY, portOffset.toString());
        }
        return this;
    }

    public ThorntailConfigurationBuilder contextPath(String contextPath)
    {
        Objects.requireNonNull(contextPath, "Context Path should not be null");
        if (CONTEXT_PATH_DEFAULT_VALUE.equals(contextPath) || "/".equals(contextPath))
        {
            this.properties.remove(CONTEXT_PATH_PROPERTY);
        }
        else
        {
            this.properties.put(CONTEXT_PATH_PROPERTY, contextPath);
        }
        return this;
    }

    public ThorntailConfigurationBuilder properties(Map<String, String> properties)
    {
        Objects.requireNonNull(properties, "Properties should not be null");
        this.properties = new TreeMap<>(properties);
        return this;
    }

    @Override
    public Integer getHttpPort()
    {
        return Integer.valueOf(properties.getOrDefault(HTTP_PORT_PROPERTY, HTTP_PORT_DEFAULT_VALUE.toString()));
    }

    @Override
    public String getContextPath()
    {
        return properties.getOrDefault(CONTEXT_PATH_PROPERTY, CONTEXT_PATH_DEFAULT_VALUE);
    }

    @Override
    public Integer getPortOffset()
    {
        return Integer.valueOf(properties.getOrDefault(PORT_OFFSET_PROPERTY, PORT_OFFSET_DEFAULT_VALUE.toString()));
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public void apply(Configuration config)
    {
        // Properties
        ConfigurationElementBuilder properties = ConfigurationElementBuilder.create().setName("properties");
        this.properties.forEach((key, value) -> properties.addChild(key).setText(value));
        config.removeConfigurationElement("properties");
        if (properties.hasChildren())
        {
            config.addConfigurationElement(properties);
        }
    }
}

/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.furnace.util.Strings;

/**
 * A builder for {@link WildFlySwarmConfiguration}
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class WildFlySwarmConfigurationBuilder implements WildFlySwarmConfiguration
{
    private String mainClass = WildFlySwarmConfiguration.MAIN_CLASS_DEFAULT_VALUE;
    private Map<String, String> properties = new TreeMap<>();

    public static WildFlySwarmConfigurationBuilder create()
    {
        return new WildFlySwarmConfigurationBuilder();
    }

    public static WildFlySwarmConfigurationBuilder create(WildFlySwarmConfiguration config)
    {
        WildFlySwarmConfigurationBuilder builder = new WildFlySwarmConfigurationBuilder();
        builder.mainClass(config.getMainClass()).properties(config.getProperties());
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
            Map<String, String> properties = new TreeMap<>();
            propertiesElem.getChildren().stream()
                        .filter(ConfigurationElement.class::isInstance)
                        .map(ConfigurationElement.class::cast)
                        .forEach(elem -> properties.put(elem.getName(), elem.getText()));
            builder.properties(properties);
        }
        return builder;
    }

    private WildFlySwarmConfigurationBuilder()
    {
    }

    public WildFlySwarmConfigurationBuilder httpPort(Integer httpPort)
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

    public WildFlySwarmConfigurationBuilder portOffset(Integer portOffset)
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

    public WildFlySwarmConfigurationBuilder contextPath(String contextPath)
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

    public WildFlySwarmConfigurationBuilder mainClass(String mainClass)
    {
        Objects.requireNonNull(properties, "Main class should not be null");
        this.mainClass = mainClass;
        return this;
    }

    public WildFlySwarmConfigurationBuilder properties(Map<String, String> properties)
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
    public String getMainClass()
    {
        return this.mainClass;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(this.properties);
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
        this.properties.forEach((key, value) -> properties.addChild(key).setText(value));
        config.removeConfigurationElement("properties");
        if (properties.hasChildren())
        {
            config.addConfigurationElement(properties);
        }
    }
}

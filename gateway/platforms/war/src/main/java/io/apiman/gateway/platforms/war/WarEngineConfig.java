/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.platforms.war;

import io.apiman.common.config.ConfigFactory;
import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.util.ReflectionUtils;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.EngineConfigTuple;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IGatewayInitializer;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.impl.DefaultPolicyErrorWriter;
import io.apiman.gateway.engine.impl.DefaultPolicyFailureWriter;
import io.apiman.gateway.engine.policy.IPolicyFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.configuration.Configuration;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarEngineConfig implements IEngineConfig {

    public static final String APIMAN_GATEWAY_REGISTRY_CLASS = "apiman-gateway.registry"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS = "apiman-gateway.plugin-registry"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS = "apiman-gateway.connector-factory"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_POLICY_FACTORY_CLASS = "apiman-gateway.policy-factory"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_METRICS_CLASS = "apiman-gateway.metrics"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_LOGGER_FACTORY_CLASS = "apiman-gateway.logger-factory"; //$NON-NLS-1$

    public static final String APIMAN_DATA_ENCRYPTER_TYPE = "apiman.encrypter.type"; //$NON-NLS-1$

    public static final String APIMAN_GATEWAY_COMPONENT_PREFIX = "apiman-gateway.components."; //$NON-NLS-1$

    public static final String APIMAN_GATEWAY_WRITER_FORMATTER_CLASS = "apiman-gateway.writers.policy-failure"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_ERROR_WRITER_CLASS = "apiman-gateway.writers.error"; //$NON-NLS-1$

    public static final String APIMAN_GATEWAY_INITIALIZERS = "apiman-gateway.initializers"; //$NON-NLS-1$

    public static final Configuration config;
    static {
        config = ConfigFactory.createConfig();
    }

    /**
     * Constructor.
     */
    public WarEngineConfig() {
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Returns the given configuration property name or the provided default
     * value if not found.
     * @param propertyName the property name
     * @param defaultValue the default value
     * @return the config property
     */
    public String getConfigProperty(String propertyName, String defaultValue) {
        return getConfig().getString(propertyName, defaultValue);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getRegistryClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IRegistry> getRegistryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_REGISTRY_CLASS, IRegistry.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the registry
     */
    @Override
    public Map<String, String> getRegistryConfig() {
        return getConfigMap(APIMAN_GATEWAY_REGISTRY_CLASS);
    }

    /**
     * @return the class to use as the {@link IPluginRegistry}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<IPluginRegistry> getPluginRegistryClass() {
        return (Class<IPluginRegistry>) loadConfigClass(APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, IPluginRegistry.class, null);
    }

    /**
     * @return all properties to be passed to the registry
     */
    @Override
    public Map<String, String> getPluginRegistryConfig() {
        Map<String, String> configMap = getConfigMap(APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS);
        String pluginsDirOverride = System.getProperty(APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS + ".pluginsDir"); //$NON-NLS-1$
        if (pluginsDirOverride != null) {
            configMap.put("pluginsDir", pluginsDirOverride); //$NON-NLS-1$
        }
        return configMap;
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getConnectorFactoryClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, IConnectorFactory.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return getConfigMap(APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getPolicyFactoryClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_POLICY_FACTORY_CLASS, IPolicyFactory.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return getConfigMap(APIMAN_GATEWAY_POLICY_FACTORY_CLASS);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getMetricsClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IMetrics> getMetricsClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_METRICS_CLASS, IMetrics.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public Map<String, String> getMetricsConfig() {
        return getConfigMap(APIMAN_GATEWAY_METRICS_CLASS);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getComponentClass(java.lang.Class, io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType,
            IPluginRegistry pluginRegistry) {
        return (Class<T>) loadConfigClass(APIMAN_GATEWAY_COMPONENT_PREFIX + componentType.getSimpleName(), componentType, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        return getConfigMap(APIMAN_GATEWAY_COMPONENT_PREFIX + componentType.getSimpleName());
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getDataEncrypterClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IDataEncrypter> getDataEncrypterClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_DATA_ENCRYPTER_TYPE, IDataEncrypter.class, pluginRegistry);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getDataEncrypterConfig()
     */
    @Override
    public Map<String, String> getDataEncrypterConfig() {
        return getConfigMap("apiman.encrypter"); //$NON-NLS-1$
    }

    /**
     * @param pluginRegistry The plugin registry
     * @return the class to use as the {@link IPolicyFailureWriter}
     */
    @SuppressWarnings("unchecked")
    public Class<IPolicyFailureWriter> getPolicyFailureWriterClass(IPluginRegistry pluginRegistry) {
        return (Class<IPolicyFailureWriter>) loadConfigClass(APIMAN_GATEWAY_WRITER_FORMATTER_CLASS,
                IPolicyFailureWriter.class, pluginRegistry, DefaultPolicyFailureWriter.class);
    }

    /**
     * @return all properties to be passed to the failure formatter
     */
    public Map<String, String> getPolicyFailureWriterConfig() {
        return getConfigMap(APIMAN_GATEWAY_WRITER_FORMATTER_CLASS);
    }

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IPolicyErrorWriter}
     */
    @SuppressWarnings("unchecked")
    public Class<IPolicyErrorWriter> getPolicyErrorWriterClass(IPluginRegistry pluginRegistry) {
        return (Class<IPolicyErrorWriter>) loadConfigClass(APIMAN_GATEWAY_ERROR_WRITER_CLASS,
                IPolicyErrorWriter.class, pluginRegistry, DefaultPolicyErrorWriter.class);
    }

    /**
     * @return all properties to be passed to the error formatter
     */
    public Map<String, String> getPolicyErrorWriterConfig() {
        return getConfigMap(APIMAN_GATEWAY_ERROR_WRITER_CLASS);
    }

    @Override
    public Class<? extends IDelegateFactory> getLoggerFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_LOGGER_FACTORY_CLASS,
                IDelegateFactory.class, pluginRegistry, DefaultDelegateFactory.class);
    }

    @Override
    public Map<String, String> getLoggerFactoryConfig() {
        return getConfigMap(APIMAN_GATEWAY_LOGGER_FACTORY_CLASS);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getGatewayInitializers(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public List<EngineConfigTuple<? extends IGatewayInitializer>> getGatewayInitializers(
            IPluginRegistry pluginRegistry) {
        List<EngineConfigTuple<? extends IGatewayInitializer>> rval = new ArrayList<>();

        String initializerIds = getConfig().getString(APIMAN_GATEWAY_INITIALIZERS);
        if (initializerIds != null) {
            for (String initializerId : initializerIds.split(",")) { //$NON-NLS-1$
                String initializerClassProp = APIMAN_GATEWAY_INITIALIZERS + "." + initializerId; //$NON-NLS-1$
                Class<? extends IGatewayInitializer> initializerClass = loadConfigClass(initializerClassProp, IGatewayInitializer.class, pluginRegistry);
                Map<String, String> configMap = getConfigMap(initializerClassProp);
                rval.add(new EngineConfigTuple<>(initializerClass, configMap));
            }
        }

        return rval;
    }

    /**
     * @return a loaded class
     */
    private <T> Class<? extends T> loadConfigClass(String property, Class<T> type, IPluginRegistry pluginRegistry) {
        Class<? extends T> rval = loadConfigClass(property, type, pluginRegistry, null);
        if (rval == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return rval;
    }

    /**
     * Load a config class
     */
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> loadConfigClass(String property, Class<T> type, IPluginRegistry pluginRegistry, Class<? extends T> defaultClass) {
        String componentSpec = getConfig().getString(property);
        if (componentSpec == null) {
            return defaultClass;
        }

        try {
            if (componentSpec.startsWith("class:")) { //$NON-NLS-1$
                Class<?> c = ReflectionUtils.loadClass(componentSpec.substring("class:".length())); //$NON-NLS-1$
                return (Class<T>) c;
            } else if (componentSpec.startsWith("plugin:")) { //$NON-NLS-1$
                PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(componentSpec);
                if (coordinates == null) {
                    throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
                }
                int ssidx = componentSpec.indexOf('/');
                if (ssidx == -1) {
                    throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
                }
                String classname = componentSpec.substring(ssidx + 1);
                Future<IAsyncResult<Plugin>> pluginF = pluginRegistry.loadPlugin(coordinates, null);
                IAsyncResult<Plugin> pluginR = pluginF.get();
                if (pluginR.isError()) {
                    throw new RuntimeException(pluginR.getError());
                }
                Plugin plugin = pluginR.getResult();
                PluginClassLoader classLoader = plugin.getLoader();
                Class<?> class1 = classLoader.loadClass(classname);
                return (Class<T>) class1;
            } else {
                Class<?> c = ReflectionUtils.loadClass(componentSpec);
                return (Class<T>) c;
            }
        } catch (ClassNotFoundException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all properties in the engine configuration that are prefixed
     * with the given prefix.
     * @param prefix the prefix
     * @return all prefixed properties
     */
    private Map<String, String> getConfigMap(String prefix) {
        Map<String, String> rval = new HashMap<>();
        Iterator<?> keys = config.getKeys(prefix);
        while (keys.hasNext()) {
            String key = String.valueOf(keys.next());
            if (key.equals(prefix)) {
                continue;
            }
            String shortKey = key.substring(prefix.length() + 1);
            rval.put(shortKey, config.getString(key));
        }
        return rval;
    }


}

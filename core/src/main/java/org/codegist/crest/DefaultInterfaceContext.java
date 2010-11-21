package org.codegist.crest;

import org.codegist.common.collect.Maps;
import org.codegist.crest.config.InterfaceConfig;

import java.util.Map;

/**
 * Default internal immutable implementation of InterfaceContext
 */
class DefaultInterfaceContext implements InterfaceContext {

    private final InterfaceConfig config;
    private final Map<String, Object> customProperties;

    public DefaultInterfaceContext(InterfaceContext context) {
        this(context.getConfig(), context.getCustomProperties());
    }

    public DefaultInterfaceContext(InterfaceConfig config, Map<String, Object> customProperties) {
        this.config = config;
        this.customProperties = Maps.unmodifiable(customProperties);
    }

    @Override
    public InterfaceConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    @Override
    public <T> T getCustomProperty(String name) {
        return (T) customProperties.get(name);
    }
}

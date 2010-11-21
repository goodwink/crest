package org.codegist.crest.config;

import org.codegist.crest.CRestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Preconfigured InterfaceConfigFactory holding a mapping class->InterfaceConfig.
 */
public class PreconfiguredInterfaceConfigFactory implements InterfaceConfigFactory {

    private final Map<Class<?>, InterfaceConfig> config;

    public PreconfiguredInterfaceConfigFactory(InterfaceConfig... configs) {
        Map<Class<?>, InterfaceConfig> configMap = new HashMap<Class<?>, InterfaceConfig>();
        for (InterfaceConfig cfg : configs) {
            configMap.put(cfg.getInterface(), cfg);
        }
        this.config = configMap;
    }

    @Override
    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) {
        return config.get(interfaze);
    }
}

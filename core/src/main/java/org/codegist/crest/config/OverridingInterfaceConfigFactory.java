package org.codegist.crest.config;

import org.codegist.crest.CRestContext;

/**
 * Simple InterfaceConfigFactory that returns a overridden configuration, result of the config creation for a given interface from two InterfaceConfigFactories.
 *
 * @see org.codegist.crest.config.Configs#override(InterfaceConfig, InterfaceConfig)
 */
public class OverridingInterfaceConfigFactory implements InterfaceConfigFactory {

    private final InterfaceConfigFactory baseFactory;

    private final InterfaceConfigFactory overriderFactory;
    private final InterfaceConfig override;

    /**
     * Build a factory that will override any result from baseFactory with the given config template
     *
     * @param baseFactory Factory from which results will be overridden by override
     * @param override    InterfaceConfig override template.
     * @see org.codegist.crest.config.Configs#override(InterfaceConfig, InterfaceConfig)
     */
    public OverridingInterfaceConfigFactory(InterfaceConfigFactory baseFactory, InterfaceConfig override) {
        this.baseFactory = baseFactory;
        this.override = override;
        this.overriderFactory = null;
    }

    /**
     * Build a factory that will override any result from baseFactory with the given config template
     *
     * @param baseFactory      Factory from which results will be overridden by overriderFactory
     * @param overriderFactory Config override factory
     * @see org.codegist.crest.config.Configs#override(InterfaceConfig, InterfaceConfig)
     */
    public OverridingInterfaceConfigFactory(InterfaceConfigFactory baseFactory, InterfaceConfigFactory overriderFactory) {
        this.baseFactory = baseFactory;
        this.overriderFactory = overriderFactory;
        this.override = null;
    }

    @Override
    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        InterfaceConfig configBase = baseFactory.newConfig(interfaze, context);
        InterfaceConfig override = this.override;
        if (overriderFactory != null) {
            override = overriderFactory.newConfig(interfaze, context);
        }
        return Configs.override(configBase, override);
    }
}

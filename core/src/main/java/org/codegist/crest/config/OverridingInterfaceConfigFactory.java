/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.crest.CRestContext;

/**
 * Simple InterfaceConfigFactory that returns a overridden configuration, result of the config creation for a given interface from two InterfaceConfigFactories.
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 * @see org.codegist.crest.config.Configs#override(InterfaceConfig, InterfaceConfig)
 */
public class OverridingInterfaceConfigFactory implements InterfaceConfigFactory {

    private final InterfaceConfigFactory baseFactory;

    private final InterfaceConfigFactory overriderFactory;
    private final InterfaceConfig override;
    private final boolean dynamicOverride;

    /**
     * Build a factory that will override any result from baseFactory with the given config template
     *
     * @param baseFactory     Factory from which results will be overridden by override
     * @param override        InterfaceConfig override template.
     * @param dynamicOverride Whether or not the override should be dynamic (meaning base and/or override can change their values over time)
     * @see org.codegist.crest.config.Configs#override(InterfaceConfig, InterfaceConfig)
     */
    public OverridingInterfaceConfigFactory(InterfaceConfigFactory baseFactory, InterfaceConfig override, boolean dynamicOverride) {
        this.baseFactory = baseFactory;
        this.override = override;
        this.overriderFactory = null;
        this.dynamicOverride = dynamicOverride;
    }

    /**
     * Uses static overrides by default
     *
     * @param baseFactory Factory from which results will be overridden by override
     * @param override    InterfaceConfig override template.
     * @see org.codegist.crest.config.OverridingInterfaceConfigFactory#OverridingInterfaceConfigFactory(InterfaceConfigFactory, InterfaceConfig, boolean)
     */
    public OverridingInterfaceConfigFactory(InterfaceConfigFactory baseFactory, InterfaceConfig override) {
        this(baseFactory, override, false);
    }

    /**
     * Build a factory that will override any result from baseFactory with the given config template
     *
     * @param baseFactory      Factory from which results will be overridden by overriderFactory
     * @param overriderFactory Config override factory
     * @param dynamicOverride  Whether or not the override should be dynamic (meaning base and/or override can change their values over time
     * @see org.codegist.crest.config.Configs#override(InterfaceConfig, InterfaceConfig)
     */
    public OverridingInterfaceConfigFactory(InterfaceConfigFactory baseFactory, InterfaceConfigFactory overriderFactory, boolean dynamicOverride) {
        this.baseFactory = baseFactory;
        this.overriderFactory = overriderFactory;
        this.override = null;
        this.dynamicOverride = dynamicOverride;
    }

    /**
     * Uses dynamic overrides by default
     *
     * @param baseFactory      Factory from which results will be overridden by overriderFactory
     * @param overriderFactory Config override factory
     * @see org.codegist.crest.config.OverridingInterfaceConfigFactory#OverridingInterfaceConfigFactory(InterfaceConfigFactory, InterfaceConfigFactory, boolean)
     */
    public OverridingInterfaceConfigFactory(InterfaceConfigFactory baseFactory, InterfaceConfigFactory overriderFactory) {
        this(baseFactory, overriderFactory, true);
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        InterfaceConfig configBase = baseFactory.newConfig(interfaze, context);
        InterfaceConfig override = this.override;
        if (overriderFactory != null) {
            override = overriderFactory.newConfig(interfaze, context);
        }
        return Configs.override(configBase, override, dynamicOverride);
    }
}

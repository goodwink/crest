/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.crest.CRestContext;

/**
 * @author laurent.gilles@codegist.org
 */
public class JaxRSAwareAnnotationDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    private final InterfaceConfigFactory crestAnnotationFactory = new CRestAnnotationDrivenInterfaceConfigFactory(true);
    private final InterfaceConfigFactory jaxRsAnnotationFactory = new JaxRSAnnotationDrivenInterfaceConfigFactory(true);
    private final boolean crestPriority;

    public JaxRSAwareAnnotationDrivenInterfaceConfigFactory(boolean crestPriority) {
        this.crestPriority = crestPriority;
    }

    public JaxRSAwareAnnotationDrivenInterfaceConfigFactory() {
        this(true);
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        InterfaceConfig crestConfig = crestAnnotationFactory.newConfig(interfaze, context);
        InterfaceConfig jaxRsConfig = jaxRsAnnotationFactory.newConfig(interfaze, context);
        InterfaceConfig baseDefaultConfig = new ConfigBuilders.InterfaceConfigBuilder(interfaze, context.getProperties())
                .setEndPoint(crestConfig.getEndPoint())
                .buildUnvalidatedConfig();

        InterfaceConfig base = crestPriority ? jaxRsConfig : crestConfig;
        InterfaceConfig overrides = crestPriority ? crestConfig : jaxRsConfig;

        InterfaceConfig overridden = Configs.override(base, overrides);

        return Configs.override(baseDefaultConfig, overridden);
    }

}

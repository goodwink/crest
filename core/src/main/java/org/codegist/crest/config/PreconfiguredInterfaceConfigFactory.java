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

import java.util.HashMap;
import java.util.Map;

/**
 * Preconfigured InterfaceConfigFactory holding a mapping class->InterfaceConfig.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
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

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) {
        return config.get(interfaze);
    }
}

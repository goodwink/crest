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

package org.codegist.crest;

import org.codegist.common.collect.Maps;
import org.codegist.crest.config.InterfaceConfig;

import java.util.Map;

/**
 * Default internal immutable implementation of InterfaceContext
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
class DefaultInterfaceContext implements InterfaceContext {

    private final InterfaceConfig config;
    private final Map<String, Object> customProperties;

    public DefaultInterfaceContext(InterfaceContext context) {
        this(context.getConfig(), context.getProperties());
    }

    public DefaultInterfaceContext(InterfaceConfig config, Map<String, Object> customProperties) {
        this.config = config;
        this.customProperties = Maps.unmodifiable(customProperties);
    }

    public InterfaceConfig getConfig() {
        return config;
    }

    public Map<String, Object> getProperties() {
        return customProperties;
    }

    public <T> T getProperty(String name) {
        return (T) customProperties.get(name);
    }
}

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
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.InterfaceConfigFactory;

import java.util.Map;

/**
 * Default internal immutable implementation of CRestContext
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
class DefaultCRestContext implements CRestContext {
    private final RestService restService;
    private final ProxyFactory proxyFactory;
    private final InterfaceConfigFactory configFactory;
    private final Map<String, Object> customProperties;

    public DefaultCRestContext(CRestContext context) {
        this(context.getRestService(), context.getProxyFactory(), context.getConfigFactory(), context.getProperties());
    }

    public DefaultCRestContext(RestService restService, ProxyFactory proxyFactory, InterfaceConfigFactory configFactory, Map<String, Object> customProperties) {
        this.restService = restService;
        this.proxyFactory = proxyFactory;
        this.configFactory = configFactory;
        this.customProperties = Maps.unmodifiable(customProperties);
    }

    public RestService getRestService() {
        return restService;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public Map<String, Object> getProperties() {
        return customProperties;
    }

    public InterfaceConfigFactory getConfigFactory() {
        return configFactory;
    }
}

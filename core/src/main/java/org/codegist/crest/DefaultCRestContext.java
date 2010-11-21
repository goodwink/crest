package org.codegist.crest;

import org.codegist.common.collect.Maps;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.InterfaceConfigFactory;

import java.util.Map;

/**
 * Default internal immutable implementation of CRestContext
 */
class DefaultCRestContext implements CRestContext {
    private final RestService restService;
    private final ProxyFactory proxyFactory;
    private final InterfaceConfigFactory configFactory;
    private final Map<String, Object> customProperties;

    public DefaultCRestContext(CRestContext context) {
        this(context.getRestService(), context.getProxyFactory(), context.getConfigFactory(), context.getCustomProperties());
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

    @Override
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    @Override
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    @Override
    public InterfaceConfigFactory getConfigFactory() {
        return configFactory;
    }
}

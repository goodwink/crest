package org.codegist.crest;

import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.InterfaceConfigFactory;

import java.util.Map;

/**
 * Global CRest context object holding the DefaultCRest dependencies
 */
public interface CRestContext {

    RestService getRestService();

    ProxyFactory getProxyFactory();

    /**
     * @return current interface configuration
     */
    InterfaceConfigFactory getConfigFactory();

    /**
     * User's custom properties.
     * <p>Can be used to override defaults values for config objects or any other user's specifics data to be passed to custom interceptors,injectors,serializers.
     *
     * @return user custom properties map.
     * @see org.codegist.crest.config.InterfaceConfig
     * @see org.codegist.crest.config.MethodConfig
     * @see org.codegist.crest.config.ParamConfig
     */
    Map<String, Object> getCustomProperties();

}

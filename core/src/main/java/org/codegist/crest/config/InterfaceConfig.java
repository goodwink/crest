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

import org.codegist.crest.interceptor.EmptyRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;

/**
 * Interface configuration holder object.
 * <p>Implementors must respect the following contract :
 * <p>- No method return null except for the ones documented or when used as an override template (see {@link Configs#override(InterfaceConfig, InterfaceConfig)})
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.CRestContext#getCustomProperties()}'s defaults overrides.
 * <p>- Every methods in the interface must have it's respective {@link MethodConfig} configured.
 * <p>- Every arguments of every methods in the interface must have it's respective {@link org.codegist.crest.config.ParamConfig} configured in its respective {@link MethodConfig} object.
 *
 * @see org.codegist.crest.config.MethodConfig
 * @see org.codegist.crest.config.ParamConfig
 * @see org.codegist.crest.config.InterfaceConfigFactory
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface InterfaceConfig {

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default encoding {@link InterfaceConfig#DEFAULT_ENCODING}.
     * <p>Expects a string.
     *
     * @see InterfaceConfig#DEFAULT_ENCODING
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_ENCODING_PROP = InterfaceConfig.class.getName() + "#encoding";

    /**
     * Default encoding applied when non specified.
     *
     * @see InterfaceConfig#getEncoding()
     */
    String DEFAULT_ENCODING = "UTF-8";

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default path {@link InterfaceConfig#DEFAULT_PATH}.
     * <p>Expects a string.
     *
     * @see InterfaceConfig#DEFAULT_PATH
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_PATH_PROP = InterfaceConfig.class.getName() + "#path";

    /**
     * Default service url fragment applied when non specified.
     *
     * @see org.codegist.crest.config.InterfaceConfig#getPath()
     */
    String DEFAULT_PATH = "";


    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default global request interceptor {@link InterfaceConfig#DEFAULT_REQUEST_INTERCEPTOR}.
     * <p>Expects an instance of {@link org.codegist.crest.interceptor.RequestInterceptor}.
     *
     * @see InterfaceConfig#DEFAULT_REQUEST_INTERCEPTOR
     * @see org.codegist.crest.interceptor.RequestInterceptor
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_REQUEST_INTERCEPTOR_PROP = InterfaceConfig.class.getName() + "#request.interceptor";

    /**
     * Default request interceptor applied when non specified.
     *
     * @see org.codegist.crest.config.InterfaceConfig#getRequestInterceptor()
     */
    RequestInterceptor DEFAULT_REQUEST_INTERCEPTOR = new EmptyRequestInterceptor();

    /*##############################################################################*/

    /**
     * Encoding of the interface.
     *
     * @return the encoding of the interface
     */
    String getEncoding();

    /**
     * Server path (eg: http://www.my-end-point.com:8080)
     *
     * @return server's path
     */
    String getServer();

    /**
     * Service base path (eg: /base/service/path)
     *
     * @return server's path
     */
    String getPath();

    /**
     * Global service request interceptor.
     * <p>For a given request, the call order is :
     * <p>- InterfaceConfig.requestInterceptor.beforeParamsInjectionHandle(...)
     * <p>- MethodConfig.requestInterceptor.beforeParamsInjectionHandle(...)
     * <p>- MethodConfig.requestInterceptor.afterParamsInjectionHandle(...)
     * <p>- InterfaceConfig.requestInterceptor.afterParamsInjectionHandle(...)
     *
     * @return global service request interceptor.
     */
    RequestInterceptor getRequestInterceptor();

    /**
     * @return The interface being configured by the current object.
     */
    Class<?> getInterface();

    /**
     * @return Method list of the interface being configured by the current object.
     */
    Method[] getMethods();

    /**
     * @param meth Method to retrieve the config for
     * @return The method config object for the given method, null if not found.
     */
    MethodConfig getMethodConfig(Method meth);
}

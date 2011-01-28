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

import org.codegist.crest.handler.*;
import org.codegist.crest.interceptor.EmptyRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;

/**
 * Method configuration holder object.
 * <p>Implementors must respect the following contract :
 * <p>- No method return null except for the ones documented or when used as an override template (see {@link Configs#override(MethodConfig, MethodConfig)})
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.InterfaceContext#getProperties()}'s defaults overrides.
 * <p>- Every arguments of every methods in the interface must have it's respective {@link org.codegist.crest.config.ParamConfig} configured in its respective {@link MethodConfig} object.
 *
 * @see org.codegist.crest.config.MethodConfig
 * @see org.codegist.crest.config.ParamConfig
 * @see org.codegist.crest.config.InterfaceConfigFactory
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface MethodConfig {

    /**
     * Default socket timeout applied when non specified.
     *
     * @see org.codegist.crest.config.MethodConfig#getSocketTimeout()
     */
    long DEFAULT_SO_TIMEOUT = 20000;

    /**
     * Default connection timeout applied when non specified.
     *
     * @see MethodConfig#getConnectionTimeout()
     */
    long DEFAULT_CO_TIMEOUT = 20000;

    /**
     * Default url fragment applied when non specified.
     *
     * @see MethodConfig#getPath()
     */
    String DEFAULT_PATH = "";

    /**
     * Default method extra params.
     *
     * @see org.codegist.crest.config.MethodConfig#getExtraParams()
     */
    BasicParamConfig[] DEFAULT_EXTRA_PARAMS = new BasicParamConfig[0];

    /**
     * Default http method applied when non specified.
     *
     * @see MethodConfig#getHttpMethod()
     */
    String DEFAULT_HTTP_METHOD = "GET";

    /**
     * Default response handler applied when non specified.
     *
     * @see MethodConfig#getResponseHandler()
     */
    Class<? extends ResponseHandler> DEFAULT_RESPONSE_HANDLER = DefaultResponseHandler.class;

    /**
     * Default error handler applied when non specified.
     *
     * @see MethodConfig#getErrorHandler()
     */
    Class<? extends ErrorHandler> DEFAULT_ERROR_HANDLER = ErrorDelegatorHandler.class;

    /**
     * Default request interceptor applied when non specified.
     *
     * @see MethodConfig#getRequestInterceptor()
     */
    Class<? extends RequestInterceptor> DEFAULT_REQUEST_INTERCEPTOR = EmptyRequestInterceptor.class;

    /**
     * Default retry handler applied when non specified.
     *
     * @see org.codegist.crest.config.MethodConfig#getRetryHandler()
     */
    Class<? extends RetryHandler> DEFAULT_RETRY_HANDLER = MaxAttemptRetryHandler.class;

    /*##############################################################################*/

    /**
     * @return The method being configured by the current object
     */
    Method getMethod();

    ResponseHandler getResponseHandler();

    ErrorHandler getErrorHandler();

    RequestInterceptor getRequestInterceptor();

    Long getSocketTimeout();

    Long getConnectionTimeout();

    RetryHandler getRetryHandler();

    /**
     * URL fragment specific to this methods.
     * <p> Doesn't contains the server part.
     * <p> Full url is {@link InterfaceConfig#getEndPoint()} + {@link InterfaceConfig#getContextPath()} + {@link MethodConfig#getPath()}
     * <p>This value can contain placeholders that points to method arguments. For a path as /my-path/{2}/{0}/{2}.json?my-param={1}, any {n} placeholder will be replaced with the serialized parameter found at the respective method argument index when using the default parameter injector.
     *
     * @return the method url fragment
     * @see InterfaceConfig#getEndPoint()
     * @see InterfaceConfig#getContextPath()
     */
    String getPath();

    String getHttpMethod();

    BasicParamConfig[] getExtraParams();

    /**
     * Get the ParamConfig object holding the configuration of the method's arguments at the requested index.
     *
     * @param index
     * @return The param config object at the specified index, null if not found.
     */
    ParamConfig getParamConfig(int index);

    /**
     * @return The param count.
     */
    Integer getParamCount();
}

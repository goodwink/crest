package org.codegist.crest.config;

import org.codegist.crest.*;
import org.codegist.crest.interceptor.EmptyRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;

/**
 * Method configuration holder object.
 * <p>Implementors must respect the following contract :
 * <p>- No method return null except for the ones documented or when used as an override template (see {@link Configs#override(MethodConfig, MethodConfig)})
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.CRestContext#getCustomProperties()}'s defaults overrides.
 * <p>- Every arguments of every methods in the interface must have it's respective {@link org.codegist.crest.config.ParamConfig} configured in its respective {@link MethodConfig} object.
 *
 * @see org.codegist.crest.config.MethodConfig
 * @see org.codegist.crest.config.ParamConfig
 * @see org.codegist.crest.config.InterfaceConfigFactory
 */
public interface MethodConfig {

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default socket timeout {@link MethodConfig#DEFAULT_SO_TIMEOUT}.
     * <p>Expects milliseconds (long).
     *
     * @see MethodConfig#DEFAULT_SO_TIMEOUT
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_SO_TIMEOUT_PROP = MethodConfig.class.getName() + "#socket.timeout";

    /**
     * Default socket timeout applied when non specified.
     *
     * @see org.codegist.crest.config.MethodConfig#getSocketTimeout()
     */
    long DEFAULT_SO_TIMEOUT = 20000;

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default connection timeout {@link MethodConfig#DEFAULT_CO_TIMEOUT}.
     * <p>Expects milliseconds (long).
     *
     * @see MethodConfig#DEFAULT_CO_TIMEOUT
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_CO_TIMEOUT_PROP = MethodConfig.class.getName() + "#connection.timeout";

    /**
     * Default connection timeout applied when non specified.
     *
     * @see MethodConfig#getConnectionTimeout()
     */
    long DEFAULT_CO_TIMEOUT = 20000;

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default url fragment {@link MethodConfig#DEFAULT_PATH}.
     * <p>Expects a string.
     *
     * @see MethodConfig#DEFAULT_PATH
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_PATH_PROP = MethodConfig.class.getName() + "#path";

    /**
     * Default url fragment applied when non specified.
     *
     * @see MethodConfig#getPath()
     */
    String DEFAULT_PATH = "";

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default http method {@link MethodConfig#DEFAULT_HTTP_METHOD}.
     * <p>Expects a member of {@link org.codegist.crest.HttpMethod}.
     *
     * @see MethodConfig#DEFAULT_HTTP_METHOD
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_HTTP_METHOD_PROP = MethodConfig.class.getName() + "#http.method";

    /**
     * Default http method applied when non specified.
     *
     * @see MethodConfig#getHttpMethod()
     */
    HttpMethod DEFAULT_HTTP_METHOD = HttpMethod.GET;

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default response handler {@link MethodConfig#DEFAULT_RESPONSE_HANDLER}.
     * <p>Expects an instance of {@link org.codegist.crest.ResponseHandler}.
     *
     * @see MethodConfig#DEFAULT_RESPONSE_HANDLER
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_RESPONSE_HANDLER_PROP = MethodConfig.class.getName() + "#response.handler";

    /**
     * Default response handler applied when non specified.
     *
     * @see MethodConfig#getResponseHandler()
     */
    ResponseHandler DEFAULT_RESPONSE_HANDLER = new DefaultResponseHandler();

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default error handler {@link MethodConfig#DEFAULT_ERROR_HANDLER}.
     * <p>Expects an instance of {@link org.codegist.crest.ErrorHandler}.
     *
     * @see MethodConfig#DEFAULT_ERROR_HANDLER
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_ERROR_HANDLER_PROP = MethodConfig.class.getName() + "#error.handler";

    /**
     * Default error handler applied when non specified.
     *
     * @see MethodConfig#getErrorHandler()
     */
    ErrorHandler DEFAULT_ERROR_HANDLER = new ErrorDelegatorHandler();

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to override the default request interceptor {@link MethodConfig#DEFAULT_REQUEST_INTERCEPTOR}.
     * <p>Expects an instance of {@link org.codegist.crest.interceptor.RequestInterceptor}.
     *
     * @see MethodConfig#DEFAULT_REQUEST_INTERCEPTOR
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    String DEFAULT_REQUEST_INTERCEPTOR_PROP = MethodConfig.class.getName() + "#request.interceptor";

    /**
     * Default request interceptor applied when non specified.
     *
     * @see MethodConfig#getRequestInterceptor()
     */
    RequestInterceptor DEFAULT_REQUEST_INTERCEPTOR = new EmptyRequestInterceptor();

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

    /**
     * URL fragment specific to this methods.
     * <p> Doesn't contains the server part.
     * <p> Full url is {@link InterfaceConfig#getServer()} + {@link InterfaceConfig#getPath()} + {@link MethodConfig#getPath()}
     * <p>This value can contain placeholders that points to method arguments. For a path as /my-path/{2}/{0}/{2}.json?my-param={1}, any {n} placeholder will be replaced with the serialized parameter found at the respective method argument index when using the default parameter injector.
     *
     * @return the method url fragment
     * @see InterfaceConfig#getServer()
     * @see InterfaceConfig#getPath()
     */
    String getPath();

    HttpMethod getHttpMethod();

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

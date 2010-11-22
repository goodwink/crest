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

package org.codegist.crest.annotate;

import org.codegist.crest.ErrorHandler;
import org.codegist.crest.ResponseHandler;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;
import org.codegist.crest.config.Fallbacks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this to annotate any {@link org.codegist.crest.annotate.RestApi} annotated interface methods.
 * <p>This annotation is optional, if not specified, configuration will fallback to the {@link org.codegist.crest.annotate.RestApi} defaults for methods.
 * <p>If used on a method, any configuration will override the {@link org.codegist.crest.annotate.RestApi} defaults, and any non specified values will fallback following the same rules as stated before.
 * <p>For more information on how these values are used, please consult {@link org.codegist.crest.config.MethodConfig} documentation.
 *
 * @see org.codegist.crest.annotate.RestApi
 * @see org.codegist.crest.annotate.RestParam
 * @see org.codegist.crest.config.MethodConfig
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RestMethod {

    /**
     * Path specific to this methods, overrides the {@link org.codegist.crest.annotate.RestApi#methodsPath()} configuration.
     * <p>This value gets concatenated to {@link  org.codegist.crest.annotate.RestApi#endPoint()} +
     * {@link  org.codegist.crest.annotate.RestApi#path()} in order to get the full server URL.
     * <p>See documentation for {@link org.codegist.crest.config.MethodConfig#getPath()}.
     * <p>Defaults fallback to {@link org.codegist.crest.annotate.RestApi#methodsPath()}.
     *
     * @return method specific path
     * @see org.codegist.crest.config.MethodConfig#getPath()
     * @see org.codegist.crest.annotate.RestApi#methodsPath()
     */
    String path() default Fallbacks.FALLBACK_STRING;

    /**
     * HTTP method specific to this service call, overrides the {@link org.codegist.crest.annotate.RestApi#methodsHttpMethod()} configuration.
     * <p>Defaults fallback to {@link org.codegist.crest.annotate.RestApi#methodsHttpMethod()}.
     *
     * @return method specific http method
     * @see org.codegist.crest.config.MethodConfig#getPath()
     * @see org.codegist.crest.annotate.RestApi#methodsHttpMethod()
     * @see org.codegist.crest.HttpMethod
     */
    String method() default Fallbacks.FALLBACK_STRING;

    /**
     * Socket read timeout in milliseconds specific to this service call, overrides the {@link org.codegist.crest.annotate.RestApi#methodsSocketTimeout()} configuration.
     * <p>Defaults fallback to {@link org.codegist.crest.annotate.RestApi#methodsSocketTimeout()}.
     *
     * @return method specific socket read timeout
     * @see org.codegist.crest.config.MethodConfig#getSocketTimeout()
     * @see org.codegist.crest.annotate.RestApi#methodsSocketTimeout()
     */
    long socketTimeout() default Fallbacks.FALLBACK_LONG;

    /**
     * Connection timeout in milliseconds specific to this service call, overrides the {@link org.codegist.crest.annotate.RestApi#methodsConnectionTimeout()} configuration.
     * <p>Defaults fallback to {@link org.codegist.crest.annotate.RestApi#methodsConnectionTimeout()}.
     *
     * @return method specific connection timeout
     * @see org.codegist.crest.config.MethodConfig#getConnectionTimeout()
     * @see org.codegist.crest.annotate.RestApi#methodsConnectionTimeout()
     */
    long connectionTimeout() default Fallbacks.FALLBACK_LONG;

    /**
     * Interceptor specific to this method, overrides the {@link org.codegist.crest.annotate.RestApi#methodsRequestInterceptor()} configuration.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#methodsRequestInterceptor()}.
     *
     * @return method specific request interceptor
     * @see org.codegist.crest.config.MethodConfig#getRequestInterceptor()
     * @see org.codegist.crest.interceptor.RequestInterceptor
     * @see org.codegist.crest.annotate.RestApi#methodsRequestInterceptor()
     */
    Class<? extends RequestInterceptor> requestInterceptor() default Fallbacks.FallbackMethodInterceptor.class;

    /**
     * Response handler specific to this method, overrides the {@link org.codegist.crest.annotate.RestApi#methodsResponseHandler()} configuration.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#methodsResponseHandler()}.
     *
     * @return method specific response handler
     * @see org.codegist.crest.config.MethodConfig#getResponseHandler()
     * @see org.codegist.crest.ResponseHandler
     * @see org.codegist.crest.annotate.RestApi#methodsResponseHandler()
     */
    Class<? extends ResponseHandler> responseHandler() default Fallbacks.FallbackResponseHandler.class;

    /**
     * Error handler specific to this method, overrides the {@link org.codegist.crest.annotate.RestApi#methodsErrorHandler()} configuration.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#methodsErrorHandler()}.
     *
     * @return default method's error handler
     * @see org.codegist.crest.config.MethodConfig#getErrorHandler()
     * @see org.codegist.crest.ErrorHandler
     * @see org.codegist.crest.annotate.RestApi#methodsErrorHandler()
     */
    Class<? extends ErrorHandler> errorHandler() default Fallbacks.FallbackErrorHandler.class;


    /**
     * Param name fallback specific to these method arguments, overrides the {@link org.codegist.crest.annotate.RestApi#paramsName()} configuration.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestParam#name()}.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#paramsName()}.
     *
     * @return method specific param name fallback
     * @see org.codegist.crest.annotate.RestApi#paramsName()
     * @see org.codegist.crest.annotate.RestParam#name()
     */
    String paramsName() default Fallbacks.FALLBACK_STRING;

    /**
     * Param destination fallback specific to these method arguments, overrides the {@link org.codegist.crest.annotate.RestApi#paramsDestination()} configuration.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestParam#destination()}.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#paramsDestination()}.
     *
     * @return method specific destination fallback
     * @see org.codegist.crest.annotate.RestApi#paramsDestination()
     * @see org.codegist.crest.annotate.RestParam#destination()
     */
    String paramsDestination() default Fallbacks.FALLBACK_STRING;

    /**
     * Param serializer fallback specific to these method arguments, overrides the {@link org.codegist.crest.annotate.RestApi#paramsSerializer()} configuration.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestParam#serializer()}.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#paramsSerializer()}.
     *
     * @return method specific param serializer fallback
     * @see org.codegist.crest.serializer.Serializer
     * @see org.codegist.crest.annotate.RestApi#paramsSerializer()
     * @see org.codegist.crest.annotate.RestParam#serializer()
     */
    Class<? extends Serializer> paramsSerializer() default Fallbacks.FallbackSerializer.class;

    /**
     * Param injector fallback specific to these method arguments, overrides the {@link org.codegist.crest.annotate.RestApi#paramsInjector()} configuration.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestParam#injector()}.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestApi#paramsInjector()}.
     *
     * @return method specific param injector fallback
     * @see org.codegist.crest.injector.RequestInjector
     * @see org.codegist.crest.annotate.RestApi#paramsInjector()
     * @see org.codegist.crest.annotate.RestParam#injector()
     */
    Class<? extends RequestInjector> paramsInjector() default Fallbacks.FallbackRequestParameterInjector.class;


}

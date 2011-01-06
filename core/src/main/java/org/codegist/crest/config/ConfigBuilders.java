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

import org.codegist.common.collect.Arrays;
import org.codegist.common.collect.Maps;
import org.codegist.common.lang.Objects;
import org.codegist.common.lang.Strings;
import org.codegist.crest.CRestException;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.HttpMethod;
import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;
import org.codegist.crest.serializer.Serializers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handy builders for {@link org.codegist.crest.config.DefaultInterfaceConfig}.
 * <p>Support auto empty/null ignore and defaults methods and params values at respectively interface and method levels.
 * <p> Eg :
 * <code><pre>
 * InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(MyInterface.class, "http://local")
 *                                       .setMethodsConnectionTimeout(20) // overall connection timeout, method configs will inherit it if not set.
 *                                       .setMethodsSocketTimeout(20) // overall socket timeout, method configs will inherit it if not set.
 *                                       .setMethodsHttpMethod("PUT") // overall http method, method configs will inherit it if not set.
 *                                       (...) more interface config
 *                                       .startMethodConfig(MyInterface.class.getDeclaredMethod(....))
 *                                           .setHttpMethod("POST")  // Method config specific http method.
 *                                           (...) more method config
 *                                           .startParamConfig(0)
 *                                               (...) more param config
 *                                           .endParamConfig()
 *                                       .endMethodConfig()
 *                                       .build();
 * // Any non specified property will be defaulted to the respective default value taken from {@link org.codegist.crest.config.InterfaceConfig},{@link MethodConfig},{@link ParamConfig}
 * </pre></code>
 *
 * @see org.codegist.crest.config.DefaultInterfaceConfig
 * @see DefaultMethodConfig
 * @see DefaultParamConfig
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public abstract class ConfigBuilders {

    @SuppressWarnings("unchecked")
    public static class InterfaceConfigBuilder extends ConfigBuilders {
        private final Class interfaze;
        private final String endPoint;
        private final Map<Method, MethodConfigBuilder> builderCache;
        private String contextPath;
        private String encoding;
        private RequestInterceptor globalInterceptor;

        /**
         * <p>This will create an unbound builder, eg to attached to any interface, thus it cannot contains any method configuration.
         */
        public InterfaceConfigBuilder() {
            this(null, null);
        }

        /**
         * Given properties map can contains user-defined default values, that override interface predefined defauts.
         * <p>This will create an unbound builder, eg to attached to any interface, thus it cannot contains any method configuration.
         * @param customProperties default values holder
         */
        public InterfaceConfigBuilder(Map<String, Object> customProperties) {
            this(null, null, customProperties);
        }

        public InterfaceConfigBuilder(Class interfaze, String endPoint) {
            this(interfaze, endPoint, null);
        }

        /**
         * Given properties map can contains user-defined default values, that override interface predefined defauts.
         * @param interfaze interface to bind the config to
         * @param endPoint endpoint
         * @param customProperties default values holder
         */
        public InterfaceConfigBuilder(Class interfaze, String endPoint, Map<String, Object> customProperties) {
            super(customProperties);
            this.interfaze = interfaze;
            this.endPoint = endPoint;
            this.builderCache = new HashMap<Method, MethodConfigBuilder>();
            if (interfaze != null)
                for (Method m : interfaze.getDeclaredMethods()) {
                    this.builderCache.put(m, new MethodConfigBuilder(this, m, customProperties));
                }
        }

        @Override
        public InterfaceConfigBuilder setIgnoreNullOrEmptyValues(boolean ignoreNullOrEmptyValues) {
            return (InterfaceConfigBuilder) super.setIgnoreNullOrEmptyValues(ignoreNullOrEmptyValues);
        }

        public DefaultInterfaceConfig build() {
            return build(true);
        }

        public DefaultInterfaceConfig buildOverrideTemplate()  {
            return build(false);
        }

        public DefaultInterfaceConfig build(boolean useDefaults) {
            Map<Method, MethodConfig> mConfig = new HashMap<Method, MethodConfig>();
            for (Map.Entry<Method, MethodConfigBuilder> entry : builderCache.entrySet()) {
                mConfig.put(entry.getKey(), entry.getValue().build(useDefaults));
            }
            if (useDefaults) {
                contextPath = defaultIfUndefined(contextPath, CRestProperty.CONFIG_INTERFACE_DEFAULT_CONTEXT_PATH, InterfaceConfig.DEFAULT_CONTEXT_PATH);
                encoding = defaultIfUndefined(encoding, CRestProperty.CONFIG_INTERFACE_DEFAULT_ENCODING, InterfaceConfig.DEFAULT_ENCODING);
                globalInterceptor = defaultIfUndefined(globalInterceptor, CRestProperty.CONFIG_INTERFACE_DEFAULT_GLOBAL_INTERCEPTOR, newInstance(InterfaceConfig.DEFAULT_GLOBAL_INTERCEPTOR));
            }
            return new DefaultInterfaceConfig(
                    interfaze,
                    endPoint,
                    contextPath,
                    encoding,
                    globalInterceptor,
                    mConfig
            );
        }


        public MethodConfigBuilder startMethodConfig(Method meth) {
            return this.builderCache.get(meth);
        }

        public InterfaceConfigBuilder setContextPath(String contextPath) {
            if (ignore(contextPath)) return this;
            this.contextPath = contextPath;
            return this;
        }

        public InterfaceConfigBuilder setEncoding(String encoding) {
            if (ignore(encoding)) return this;
            this.encoding = encoding;
            return this;
        }

        public InterfaceConfigBuilder setGlobalInterceptor(RequestInterceptor requestInterceptor) {
            if (ignore(requestInterceptor)) return this;
            this.globalInterceptor = requestInterceptor;
            return this;
        }

        public InterfaceConfigBuilder setGlobalInterceptor(String interceptorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(interceptorClassName)) return this;
            return setGlobalInterceptor((Class<? extends RequestInterceptor>) Class.forName(interceptorClassName));
        }

        public InterfaceConfigBuilder setGlobalInterceptor(Class<? extends RequestInterceptor> interceptorCls) throws IllegalAccessException, InstantiationException {
            if (ignore(interceptorCls)) return this;
            return setGlobalInterceptor(newInstance(interceptorCls));
        }

        public InterfaceConfigBuilder setMethodsSocketTimeout(Long socketTimeout) {
            if (ignore(socketTimeout)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setSocketTimeout(socketTimeout);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsSocketTimeout(String socketTimeout) {
            if (ignore(socketTimeout)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setSocketTimeout(socketTimeout);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsConnectionTimeout(Long connectionTimeout) {
            if (ignore(connectionTimeout)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setConnectionTimeout(connectionTimeout);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsConnectionTimeout(String connectionTimeout) {
            if (ignore(connectionTimeout)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setConnectionTimeout(connectionTimeout);
            }
            return this;
        }     

        public InterfaceConfigBuilder addMethodsStaticParam(String name, String value, Destination destination){
            if (ignore(name, value, destination)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.addStaticParam(name, value, destination);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsSerializer(Serializer paramSerializer) {
            if (ignore(paramSerializer)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsSerializer(paramSerializer);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsSerializer(String paramSerializerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(paramSerializerClassName)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsSerializer(paramSerializerClassName);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsSerializer(Class<? extends Serializer> paramSerializerCls) throws IllegalAccessException, InstantiationException {
            if (ignore(paramSerializerCls)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsSerializer(paramSerializerCls);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsInjector(Injector injector) {
            if (ignore(injector)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsInjector(injector);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsInjector(String injectorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(injectorClassName)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsInjector(injectorClassName);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsInjector(Class<? extends Injector> injectorCls) throws IllegalAccessException, InstantiationException {
            if (ignore(injectorCls)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsInjector(injectorCls);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsName(String paramName) {
            if (ignore(paramName)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsName(paramName);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsDestination(Destination paramDestination) {
            if (ignore(paramDestination)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsDestination(paramDestination);
            }
            return this;
        }

        public InterfaceConfigBuilder setParamsDestination(String destination) {
            if (ignore(destination)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setParamsDestination(destination);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsRequestInterceptor(RequestInterceptor requestInterceptor) {
            if (ignore(requestInterceptor)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setRequestInterceptor(requestInterceptor);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsRequestInterceptor(String requestInterceptorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(requestInterceptorClassName)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setRequestInterceptor(requestInterceptorClassName);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsRequestInterceptor(Class<? extends RequestInterceptor> requestInterceptorCls) throws IllegalAccessException, InstantiationException {
            if (ignore(requestInterceptorCls)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setRequestInterceptor(requestInterceptorCls);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsResponseHandler(ResponseHandler responseHandler) {
            if (ignore(responseHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setResponseHandler(responseHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsResponseHandler(String responseHandlerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(responseHandlerClassName)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setResponseHandler(responseHandlerClassName);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsResponseHandler(Class<? extends ResponseHandler> responseHandlerClass) throws IllegalAccessException, InstantiationException {
            if (ignore(responseHandlerClass)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setResponseHandler(responseHandlerClass);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsErrorHandler(ErrorHandler errorHandler) {
            if (ignore(errorHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setErrorHandler(errorHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsErrorHandler(String errorHandler) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(errorHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setErrorHandler(errorHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsErrorHandler(Class<? extends ErrorHandler> errorHandler) throws IllegalAccessException, InstantiationException {
            if (ignore(errorHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setErrorHandler(errorHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsRetryHandler(RetryHandler retryHandler) {
            if (ignore(retryHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setRetryHandler(retryHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsRetryHandler(String retryHandler) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(retryHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setRetryHandler(retryHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsRetryHandler(Class<? extends RetryHandler> retryHandler) throws IllegalAccessException, InstantiationException {
            if (ignore(retryHandler)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setRetryHandler(retryHandler);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsPath(String path) {
            if (ignore(path)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setPath(path);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsHttpMethod(HttpMethod meth) {
            if (ignore(meth)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setHttpMethod(meth);
            }
            return this;
        }

        public InterfaceConfigBuilder setMethodsHttpMethod(String meth) {
            if (ignore(meth)) return this;
            for (MethodConfigBuilder b : builderCache.values()) {
                b.setHttpMethod(meth);
            }
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    public static class MethodConfigBuilder extends ConfigBuilders {
        private final Method method;
        private final ParamConfigBuilder[] paramConfigBuilders;
        private final InterfaceConfigBuilder parent;

        private String path;
        private HttpMethod meth;
        private Map<String,StaticParam> params = new LinkedHashMap<String, StaticParam>();
        private Long socketTimeout;
        private Long connectionTimeout;
        private RequestInterceptor requestInterceptor;
        private ResponseHandler responseHandler;
        private ErrorHandler errorHandler;
        private RetryHandler retryHandler;

        public MethodConfigBuilder(Method method) {
            this(method, null);
        }

        /**
         * Given properties map can contains user-defined default values, that override interface predefined defauts.
         * @param method method being configured
         * @param customProperties default values holder
         */
        public MethodConfigBuilder(Method method, Map<String, Object> customProperties) {
            this(null, method, customProperties);
        }

        private MethodConfigBuilder(InterfaceConfigBuilder parent, Method method) {
            this(parent, method, null);
        }

        private MethodConfigBuilder(InterfaceConfigBuilder parent, Method method, Map<String, Object> customProperties) {
            super(customProperties);
            this.parent = parent;
            this.method = method;
            this.paramConfigBuilders = new ParamConfigBuilder[method.getParameterTypes().length];
            for (int i = 0; i < this.paramConfigBuilders.length; i++) {
                this.paramConfigBuilders[i] = new ParamConfigBuilder(this, method.getGenericParameterTypes()[i], customProperties);
            }
        }


        public MethodConfig build() {
            return build(true);
        }

        public MethodConfig buildOverrideTemplate() {
            return build(false);
        }

        public MethodConfig build(boolean useDefaults) {
            ParamConfig[] pConfig = new ParamConfig[paramConfigBuilders.length];
            for (int i = 0; i < paramConfigBuilders.length; i++) {
                pConfig[i] = this.paramConfigBuilders[i].build(useDefaults);
            }
            StaticParam[] defaultParams = params != null && params.size() > 0 ? params.values().toArray(new StaticParam[params.size()]) : null;
            if (useDefaults) {
                path = defaultIfUndefined(path, CRestProperty.CONFIG_METHOD_DEFAULT_PATH, MethodConfig.DEFAULT_PATH);
                meth = defaultIfUndefined(meth, CRestProperty.CONFIG_METHOD_DEFAULT_HTTP_METHOD, MethodConfig.DEFAULT_HTTP_METHOD);
                StaticParam[] defs = defaultIfUndefined(null, CRestProperty.CONFIG_METHOD_DEFAULT_PARAMS, MethodConfig.DEFAULT_PARAMS);
                defaultParams = Arrays.merge(StaticParam.class, defaultParams, defs);
                socketTimeout = defaultIfUndefined(socketTimeout, CRestProperty.CONFIG_METHOD_DEFAULT_SO_TIMEOUT, MethodConfig.DEFAULT_SO_TIMEOUT);
                connectionTimeout = defaultIfUndefined(connectionTimeout, CRestProperty.CONFIG_METHOD_DEFAULT_CO_TIMEOUT, MethodConfig.DEFAULT_CO_TIMEOUT);
                requestInterceptor = defaultIfUndefined(requestInterceptor, CRestProperty.CONFIG_METHOD_DEFAULT_REQUEST_INTERCEPTOR, newInstance(MethodConfig.DEFAULT_REQUEST_INTERCEPTOR));
                responseHandler = defaultIfUndefined(responseHandler, CRestProperty.CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER, newInstance(MethodConfig.DEFAULT_RESPONSE_HANDLER));
                errorHandler = defaultIfUndefined(errorHandler, CRestProperty.CONFIG_METHOD_DEFAULT_ERROR_HANDLER, newInstance(MethodConfig.DEFAULT_ERROR_HANDLER));
                retryHandler = defaultIfUndefined(retryHandler, CRestProperty.CONFIG_METHOD_DEFAULT_RETRY_HANDLER, newInstance(MethodConfig.DEFAULT_RETRY_HANDLER));
            }
            return new DefaultMethodConfig(
                    method,
                    path,
                    defaultParams,
                    meth,
                    socketTimeout,
                    connectionTimeout,
                    requestInterceptor,
                    responseHandler,
                    errorHandler,
                    retryHandler,
                    pConfig
            );
        }

        public InterfaceConfigBuilder endMethodConfig() {
            return parent;
        }

        @Override
        public MethodConfigBuilder setIgnoreNullOrEmptyValues(boolean ignoreNullOrEmptyValues) {
            return (MethodConfigBuilder) super.setIgnoreNullOrEmptyValues(ignoreNullOrEmptyValues);
        }

        public ParamConfigBuilder startParamConfig(int index) {
            return paramConfigBuilders[index];
        }

        public MethodConfigBuilder setPath(String path) {
            if (ignore(path)) return this;
            this.path = path;
            return this;
        }

        public MethodConfigBuilder addStaticParam(String name, String value, Destination destination){
            if (ignore(name, value, destination)) return this;
            this.params.put(name, new DefaultStaticParam(name, value, destination));
            return this;
        }

        public MethodConfigBuilder setHttpMethod(String meth) {
            if (ignore(meth)) return this;
            return setHttpMethod(HttpMethod.valueOf(meth));
        }

        public MethodConfigBuilder setHttpMethod(HttpMethod meth) {
            if (ignore(meth)) return this;
            this.meth = meth;
            return this;
        }

        public MethodConfigBuilder setSocketTimeout(Long socketTimeout) {
            if (ignore(socketTimeout)) return this;
            this.socketTimeout = socketTimeout;
            return this;
        }

        public MethodConfigBuilder setSocketTimeout(String socketTimeout) {
            if (ignore(socketTimeout)) return this;
            return setSocketTimeout(Long.parseLong(socketTimeout));
        }

        public MethodConfigBuilder setConnectionTimeout(Long connectionTimeout) {
            if (ignore(connectionTimeout)) return this;
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public MethodConfigBuilder setConnectionTimeout(String connectionTimeout) {
            if (ignore(connectionTimeout)) return this;
            return setConnectionTimeout(Long.parseLong(connectionTimeout));
        }

        public MethodConfigBuilder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            if (ignore(requestInterceptor)) return this;
            this.requestInterceptor = requestInterceptor;
            return this;
        }

        public MethodConfigBuilder setRequestInterceptor(String interceptorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(interceptorClassName)) return this;
            return setRequestInterceptor((Class<? extends RequestInterceptor>) Class.forName(interceptorClassName));
        }

        public MethodConfigBuilder setRequestInterceptor(Class<? extends RequestInterceptor> interceptorCls) throws IllegalAccessException, InstantiationException {
            if (ignore(interceptorCls)) return this;
            return setRequestInterceptor(newInstance(interceptorCls));
        }

        public MethodConfigBuilder setResponseHandler(ResponseHandler responseHandler) {
            if (ignore(responseHandler)) return this;
            this.responseHandler = responseHandler;
            return this;
        }

        public MethodConfigBuilder setResponseHandler(String responseHandlerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(responseHandlerClassName)) return this;
            return setResponseHandler((Class<? extends ResponseHandler>) Class.forName(responseHandlerClassName));
        }

        public MethodConfigBuilder setResponseHandler(Class<? extends ResponseHandler> responseHandlerClass) throws IllegalAccessException, InstantiationException {
            if (ignore(responseHandlerClass)) return this;
            return setResponseHandler(newInstance(responseHandlerClass));
        }


        public MethodConfigBuilder setErrorHandler(ErrorHandler errorHandler) {
            if (ignore(errorHandler)) return this;
            this.errorHandler = errorHandler;
            return this;
        }

        public MethodConfigBuilder setErrorHandler(String methodHandlerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(methodHandlerClassName)) return this;
            return setErrorHandler((Class<? extends ErrorHandler>) Class.forName(methodHandlerClassName));
        }

        public MethodConfigBuilder setErrorHandler(Class<? extends ErrorHandler> methodHandlerClass) throws IllegalAccessException, InstantiationException {
            if (ignore(methodHandlerClass)) return this;
            return setErrorHandler(newInstance(methodHandlerClass));
        }


        public MethodConfigBuilder setRetryHandler(RetryHandler retryHandler) {
            if (ignore(retryHandler)) return this;
            this.retryHandler = retryHandler;
            return this;
        }

        public MethodConfigBuilder setRetryHandler(String retryHandlerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(retryHandlerClassName)) return this;
            return setRetryHandler((Class<? extends RetryHandler>) Class.forName(retryHandlerClassName));
        }

        public MethodConfigBuilder setRetryHandler(Class<? extends RetryHandler> retryHandlerClass) throws IllegalAccessException, InstantiationException {
            if (ignore(retryHandlerClass)) return this;
            return setRetryHandler(newInstance(retryHandlerClass));
        }


        public MethodConfigBuilder setParamsSerializer(Serializer paramSerializer) {
            if (ignore(paramSerializer)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setSerializer(paramSerializer);
            }
            return this;
        }

        public MethodConfigBuilder setParamsSerializer(String paramSerializerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(paramSerializerClassName)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setSerializer(paramSerializerClassName);
            }
            return this;
        }

        public MethodConfigBuilder setParamsSerializer(Class<? extends Serializer> paramSerializer) throws IllegalAccessException, InstantiationException {
            if (ignore(paramSerializer)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setSerializer(paramSerializer);
            }
            return this;
        }

        public MethodConfigBuilder setParamsInjector(Injector injector) {
            if (ignore(injector)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setInjector(injector);
            }
            return this;
        }

        public MethodConfigBuilder setParamsInjector(String injectorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(injectorClassName)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setInjector(injectorClassName);
            }
            return this;
        }

        public MethodConfigBuilder setParamsInjector(Class<? extends Injector> injectorCls) throws IllegalAccessException, InstantiationException {
            if (ignore(injectorCls)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setInjector(injectorCls);
            }
            return this;
        }

        public MethodConfigBuilder setParamsName(String paramName) {
            if (ignore(paramName)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setName(paramName);
            }
            return this;
        }

        public MethodConfigBuilder setParamsDestination(String destination) {
            if (ignore(destination)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setDestination(destination);
            }
            return this;
        }

        public MethodConfigBuilder setParamsDestination(Destination paramDestination) {
            if (ignore(paramDestination)) return this;
            for (ParamConfigBuilder b : paramConfigBuilders) {
                b.setDestination(paramDestination);
            }
            return this;
        }

    }

    @SuppressWarnings("unchecked")
    public static class ParamConfigBuilder extends ConfigBuilders {
        private final MethodConfigBuilder parent;
        private final Type type;
        private String name;
        private Destination dest;
        private Serializer serializer;
        private Injector injector;


        /**
         * Given properties map can contains user-defined default values, that override interface predefined defauts.
         * @param customProperties default values holder
         */
        public ParamConfigBuilder(Type type, Map<String, Object> customProperties) {
            this(null, type, customProperties);
        }

        private ParamConfigBuilder(MethodConfigBuilder parent, Type type) {
            this(parent, type, null);
        }

        private ParamConfigBuilder(MethodConfigBuilder parent, Type type, Map<String, Object> customProperties) {
            super(customProperties);
            this.type = type;
            this.parent = parent;
        }

        public DefaultParamConfig build() {
            return build(true);
        }

        public DefaultParamConfig buildOverrideTemplate() {
            return build(false);
        }

        public DefaultParamConfig build(boolean useDefaults) {
            if (useDefaults) {
                name = defaultIfUndefined(name, CRestProperty.CONFIG_PARAM_DEFAULT_NAME, ParamConfig.DEFAULT_NAME);
                dest = defaultIfUndefined(dest, CRestProperty.CONFIG_PARAM_DEFAULT_DESTINATION, ParamConfig.DEFAULT_DESTINATION);
                injector = defaultIfUndefined(injector, CRestProperty.CONFIG_PARAM_DEFAULT_INJECTOR, newInstance(ParamConfig.DEFAULT_INJECTOR));
                serializer = defaultIfUndefined(serializer, CRestProperty.CONFIG_PARAM_DEFAULT_SERIALIZER, newInstance(ParamConfig.DEFAULT_SERIALIZER));

                if(serializer == null) {
                    // if null, then choose which serializer to apply using default rules
                    serializer = Serializers.getFor(customProperties, type);
                }
            }
            return new DefaultParamConfig(
                    name,
                    dest,
                    serializer,
                    injector
            );
        }

        public MethodConfigBuilder endParamConfig() {
            return parent;
        }

        @Override
        public ParamConfigBuilder setIgnoreNullOrEmptyValues(boolean ignoreNullOrEmptyValues) {
            return (ParamConfigBuilder) super.setIgnoreNullOrEmptyValues(ignoreNullOrEmptyValues);
        }

        public ParamConfigBuilder setName(String name) {
            if (ignore(name)) return this;
            this.name = name;
            return this;
        }

        public ParamConfigBuilder setDestination(String dest) {
            if (ignore(dest)) return this;
            return setDestination(Destination.valueOf(dest));
        }

        public ParamConfigBuilder setDestination(Destination dest) {
            if (ignore(dest)) return this;
            this.dest = dest;
            return this;
        }

        /**
         * Sets the argument's serializer. If not set, the system automatically choose a serializer based on the argument type. See {@link org.codegist.crest.CRest} for the selection rules.
         * @param serializer the serializer to use for this argument
         * @return current builder
         */
        public ParamConfigBuilder setSerializer(Serializer serializer) {
            if (ignore(serializer)) return this;
            this.serializer = serializer;
            return this;
        }

        /**
         * Sets the argument's serializer. If not set, the system automatically choose a serializer based on the argument type. See {@link org.codegist.crest.CRest} for the selection rules.
         * @param serializerClassName the serializer classname to use for this argument
         * @return current builder
         */
        public ParamConfigBuilder setSerializer(String serializerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(serializerClassName)) return this;
            return setSerializer((Class<? extends Serializer>) Class.forName(serializerClassName));
        }

        /**
         * Sets the argument's serializer. If not set, the system automatically choose a serializer based on the argument type. See {@link org.codegist.crest.CRest} for the selection rules.
         * @param serializer the serializer to use for this argument
         * @return current builder
         */
        public ParamConfigBuilder setSerializer(Class<? extends Serializer> serializer) throws IllegalAccessException, InstantiationException {
            if (ignore(serializer)) return this;
            return setSerializer(newInstance(serializer));
        }

        public ParamConfigBuilder setInjector(Injector injector) {
            if (ignore(injector)) return this;
            this.injector = injector;
            return this;
        }

        public ParamConfigBuilder setInjector(String injectorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(injectorClassName)) return this;
            return setInjector((Class<? extends Injector>) Class.forName(injectorClassName));
        }

        public ParamConfigBuilder setInjector(Class<? extends Injector> injector) throws IllegalAccessException, InstantiationException {
            if (ignore(injector)) return this;
            return setInjector(newInstance(injector));
        }
    }

    protected final Map<String, Object> customProperties;
    private boolean ignoreNullOrEmptyValues;

    private ConfigBuilders(Map<String, Object> customProperties) {
        this.customProperties = Maps.unmodifiable(customProperties);
    }

    <T> T defaultIfUndefined(T value, String defProp, T def) {
        if (def instanceof String) {
            String defs = Strings.defaultIfBlank((String) customProperties.get(defProp), (String) def);
            return (T) Strings.defaultIfBlank((String) value, defs);
        } else {
            def = Objects.defaultIfNull((T) customProperties.get(defProp), def);
            return Objects.defaultIfNull(value, def);
        }
    }

    ConfigBuilders setIgnoreNullOrEmptyValues(boolean ignoreNullOrEmptyValues) {
        this.ignoreNullOrEmptyValues = ignoreNullOrEmptyValues;
        return this;
    }

    <T> T newInstance(Class<T> clazz) {
        if(clazz == null) return null;
        try {
            return newInstance(clazz.getConstructor(Map.class), customProperties);
        } catch (CRestException e) {
            throw e;
        } catch (Exception e) {
            try {
                return newInstance(clazz.getConstructor());
            } catch (Exception e1) {
                throw new CRestException(e1);
            }
        }
    }
    private  <T> T newInstance(Constructor<T> constructor, Object... args) throws Exception {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw new CRestException(e.getCause());
        }
    }

    boolean ignore(Object... values) {
        for(Object v : values){
            if(ignore(v)) return true;
        }
        return false;
    }
    boolean ignore(Object value) {
        if (!ignoreNullOrEmptyValues) return false;
        return (value == null || (value instanceof String && Strings.isBlank((String)value)));
    }

}
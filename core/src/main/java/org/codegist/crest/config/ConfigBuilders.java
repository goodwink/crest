package org.codegist.crest.config;

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.Objects;
import org.codegist.common.lang.Strings;
import org.codegist.crest.ErrorHandler;
import org.codegist.crest.HttpMethod;
import org.codegist.crest.ResponseHandler;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;

import java.lang.reflect.Method;
import java.util.HashMap;
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
 */
public abstract class ConfigBuilders {

    @SuppressWarnings("unchecked")
    public static class InterfaceConfigBuilder extends ConfigBuilders {
        private final Class interfaze;
        private final String server;
        private final Map<Method, MethodConfigBuilder> builderCache;
        private String path;
        private String encoding;
        private RequestInterceptor requestInterceptor;

        public InterfaceConfigBuilder() {
            this(null, null);
        }

        public InterfaceConfigBuilder(Map<String, Object> customProperties) {
            this(null, null, customProperties);
        }

        public InterfaceConfigBuilder(Class interfaze, String server) {
            this(interfaze, server, null);
        }

        public InterfaceConfigBuilder(Class interfaze, String server, Map<String, Object> customProperties) {
            super(customProperties);
            this.interfaze = interfaze;
            this.server = server;
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

        public DefaultInterfaceConfig buildOverrideTemplate() {
            return build(false);
        }

        public DefaultInterfaceConfig build(boolean useDefaults) {
            Map<Method, MethodConfig> mConfig = new HashMap<Method, MethodConfig>();
            for (Map.Entry<Method, MethodConfigBuilder> entry : builderCache.entrySet()) {
                mConfig.put(entry.getKey(), entry.getValue().build(useDefaults));
            }
            if (useDefaults) {
                path = defaultIfUndefined(path, InterfaceConfig.DEFAULT_PATH_PROP, InterfaceConfig.DEFAULT_PATH);
                encoding = defaultIfUndefined(encoding, InterfaceConfig.DEFAULT_ENCODING_PROP, InterfaceConfig.DEFAULT_ENCODING);
                requestInterceptor = defaultIfUndefined(requestInterceptor, InterfaceConfig.DEFAULT_REQUEST_INTERCEPTOR_PROP, InterfaceConfig.DEFAULT_REQUEST_INTERCEPTOR);
            }
            return new DefaultInterfaceConfig(
                    interfaze,
                    server,
                    path,
                    encoding,
                    requestInterceptor,
                    mConfig
            );
        }


        public MethodConfigBuilder startMethodConfig(Method meth) {
            return this.builderCache.get(meth);
        }

        public InterfaceConfigBuilder setPath(String path) {
            if (ignore(path)) return this;
            this.path = path;
            return this;
        }

        public InterfaceConfigBuilder setEncoding(String encoding) {
            if (ignore(encoding)) return this;
            this.encoding = encoding;
            return this;
        }

        public InterfaceConfigBuilder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            if (ignore(requestInterceptor)) return this;
            this.requestInterceptor = requestInterceptor;
            return this;
        }

        public InterfaceConfigBuilder setRequestInterceptor(String interceptorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(interceptorClassName)) return this;
            return setRequestInterceptor((Class<? extends RequestInterceptor>) Class.forName(interceptorClassName));
        }

        public InterfaceConfigBuilder setRequestInterceptor(Class<? extends RequestInterceptor> interceptorCls) throws IllegalAccessException, InstantiationException {
            if (ignore(interceptorCls)) return this;
            return setRequestInterceptor(interceptorCls.newInstance());
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

        public InterfaceConfigBuilder setParamsInjector(RequestInjector injector) {
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

        public InterfaceConfigBuilder setParamsInjector(Class<? extends RequestInjector> injectorCls) throws IllegalAccessException, InstantiationException {
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
        private Long socketTimeout;
        private Long connectionTimeout;
        private RequestInterceptor requestInterceptor;
        private ResponseHandler responseHandler;
        private ErrorHandler errorHandler;

        public MethodConfigBuilder(Method method) {
            this(method, null);
        }

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
                this.paramConfigBuilders[i] = new ParamConfigBuilder(this, customProperties);
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
            if (useDefaults) {
                path = defaultIfUndefined(path, MethodConfig.DEFAULT_PATH_PROP, MethodConfig.DEFAULT_PATH);
                meth = defaultIfUndefined(meth, MethodConfig.DEFAULT_HTTP_METHOD_PROP, MethodConfig.DEFAULT_HTTP_METHOD);
                socketTimeout = defaultIfUndefined(socketTimeout, MethodConfig.DEFAULT_SO_TIMEOUT_PROP, MethodConfig.DEFAULT_SO_TIMEOUT);
                connectionTimeout = defaultIfUndefined(connectionTimeout, MethodConfig.DEFAULT_CO_TIMEOUT_PROP, MethodConfig.DEFAULT_CO_TIMEOUT);
                requestInterceptor = defaultIfUndefined(requestInterceptor, MethodConfig.DEFAULT_REQUEST_INTERCEPTOR_PROP, MethodConfig.DEFAULT_REQUEST_INTERCEPTOR);
                responseHandler = defaultIfUndefined(responseHandler, MethodConfig.DEFAULT_RESPONSE_HANDLER_PROP, MethodConfig.DEFAULT_RESPONSE_HANDLER);
                errorHandler = defaultIfUndefined(errorHandler, MethodConfig.DEFAULT_ERROR_HANDLER_PROP, MethodConfig.DEFAULT_ERROR_HANDLER);
            }
            return new DefaultMethodConfig(
                    method,
                    path,
                    meth,
                    socketTimeout,
                    connectionTimeout,
                    requestInterceptor,
                    responseHandler,
                    errorHandler,
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
            return setRequestInterceptor(interceptorCls.newInstance());
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
            return setResponseHandler(responseHandlerClass.newInstance());
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
            return setErrorHandler(methodHandlerClass.newInstance());
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

        public MethodConfigBuilder setParamsInjector(RequestInjector injector) {
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

        public MethodConfigBuilder setParamsInjector(Class<? extends RequestInjector> injectorCls) throws IllegalAccessException, InstantiationException {
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
        private String name;
        private Destination dest;
        private Serializer serializer;
        private RequestInjector injector;


        public ParamConfigBuilder() {
            this(null, null);
        }

        public ParamConfigBuilder(Map<String, Object> customProperties) {
            this(null, customProperties);
        }

        private ParamConfigBuilder(MethodConfigBuilder parent) {
            this(parent, null);
        }

        private ParamConfigBuilder(MethodConfigBuilder parent, Map<String, Object> customProperties) {
            super(customProperties);
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
                name = defaultIfUndefined(name, ParamConfig.DEFAULT_NAME_PROP, ParamConfig.DEFAULT_NAME);
                dest = defaultIfUndefined(dest, ParamConfig.DEFAULT_DESTINATION_PROP, ParamConfig.DEFAULT_DESTINATION);
                serializer = defaultIfUndefined(serializer, ParamConfig.DEFAULT_SERIALIZER_PROP, (ParamConfig.DEFAULT_SERIALIZER));
                injector = defaultIfUndefined(injector, ParamConfig.DEFAULT_INJECTOR_PROP, (ParamConfig.DEFAULT_INJECTOR));
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

        public ParamConfigBuilder setSerializer(Serializer serializer) {
            if (ignore(serializer)) return this;
            this.serializer = serializer;
            return this;
        }

        public ParamConfigBuilder setSerializer(String serializerClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(serializerClassName)) return this;
            return setSerializer((Class<? extends Serializer>) Class.forName(serializerClassName));
        }

        public ParamConfigBuilder setSerializer(Class<? extends Serializer> serializer) throws IllegalAccessException, InstantiationException {
            if (ignore(serializer)) return this;
            return setSerializer(serializer.newInstance());
        }

        public ParamConfigBuilder setInjector(RequestInjector injector) {
            if (ignore(injector)) return this;
            this.injector = injector;
            return this;
        }

        public ParamConfigBuilder setInjector(String injectorClassName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            if (ignore(injectorClassName)) return this;
            return setInjector((Class<? extends RequestInjector>) Class.forName(injectorClassName));
        }

        public ParamConfigBuilder setInjector(Class<? extends RequestInjector> injector) throws IllegalAccessException, InstantiationException {
            if (ignore(injector)) return this;
            return setInjector(injector.newInstance());
        }
    }

    private final Map<String, ?> customProperties;
    private boolean ignoreNullOrEmptyValues;

    private ConfigBuilders(Map<String, ?> customProperties) {
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

    boolean ignore(Object value) {
        if (!ignoreNullOrEmptyValues) return false;
        return (value == null || (value instanceof String && ((String) value).trim().isEmpty()));
    }

}
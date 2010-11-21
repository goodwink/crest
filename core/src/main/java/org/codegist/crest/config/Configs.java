package org.codegist.crest.config;

import org.codegist.common.lang.Strings;
import org.codegist.crest.ErrorHandler;
import org.codegist.crest.HttpMethod;
import org.codegist.crest.ResponseHandler;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;

import java.lang.reflect.Method;

public final class Configs {
    private Configs() {
    }


    /**
     * Overrides and config (overrides) with another one (base).
     * <p>The override is a config template where nulls values are legals and will fallback to the base config. Base config must apply to the general contract of {@link org.codegist.crest.config.InterfaceConfig}.
     * <p>Any non-null values in override config will take priority over base config.
     * <p>RequestInterceptor are not overriding each other but are chaining, thus if either override and base configs declare a request interceptor, both of them will run, with the override's request interceptor running before the base one.
     * <p> The returned config is a dynamic view over the two given config, thus the two configs can change over time and the resulting config will reflect the changes.
     *
     * @param base      Normal full configured config, respect the general contract of InterfaceConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     * @see org.codegist.crest.config.InterfaceConfig
     */
    public static InterfaceConfig override(InterfaceConfig base, InterfaceConfig overrides) {
        if (overrides == null) return base;
        return new OverridingInterfaceConfig(base, overrides);// could be done by proxy
    }

    /**
     * Overrides and config (overrides) with another one (base).
     * <p>The override is a config template where nulls values are legals and will fallback to the base config. Base config must apply to the general contract of {@link org.codegist.crest.config.MethodConfig}.
     * <p>Any non-null values in override config will take priority over base config.
     * <p>RequestInterceptor are not overriding each other but are chaining, thus if either override and base configs declare a request interceptor, both of them will run, with the override's request interceptor running before the base one.
     * <p> The returned config is a dynamic view over the two given config, thus the two configs can change over time and the resulting config will reflect the changes.
     *
     * @param base      Normal full configured config, respect the general contract of MethodConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     * @see org.codegist.crest.config.MethodConfig
     */
    public static MethodConfig override(MethodConfig base, MethodConfig overrides) {
        if (overrides == null) return base;
        return new OverridingMethodConfig(base, overrides);// could be done by proxy
    }

    /**
     * Overrides and config (overrides) with another one (base).
     * <p>The override is a config template where nulls values are legals and will fallback to the base config. Base config must apply to the general contract of {@link org.codegist.crest.config.ParamConfig}.
     * <p>Any non-null values in override config will take priority over base config.
     * <p> The returned config is a dynamic view over the two given config, thus the two configs can change over time and the resulting config will reflect the changes.
     *
     * @param base      Normal full configured config, respect the general contract of ParamConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     * @see org.codegist.crest.config.ParamConfig
     */
    public static ParamConfig override(ParamConfig base, ParamConfig overrides) {
        if (overrides == null) return base;
        return new OverridingParamConfig(base, overrides); // could be done by proxy
    }

    @SuppressWarnings("unchecked")
    static Class<? extends RequestInjector> chooseInjector(Class<? extends RequestInjector> typeInjector, String interfaceInjectorClassName) throws ClassNotFoundException {
        if (Strings.isBlank(interfaceInjectorClassName)) return typeInjector;
        return chooseInjector(typeInjector, (Class<? extends RequestInjector>) Class.forName(interfaceInjectorClassName));
    }

    static Class<? extends RequestInjector> chooseInjector(Class<? extends RequestInjector> typeInjector, Class<? extends RequestInjector> interfaceInjector) {
        // interface injector takes priority over type injector
        return interfaceInjector != null ? interfaceInjector : typeInjector;
    }


    private static class OverridingParamConfig implements ParamConfig {
        private final ParamConfig base;
        private final ParamConfig override;

        private OverridingParamConfig(ParamConfig base, ParamConfig override) {
            this.base = base;
            this.override = override;
        }

        @Override
        public Serializer getSerializer() {
            return override.getSerializer() != null ? override.getSerializer() : base.getSerializer();
        }

        @Override
        public Destination getDestination() {
            return override.getDestination() != null ? override.getDestination() : base.getDestination();
        }

        @Override
        public String getName() {
            return override.getName() != null ? override.getName() : base.getName();
        }

        public RequestInjector getInjector() {
            return override.getInjector() != null ? override.getInjector() : base.getInjector();
        }
    }

    private static class OverridingMethodConfig implements MethodConfig {
        private final MethodConfig base;
        private final MethodConfig override;

        private OverridingMethodConfig(MethodConfig base, MethodConfig override) {
            this.base = base;
            this.override = override;
        }

        @Override
        public Integer getParamCount() {
            return override.getParamCount() != null ? override.getParamCount() : base.getParamCount();
        }

        @Override
        public ResponseHandler getResponseHandler() {
            return override.getResponseHandler() != null ? override.getResponseHandler() : base.getResponseHandler();
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return override.getErrorHandler() != null ? override.getErrorHandler() : base.getErrorHandler();
        }

        @Override
        public RequestInterceptor getRequestInterceptor() {
            if (override.getRequestInterceptor() == null) {
                return base.getRequestInterceptor();
            } else if (base.getRequestInterceptor() == null) {
                return override.getRequestInterceptor();
            } else {
                return new CompositeRequestInterceptor(override.getRequestInterceptor(), base.getRequestInterceptor());
            }
        }

        @Override
        public Long getSocketTimeout() {
            return override.getSocketTimeout() != null ? override.getSocketTimeout() : base.getSocketTimeout();
        }

        @Override
        public Long getConnectionTimeout() {
            return override.getConnectionTimeout() != null ? override.getConnectionTimeout() : base.getConnectionTimeout();
        }

        @Override
        public String getPath() {
            return override.getPath() != null ? override.getPath() : base.getPath();
        }

        @Override
        public Method getMethod() {
            return override.getMethod() != null ? override.getMethod() : base.getMethod();
        }

        @Override
        public HttpMethod getHttpMethod() {
            return override.getHttpMethod() != null ? override.getHttpMethod() : base.getHttpMethod();
        }

        @Override
        public ParamConfig getParamConfig(int index) {
            ParamConfig baseConfig = base.getParamConfig(index);
            ParamConfig overrideConfig = override.getParamConfig(index);
            if (overrideConfig == null) return baseConfig;
            if (baseConfig == null) return null;
            return new OverridingParamConfig(baseConfig, overrideConfig);
        }
    }

    private static class OverridingInterfaceConfig implements InterfaceConfig {
        private final InterfaceConfig base;
        private final InterfaceConfig override;

        private OverridingInterfaceConfig(InterfaceConfig base, InterfaceConfig override) {
            this.base = base;
            this.override = override;
        }

        @Override
        public String getEncoding() {
            return override.getEncoding() != null ? override.getEncoding() : base.getEncoding();
        }

        @Override
        public Method[] getMethods() {
            return override.getMethods() != null ? override.getMethods() : base.getMethods();
        }

        @Override
        public String getServer() {
            return override.getServer() != null ? override.getServer() : base.getServer();
        }

        @Override
        public Class<?> getInterface() {
            return override.getInterface() != null ? override.getInterface() : base.getInterface();
        }

        @Override
        public String getPath() {
            return override.getPath() != null ? override.getPath() : base.getPath();
        }

        @Override
        public RequestInterceptor getRequestInterceptor() {
            if (override.getRequestInterceptor() == null) {
                return base.getRequestInterceptor();
            } else if (base.getRequestInterceptor() == null) {
                return override.getRequestInterceptor();
            } else {
                return new CompositeRequestInterceptor(override.getRequestInterceptor(), base.getRequestInterceptor());
            }
        }

        public MethodConfig getMethodConfig(Method meth) {
            MethodConfig baseConfig = base.getMethodConfig(meth);
            MethodConfig overrideConfig = override.getMethodConfig(meth);
            if (overrideConfig == null) return baseConfig;
            if (baseConfig == null) return null;
            return new OverridingMethodConfig(baseConfig, overrideConfig);
        }
    }
}

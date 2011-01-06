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

import org.codegist.crest.HttpMethod;
import org.codegist.crest.annotate.Destination;
import org.codegist.crest.annotate.Name;
import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;

import java.lang.reflect.Method;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public final class Configs {

    private Configs() {
        throw new IllegalStateException();
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
    static ConfigBuilders.ParamConfigBuilder injectAnnotatedConfig(ConfigBuilders.ParamConfigBuilder config, Class<?> paramType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        /* Params type specifics */
        org.codegist.crest.annotate.Serializer serializer = paramType.getAnnotation(org.codegist.crest.annotate.Serializer.class);
        Name name = paramType.getAnnotation(Name.class);
        Destination destination = paramType.getAnnotation(org.codegist.crest.annotate.Destination.class);
        org.codegist.crest.annotate.Injector injector = paramType.getAnnotation(org.codegist.crest.annotate.Injector.class);

        if(serializer != null) config.setSerializer(serializer.value());
        if(name != null) config.setName(name.value());
        if(destination != null) config.setDestination(destination.value());
        if(injector != null) config.setInjector(injector.value());

        return config;
    }


    private static class OverridingParamConfig implements ParamConfig {
        private final ParamConfig base;
        private final ParamConfig override;

        private OverridingParamConfig(ParamConfig base, ParamConfig override) {
            this.base = base;
            this.override = override;
        }

        public Serializer getSerializer() {
            return override.getSerializer() != null ? override.getSerializer() : base.getSerializer();
        }

        public org.codegist.crest.config.Destination getDestination() {
            return override.getDestination() != null ? override.getDestination() : base.getDestination();
        }

        public String getName() {
            return override.getName() != null ? override.getName() : base.getName();
        }

        public Injector getInjector() {
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

        public StaticParam[] getStaticParams() {
            return override.getStaticParams() != null ? override.getStaticParams() : base.getStaticParams();
        }

        public Integer getParamCount() {
            return override.getParamCount() != null ? override.getParamCount() : base.getParamCount();
        }

        public ResponseHandler getResponseHandler() {
            return override.getResponseHandler() != null ? override.getResponseHandler() : base.getResponseHandler();
        }

        public ErrorHandler getErrorHandler() {
            return override.getErrorHandler() != null ? override.getErrorHandler() : base.getErrorHandler();
        }

        public RetryHandler getRetryHandler() {
            return override.getRetryHandler() != null ? override.getRetryHandler() : base.getRetryHandler();
        }

        public RequestInterceptor getRequestInterceptor() {
            if (override.getRequestInterceptor() == null) {
                return base.getRequestInterceptor();
            } else if (base.getRequestInterceptor() == null) {
                return override.getRequestInterceptor();
            } else {
                return new CompositeRequestInterceptor(override.getRequestInterceptor(), base.getRequestInterceptor());
            }
        }

        public Long getSocketTimeout() {
            return override.getSocketTimeout() != null ? override.getSocketTimeout() : base.getSocketTimeout();
        }

        public Long getConnectionTimeout() {
            return override.getConnectionTimeout() != null ? override.getConnectionTimeout() : base.getConnectionTimeout();
        }

        public String getPath() {
            return override.getPath() != null ? override.getPath() : base.getPath();
        }

        public Method getMethod() {
            return override.getMethod() != null ? override.getMethod() : base.getMethod();
        }

        public HttpMethod getHttpMethod() {
            return override.getHttpMethod() != null ? override.getHttpMethod() : base.getHttpMethod();
        }

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

        public String getEncoding() {
            return override.getEncoding() != null ? override.getEncoding() : base.getEncoding();
        }

        public Method[] getMethods() {
            return override.getMethods() != null ? override.getMethods() : base.getMethods();
        }

        public String getEndPoint() {
            return override.getEndPoint() != null ? override.getEndPoint() : base.getEndPoint();
        }

        public Class<?> getInterface() {
            return override.getInterface() != null ? override.getInterface() : base.getInterface();
        }

        public String getContextPath() {
            return override.getContextPath() != null ? override.getContextPath() : base.getContextPath();
        }

        public RequestInterceptor getGlobalInterceptor() {
            if (override.getGlobalInterceptor() == null) {
                return base.getGlobalInterceptor();
            } else if (base.getGlobalInterceptor() == null) {
                return override.getGlobalInterceptor();
            } else {
                return new CompositeRequestInterceptor(override.getGlobalInterceptor(), base.getGlobalInterceptor());
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

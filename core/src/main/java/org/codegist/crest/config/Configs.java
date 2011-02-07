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

import org.codegist.common.lang.Objects;
import org.codegist.common.lang.Strings;
import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public final class Configs {

    private Configs() {
        throw new IllegalStateException();
    }


    /**
     * Overrides a config (overrides) with another one (base).
     * <p>The override is a config template where nulls values are legals and will fallback to the base config. Base config must apply to the general contract of {@link org.codegist.crest.config.InterfaceConfig}.
     * <p>Any non-null values in override config will take priority over base config.
     * <p>RequestInterceptor are not overriding each other but are chaining, thus if either override and base configs declare a request interceptor, both of them will run, with the override's request interceptor running before the base one.
     * <p>If dynamic flag is true, the returned config is a dynamic view over the two given config, thus the two configs can change over time and the resulting config will reflect the changes.
     *
     * @param base      Normal full configured config, respect the general contract of InterfaceConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @param dynamic Wheter the resulting override should redirect calls to given base or overrides configs.
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     * @see org.codegist.crest.config.InterfaceConfig
     */
    public static InterfaceConfig override(InterfaceConfig base, InterfaceConfig overrides, boolean dynamic) {
        if (overrides == null) return base;
        if(dynamic) return new DynamicOverridingInterfaceConfig(base, overrides);// could be done by proxy

        Map<Method, MethodConfig> cache = new HashMap<Method, MethodConfig>();
        for(Method method : Objects.defaultIfNull(overrides.getMethods(), base.getMethods())){
            cache.put(method, override(base.getMethodConfig(method), overrides.getMethodConfig(method), dynamic));
        }
        return new DefaultInterfaceConfig(
                Objects.defaultIfNull(overrides.getInterface(), base.getInterface()),
                Objects.defaultIfNull(overrides.getEndPoint(), base.getEndPoint()),
                Objects.defaultIfNull(overrides.getContextPath(), base.getContextPath()),
                Objects.defaultIfNull(overrides.getEncoding(), base.getEncoding()),
                Objects.defaultIfNull(overrides.getGlobalInterceptor(), base.getGlobalInterceptor()),
                cache
        );
    }

    /**
     * @see Configs#override(InterfaceConfig, InterfaceConfig, boolean)
     * @param base      Normal full configured config, respect the general contract of InterfaceConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. This is a static override, changes over time of both config won't be reflected
     */
    public static InterfaceConfig override(InterfaceConfig base, InterfaceConfig overrides) {
        return override(base, overrides, false);
    }

    /**
     * Overrides a config (overrides) with another one (base).
     * <p>The override is a config template where nulls values are legals and will fallback to the base config. Base config must apply to the general contract of {@link org.codegist.crest.config.MethodConfig}.
     * <p>Any non-null values in override config will take priority over base config.
     * <p>RequestInterceptor are not overriding each other but are chaining, thus if either override and base configs declare a request interceptor, both of them will run, with the override's request interceptor running before the base one.
     * <p>If dynamic flag is true, the returned config is a dynamic view over the two given config, thus the two configs can change over time and the resulting config will reflect the changes.
     *
     * @param base      Normal full configured config, respect the general contract of MethodConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @param dynamic Wheter the resulting override should redirect calls to given base or overrides configs.
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     * @see org.codegist.crest.config.MethodConfig
     */
    public static MethodConfig override(MethodConfig base, MethodConfig overrides, boolean dynamic) {
        if (overrides == null) return base;
        if(dynamic) return new DynamicOverridingMethodConfig(base, overrides);// could be done by proxy

        ParamConfig[] pl = new ParamConfig[Objects.defaultIfNull(overrides.getParamCount(), base.getParamCount())];
        for(int i = 0; i < pl.length; i++){
            pl[i] = override(base.getParamConfig(i), overrides.getParamConfig(i), dynamic);
        }

        return new DefaultMethodConfig(
                Objects.defaultIfNull(overrides.getMethod(), base.getMethod()),
                Objects.defaultIfNull(overrides.getPath(), base.getPath()),
                Objects.defaultIfNull(overrides.getHttpMethod(), base.getHttpMethod()),
                Objects.defaultIfNull(overrides.getSocketTimeout(), base.getSocketTimeout()),
                Objects.defaultIfNull(overrides.getConnectionTimeout(), base.getConnectionTimeout()),
                Objects.defaultIfNull(overrides.getRequestInterceptor(), base.getRequestInterceptor()),
                Objects.defaultIfNull(overrides.getResponseHandler(), base.getResponseHandler()),
                Objects.defaultIfNull(overrides.getErrorHandler(), base.getErrorHandler()),
                Objects.defaultIfNull(overrides.getRetryHandler(), base.getRetryHandler()),
                pl,
                Objects.defaultIfNull(overrides.getExtraParams(), base.getExtraParams())
        );
    }
    /**
      * @see Configs#override(MethodConfig, MethodConfig, boolean)
      * @param base      Normal full configured config, respect the general contract of MethodConfig object
      * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
      * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
      */
    public static MethodConfig override(MethodConfig base, MethodConfig overrides) {
        return override(base, overrides, true);
    }

    /**
     * Overrides a config (overrides) with another one (base).
     * <p>The override is a config template where nulls values are legals and will fallback to the base config. Base config must apply to the general contract of {@link org.codegist.crest.config.ParamConfig}.
     * <p>Any non-null values in override config will take priority over base config.
     * <p>If dynamic flag is true, the returned config is a dynamic view over the two given config, thus the two configs can change over time and the resulting config will reflect the changes.
     *
     * @param base      Normal full configured config, respect the general contract of ParamConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @param dynamic Wheter the resulting override should redirect calls to given base or overrides configs.
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     * @see org.codegist.crest.config.ParamConfig
     */
    public static ParamConfig override(ParamConfig base, ParamConfig overrides, boolean dynamic) {
        if (overrides == null) return base;
        if(dynamic) return new DynamicOverridingParamConfig(base, overrides); // could be done by proxy

        return new DefaultParamConfig(
                Objects.defaultIfNull(overrides.getName(), base.getName()),
                Objects.defaultIfNull(overrides.getDefaultValue(), base.getDefaultValue()),
                Objects.defaultIfNull(overrides.getDestination(), base.getDestination()),
                Objects.defaultIfNull(overrides.getSerializer(), base.getSerializer()),
                Objects.defaultIfNull(overrides.getInjector(), base.getInjector())
        );
    }

    /**
     *
     * @see Configs#override(ParamConfig, ParamConfig, boolean)
     * @param base      Normal full configured config, respect the general contract of ParamConfig object
     * @param overrides Config template, can hold null values, that plays as flag to indicate a fallback to the base config
     * @return A view that gives priority of "overrides" non-null values object upon "base" object. Any changes at runtime will be reflected.
     */
    public static ParamConfig override(ParamConfig base, ParamConfig overrides) {
        return override(base, overrides, true);
    }

    @SuppressWarnings("unchecked")
    static ConfigBuilders.ParamConfigBuilder injectAnnotatedConfig(ConfigBuilders.ParamConfigBuilder config, Class<?> paramType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        /* Params type specifics */
        org.codegist.crest.annotate.Serializer serializer = paramType.getAnnotation(org.codegist.crest.annotate.Serializer.class);
        org.codegist.crest.annotate.Injector injector = paramType.getAnnotation(org.codegist.crest.annotate.Injector.class);

        if(serializer != null) config.setSerializer(serializer.value());
        if(injector != null) config.setInjector(injector.value());

        return config;
    }


    private static class DynamicOverridingParamConfig implements ParamConfig {
        private final ParamConfig base;
        private final ParamConfig override;

        private DynamicOverridingParamConfig(ParamConfig base, ParamConfig override) {
            this.base = base;
            this.override = override;
        }

        public Serializer getSerializer() {
            return Objects.defaultIfNull(override.getSerializer(), base.getSerializer());
        }

        public org.codegist.crest.config.Destination getDestination() {
            return Objects.defaultIfNull(override.getDestination(), base.getDestination());
        }

        public String getName() {
            return Objects.defaultIfNull(override.getName(), base.getName());
        }

        public String getDefaultValue() {
            return Objects.defaultIfNull(override.getDefaultValue(), base.getDefaultValue());
        }

        public Injector getInjector() {
            return Objects.defaultIfNull(override.getInjector(), base.getInjector());
        }
    }

    private static class DynamicOverridingMethodConfig implements MethodConfig {
        private final MethodConfig base;
        private final MethodConfig override;

        private DynamicOverridingMethodConfig(MethodConfig base, MethodConfig override) {
            this.base = base;
            this.override = override;
        }

        public BasicParamConfig[] getExtraParams() {
            return Objects.defaultIfNull(override.getExtraParams(), base.getExtraParams());
        }

        public Integer getParamCount() {
            return Objects.defaultIfNull(override.getParamCount(), base.getParamCount());
        }

        public ResponseHandler getResponseHandler() {
            return Objects.defaultIfNull(override.getResponseHandler(), base.getResponseHandler());
        }

        public ErrorHandler getErrorHandler() {
            return Objects.defaultIfNull(override.getErrorHandler(), base.getErrorHandler());
        }

        public RetryHandler getRetryHandler() {
            return Objects.defaultIfNull(override.getRetryHandler(), base.getRetryHandler());
        }

        public RequestInterceptor getRequestInterceptor() {
            return Objects.defaultIfNull(override.getRequestInterceptor(), base.getRequestInterceptor());
        }

        public Long getSocketTimeout() {
            return Objects.defaultIfNull(override.getSocketTimeout(), base.getSocketTimeout());
        }

        public Long getConnectionTimeout() {
            return Objects.defaultIfNull(override.getConnectionTimeout(), base.getConnectionTimeout());
        }

        public String getPath() {
            return Objects.defaultIfNull(override.getPath(), base.getPath());
        }

        public Method getMethod() {
            return Objects.defaultIfNull(override.getMethod(), base.getMethod());
        }

        public String getHttpMethod() {
            return Objects.defaultIfNull(override.getHttpMethod(), base.getHttpMethod());
        }

        public ParamConfig getParamConfig(int index) {
            ParamConfig baseConfig = base.getParamConfig(index);
            ParamConfig overrideConfig = override.getParamConfig(index);
            if (overrideConfig == null) return baseConfig;
            if (baseConfig == null) return null;
            return new DynamicOverridingParamConfig(baseConfig, overrideConfig);
        }
    }

    private static class DynamicOverridingInterfaceConfig implements InterfaceConfig {
        private final InterfaceConfig base;
        private final InterfaceConfig override;

        private DynamicOverridingInterfaceConfig(InterfaceConfig base, InterfaceConfig override) {
            this.base = base;
            this.override = override;
        }

        public String getEncoding() {
            return Objects.defaultIfNull(override.getEncoding(), base.getEncoding());
        }

        public Method[] getMethods() {
            return Objects.defaultIfNull(override.getMethods(), base.getMethods());
        }

        public String getEndPoint() {
            return Objects.defaultIfNull(override.getEndPoint(), base.getEndPoint());
        }

        public Class<?> getInterface() {
            return Objects.defaultIfNull(override.getInterface(), base.getInterface());
        }

        public String getContextPath() {
            return Objects.defaultIfNull(override.getContextPath(), base.getContextPath());
        }

        public RequestInterceptor getGlobalInterceptor() {
            return Objects.defaultIfNull(override.getGlobalInterceptor(), base.getGlobalInterceptor());
        }

        public MethodConfig getMethodConfig(Method meth) {
            MethodConfig baseConfig = base.getMethodConfig(meth);
            MethodConfig overrideConfig = override.getMethodConfig(meth);
            if (overrideConfig == null) return baseConfig;
            if (baseConfig == null) return null;
            return new DynamicOverridingMethodConfig(baseConfig, overrideConfig);
        }
    }
}


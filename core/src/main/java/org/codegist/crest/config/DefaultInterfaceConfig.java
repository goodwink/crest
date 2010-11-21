package org.codegist.crest.config;

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.InterfaceConfig}
 */
class DefaultInterfaceConfig implements InterfaceConfig {

    private final Class<?> interfaze;
    private final String server;
    private final String path;
    private final String encoding;
    private final RequestInterceptor requestInterceptor;

    private final Map<Method, MethodConfig> cache;

    DefaultInterfaceConfig(Class<?> interfaze, String server, String path, String encoding, RequestInterceptor requestInterceptor, Map<Method, MethodConfig> cache) {
        this.interfaze = interfaze;
        this.server = server;
        this.path = path;
        this.encoding = encoding;
        this.requestInterceptor = requestInterceptor;
        this.cache = Maps.unmodifiable(cache);
    }

    @Override
    public Class<?> getInterface() {
        return interfaze;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public RequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    @Override
    public Method[] getMethods() {
        return interfaze != null ? interfaze.getDeclaredMethods() : null;
    }

    @Override
    public MethodConfig getMethodConfig(Method meth) {
        return cache != null ? cache.get(meth) : null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("interface", interfaze)
                .append("server", server)
                .append("path", path)
                .append("encoding", encoding)
                .append("requestInterceptor", requestInterceptor)
                .append("cache", cache)
                .toString();
    }


}

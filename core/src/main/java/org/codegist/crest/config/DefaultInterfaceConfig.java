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

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.InterfaceConfig}
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
class DefaultInterfaceConfig implements InterfaceConfig {

    private final Class<?> interfaze;
    private final String endPoint;
    private final String contextPath;
    private final String encoding;
    private final RequestInterceptor globalInterceptor;

    private final Map<Method, MethodConfig> cache;

    DefaultInterfaceConfig(Class<?> interfaze, String endPoint, String contextPath, String encoding, RequestInterceptor globalInterceptor, Map<Method, MethodConfig> cache) {
        this.interfaze = interfaze;
        this.endPoint = endPoint;
        this.contextPath = contextPath;
        this.encoding = encoding;
        this.globalInterceptor = globalInterceptor;
        this.cache = Maps.unmodifiable(cache);
    }

    public Class<?> getInterface() {
        return interfaze;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getEncoding() {
        return encoding;
    }

    public RequestInterceptor getGlobalInterceptor() {
        return globalInterceptor;
    }

    public Method[] getMethods() {
        return interfaze != null ? interfaze.getDeclaredMethods() : null;
    }

    public MethodConfig getMethodConfig(Method meth) {
        return cache != null ? cache.get(meth) : null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("interface", interfaze)
                .append("server", endPoint)
                .append("contextPath", contextPath)
                .append("encoding", encoding)
                .append("globalInterceptor", globalInterceptor)
                .append("cache", cache)
                .toString();
    }


}

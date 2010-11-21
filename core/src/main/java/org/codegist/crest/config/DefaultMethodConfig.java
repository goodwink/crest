package org.codegist.crest.config;

import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.ErrorHandler;
import org.codegist.crest.HttpMethod;
import org.codegist.crest.ResponseHandler;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.DefaultMethodConfig}
 */
class DefaultMethodConfig implements MethodConfig {

    private final Method method;
    private final String path;
    private final HttpMethod httpMethod;
    private final Long socketTimeout;
    private final Long connectionTimeout;
    private final RequestInterceptor requestInterceptor;
    private final ResponseHandler responseHandler;
    private final ErrorHandler errorHandler;

    private final ParamConfig[] paramConfigs;

    DefaultMethodConfig(Method method, String path, HttpMethod httpMethod, Long socketTimeout, Long connectionTimeout, RequestInterceptor requestInterceptor, ResponseHandler responseHandler, ErrorHandler errorHandler, ParamConfig[] paramConfigs) {
        this.method = method;
        this.path = path;
        this.httpMethod = httpMethod;
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.requestInterceptor = requestInterceptor;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        this.paramConfigs = paramConfigs.clone();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    @Override
    public Long getSocketTimeout() {
        return socketTimeout;
    }

    @Override
    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public RequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public ParamConfig getParamConfig(int index) {
        return paramConfigs != null && index < paramConfigs.length ? paramConfigs[index] : null;
    }

    @Override
    public Integer getParamCount() {
        return paramConfigs != null ? paramConfigs.length : null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("path", path)
                .append("method", method)
                .append("httpMethod", httpMethod)
                .append("socketTimeout", socketTimeout)
                .append("connectionTimeout", connectionTimeout)
                .append("requestInterceptor", requestInterceptor)
                .append("responseHandler", responseHandler)
                .append("errorHandler", errorHandler)
                .append("paramConfigs", paramConfigs)
                .toString();
    }
}

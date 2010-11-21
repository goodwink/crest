package org.codegist.crest.interceptor;

import org.codegist.common.collect.Maps;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;

import java.util.Map;

/**
 * Interceptor that add some arbitrary query string, body and/or headers parameters to the requests.
 */
public class RequestParamDefaultsInterceptor extends RequestInterceptorAdapter {

    private final Map<String, String> queryString;
    private final Map<String, String> headers;
    private final Map<String, Object> body;

    public RequestParamDefaultsInterceptor(Map<String, String> queryString, Map<String, String> headers, Map<String, Object> body) {
        this.queryString = Maps.unmodifiable(queryString);
        this.headers = Maps.unmodifiable(headers);
        this.body = Maps.unmodifiable(body);
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        builder
                .addHeaders(headers)
                .addQueryParams(queryString)
                .addBodyParams(body);
        return true;
    }
}

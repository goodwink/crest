package org.codegist.crest;

import java.lang.reflect.Type;

/**
 * Default internal immutable implementation of ResponseContext
 */
class DefaultResponseContext implements ResponseContext {

    private final HttpResponse response;
    private final RequestContext context;

    public DefaultResponseContext(ResponseContext context) {
        this(context.getRequestContext(), context.getResponse());
    }

    public DefaultResponseContext(RequestContext context, HttpResponse response) {
        this.context = context;
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public Type getExpectedGenericType() {
        return context.getMethod().getGenericReturnType();
    }

    public Class<?> getExpectedType() {
        return context.getMethod().getReturnType();
    }

    public RequestContext getRequestContext() {
        return context;
    }
}

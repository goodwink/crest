package org.codegist.crest.interceptor;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;

/**
 * Simple {@link org.codegist.crest.interceptor.RequestInterceptor} adapter.
 *
 * @see RequestInterceptor
 */
public class RequestInterceptorAdapter implements RequestInterceptor {

    @Override
    public boolean beforeParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        return true;
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        return true;
    }

}

package org.codegist.crest.interceptor;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;

/**
 * Simple composite request interceptor that delegate notifications to a predefined list of interceptors.
 * <p>If any of the delegates returns false, the process is stopped and return false as well.
 */
public class CompositeRequestInterceptor implements RequestInterceptor {
    private final RequestInterceptor[] interceptors;

    public CompositeRequestInterceptor(RequestInterceptor... interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public boolean beforeParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        for (RequestInterceptor interceptor : interceptors) {
            if (interceptor == null) continue;
            if (!interceptor.beforeParamsInjectionHandle(builder, context)) return false;
        }
        return true;
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        for (RequestInterceptor interceptor : interceptors) {
            if (interceptor == null) continue;
            if (!interceptor.afterParamsInjectionHandle(builder, context)) return false;
        }
        return true;
    }

    public RequestInterceptor[] getInterceptors() {
        return interceptors.clone();
    }
}

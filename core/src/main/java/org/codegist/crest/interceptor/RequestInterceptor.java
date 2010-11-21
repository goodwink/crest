package org.codegist.crest.interceptor;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;

/**
 * A request interceptor is notified before and after the parameters have been added to the request.
 * <p>It can be used to cancel a request from being fired by returning false, or arbitrary modify the request.
 */
public interface RequestInterceptor {

    /**
     * Called after general parameter have been added to the request, but before parameters are injected into it.
     *
     * @param builder The current http request being build
     * @param context The current request context
     * @return a flag indicating whether to continue or cancel the current request.
     */
    boolean beforeParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context);


    /**
     * Called after parameters have been injected into the request.
     *
     * @param builder The current http request being build
     * @param context The current request context
     * @return a flag indicating whether to continue or cancel the current request.
     */
    boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context);

}

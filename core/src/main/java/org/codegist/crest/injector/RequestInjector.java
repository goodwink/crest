package org.codegist.crest.injector;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.ParamContext;

/**
 * A request injector is used to inject any method parameter values in the http request before it gets fired. Can modify the http request as wanted.
 */
public interface RequestInjector {

    /**
     * Injects the current param into the request.
     *
     * @param builder Current http request being build.
     * @param context The current param context holding the value of the current method argument and all other context objects.
     */
    void inject(HttpRequest.Builder builder, ParamContext context);

}

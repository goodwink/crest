package org.codegist.crest;

import java.lang.reflect.Type;

/**
 * Response context, passed to the response handlers and error handlers.
 *
 * @see org.codegist.crest.ResponseHandler
 * @see org.codegist.crest.ErrorHandler
 */
public interface ResponseContext {

    RequestContext getRequestContext();

    HttpResponse getResponse();

    Type getExpectedGenericType();

    Class<?> getExpectedType();

}

package org.codegist.crest;

/**
 * Response handler is invoked for each request's as long as the interface doesn't specifically requested for the raw response (Reader or InputStream method return types.)
 * <p>Response handler role is to check for thridparties specific error formatted content in the response, and returns the expected return type for method calls.
 * <p>NB: if the response code if different from HTTP 200, this handler won't be called, error handler will be directly invoked
 */
public interface ResponseHandler {

    Object handle(ResponseContext responseContext) throws CRestException;

}

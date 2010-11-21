package org.codegist.crest;

/**
 * Error handler gets invoked when an exception occurs during each request lifecyle :
 * <p>- Generation : involves RequestInterceptors, RequestInjectors, Serializer.
 * <p>- Firing : Any non 200 response status code while fire an exception.
 * <p>- Response handling : involves ResponseHandlers.
 */
public interface ErrorHandler {

    /**
     * @param context Current response context. Inner HttpResponse if not guaranteed to be available as an error could have occured during request generation lifecycle step.
     * @param e       Exception occured
     * @param <T>     Expected return type
     * @return any value of the expected error type when exception is ignored
     * @throws Exception Any thrown exception while be delegated to the client using the relative rest interface.
     * @see ErrorHandler
     */
    <T> T handle(ResponseContext context, Exception e) throws Exception;

}

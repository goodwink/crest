package org.codegist.crest;

/**
 * Error handler that always delegate the given exception to the caller.
 */
public class ErrorDelegatorHandler implements ErrorHandler {

    @Override
    public <T> T handle(ResponseContext context, Exception e) throws Exception {
        throw e;
    }
}

package org.codegist.crest;

import org.codegist.common.reflect.Types;

/**
 * Error handler that ignores exception and return default values
 */
public class DefaultValuesErrorHandler implements ErrorHandler {

    @Override
    public <T> T handle(ResponseContext context, Exception e) throws Exception {
        // Should log the exception.
        return Types.<T>getDefaultValueFor(context.getRequestContext().getMethodConfig().getMethod().getReturnType());
    }

}

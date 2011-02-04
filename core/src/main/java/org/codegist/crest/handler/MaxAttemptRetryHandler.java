/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.handler;

import org.codegist.common.lang.Numbers;
import org.codegist.common.log.Logger;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.ResponseContext;

import java.util.Map;

/**
 * Default retry handler always returns true is the given attempt is strictly less than given max value.
 * Defaults configuration always return false. This behavior can be changed
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class MaxAttemptRetryHandler implements RetryHandler {
    /**
     * Default retry handler max value used by the empty constructor.
     */
    public static final int DEFAULT_MAX = 0;

    private final int max;

    private static final Logger LOG = Logger.getLogger(MaxAttemptRetryHandler.class);

    public MaxAttemptRetryHandler(int max) {
        this.max = max;
    }

    public MaxAttemptRetryHandler() {
        this(DEFAULT_MAX);
    }

    public MaxAttemptRetryHandler(Map<String, Object> customProperties) {
        this.max = Numbers.parse((String) customProperties.get(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS), DEFAULT_MAX);
    }


    public boolean retry(ResponseContext response, Exception exception, int retryNumber) {
        boolean retry = retryNumber < max;
        LOG.debug("Retrying attempt=%d,max=%d,retry=%b,reason=%s", retryNumber, max, retry, exception != null ? exception.getMessage() : "unknown");
        return retry;
    }
}

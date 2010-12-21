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

package org.codegist.crest.security.handler;

import org.codegist.common.lang.Numbers;
import org.codegist.common.lang.Validate;
import org.codegist.common.log.Logger;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.HttpException;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.security.AuthentificationManager;

import java.util.Map;

/**
 * Authentification retry handler that refresh the authentification if the retry cause is a 401 problem.
 * <p>Requires an {@link org.codegist.crest.security.AuthentificationManager} instance to be present in the custom properties.
 */
public class RefreshAuthentificationRetryHandler implements RetryHandler {

    private final static Logger LOGGER = Logger.getLogger(RefreshAuthentificationRetryHandler.class);
    public static final int DEFAULT_MAX_ATTEMPTS = 1; /* will retry just once in order to refresh the token */

    private final AuthentificationManager authentificationManager;
    private final int max;

    public RefreshAuthentificationRetryHandler(Map<String, Object> customProperties) {
        this.max = Numbers.parse((String) customProperties.get(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS), DEFAULT_MAX_ATTEMPTS);
        authentificationManager = (AuthentificationManager) customProperties.get(AuthentificationManager.class.getName());
        Validate.notNull(this.authentificationManager, "No authentification manager found, please pass it in the properties (key=" + AuthentificationManager.class.getName() + ")");
    }

    public boolean retry(ResponseContext response, Exception exception, int retryNumber) {
        if (retryNumber >= (max + 1) /* +1 so that even if no retry are requested on failure, at least on is done for refreshing the token */
                || !(exception instanceof HttpException)
                || ((HttpException) exception).getResponse().getStatusCode() != 401) {
            LOGGER.debug("Not retrying, maximum failure reached or catched exception is neither a HttpException nor a 401 HTTP error code");
            return false;
        }
        LOGGER.debug("HTTP code 401 detected, refreshing authentification and retry.");
        authentificationManager.refresh();
        return true;
    }

}

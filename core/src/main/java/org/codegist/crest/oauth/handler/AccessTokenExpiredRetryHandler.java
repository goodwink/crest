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

package org.codegist.crest.oauth.handler;

import org.codegist.common.lang.Numbers;
import org.codegist.crest.CRestException;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.HttpException;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.oauth.interceptor.OAuthInterceptor;

import java.util.Map;

public class AccessTokenExpiredRetryHandler implements RetryHandler {

    public static final int DEFAULT_YUI_MAX_ATTEMPTS = 1; /* will retry just once in order to refresh the token */

    private final int max;

    public AccessTokenExpiredRetryHandler(Map<String, Object> customProperties) {
        this.max = Numbers.parse((String) customProperties.get(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS), DEFAULT_YUI_MAX_ATTEMPTS);
    }

    public boolean retry(ResponseContext response, Exception exception, int retryNumber) {
        if (retryNumber > (max + 1) /* +1 so that even if no retry are requested on failure, at least on is done for refreshing the token */
                || !(exception instanceof HttpException)
                || ((HttpException) exception).getResponse().getStatusCode() != 401)
            return false;
        try {
            // Gets the global authenticator interceptor

            OAuthInterceptor oAuthInterceptor;
            if(response.getRequestContext().getConfig().getGlobalInterceptor() instanceof OAuthInterceptor) {
                oAuthInterceptor = (OAuthInterceptor) response.getRequestContext().getConfig().getGlobalInterceptor();
            }else if(response.getRequestContext().getMethodConfig().getRequestInterceptor() instanceof OAuthInterceptor) {
                oAuthInterceptor = (OAuthInterceptor) response.getRequestContext().getMethodConfig().getRequestInterceptor();
            } else{
                throw new IllegalStateException("No OAuthInterceptor found!");
            }

            oAuthInterceptor.refreshAccessToken();
            return true;
        } catch (Exception e) {
            throw new CRestException(e);
        }
    }

}

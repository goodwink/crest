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

package org.codegist.crest.delicious.handler;

import org.codegist.common.lang.Numbers;
import org.codegist.common.lang.Strings;
import org.codegist.common.lang.Validate;
import org.codegist.common.net.Urls;
import org.codegist.crest.*;
import org.codegist.crest.delicious.interceptor.YahooOAuthInterceptor;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.Param;

import java.net.URISyntaxException;
import java.util.Map;

public class YahooExpiredTokenRetryHandler implements RetryHandler {

    public static final String YUI_EXPIRED_TOKEN_URL_PROP = "yui.handler.retry.expired-token.url";
    public static final String DEFAULT_YUI_EXPIRED_TOKEN_URL = "https://api.login.yahoo.com/oauth/v2/get_token";
    public static final int DEFAULT_YUI_MAX_ATTEMPTS = 1; /* will retry just once in order to refresh the token */

    private final int max;
    private final String url;
    private final RestService restService;

    public YahooExpiredTokenRetryHandler(Map<String, Object> customProperties) {
        restService = (RestService) customProperties.get(RestService.class.getName());
        this.max = Numbers.parse((String) customProperties.get(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS), DEFAULT_YUI_MAX_ATTEMPTS);
        this.url = Strings.defaultIfBlank((String) customProperties.get(YUI_EXPIRED_TOKEN_URL_PROP), DEFAULT_YUI_EXPIRED_TOKEN_URL);
        Validate.notNull(this.restService, "Custom properties map expected to contain a RestService instance (key=" + RestService.class.getName() + ")");
    }

    public boolean retry(ResponseContext response, Exception exception, int retryNumber) {
        if (retryNumber > (max + 1) /* +1 so that even if no retry are requested on failure, at least on is done for refreshing the token */
                || !(exception instanceof HttpException)
                || ((HttpException) exception).getResponse().getStatusCode() != 401)
            return false;
        try {
            // Gets the global authenticator interceptor
            YahooOAuthInterceptor oAuthInterceptor = (YahooOAuthInterceptor) response.getRequestContext().getConfig().getGlobalInterceptor();

            // Requests a token refresh
            Map<String, String> refreshedToken = fireRefreshTokenRequest(oAuthInterceptor.getOAuthenticator(), oAuthInterceptor.getSessionHandle());

            // Extracts token informations from the request.
            oAuthInterceptor.refreshToken(
                    refreshedToken.get("oauth_token"),
                    refreshedToken.get("oauth_token_secret"),
                    refreshedToken.get("oauth_session_handle")
            );

            return true;
        } catch (Exception e) {
            throw new CRestException(e);
        }
    }

    private Map<String, String> fireRefreshTokenRequest(OAuthenticator authenticator, String sessionHandle) throws URISyntaxException {
        // Builds the refresh request, sign it using the authenticator, fire it and parse the result
        HttpRequest.Builder request = new HttpRequest.Builder(this.url);
        authenticator.process(request, new Param("oauth_session_handle", sessionHandle));
        HttpResponse refreshTokenResponse = restService.exec(request.build());
        return Urls.parseQueryString(refreshTokenResponse.asString());
    }

}

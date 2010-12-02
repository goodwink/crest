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

package org.codegist.crest.delicious.interceptor;

import org.codegist.common.lang.Validate;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;
import org.codegist.crest.interceptor.OAuthInterceptor;
import org.codegist.crest.oauth.Token;

import java.util.Map;


public class YahooOAuthInterceptor extends OAuthInterceptor {

    public static final String YUI_OAUTH_SESSION_HANDLE = "yui.authentification.oauth.session.handle";

    public YahooOAuthInterceptor(Map<String, Object> customProperties) {
        super(
                isWriteToHeader(customProperties),
                getConsumerToken(customProperties),
                getAccessToken(customProperties)
        );
    }

    public String getSessionHandle(){
        return ((YuiToken) this.getOAuthenticator().getAccessToken()).getSessionHandle();
    }
    
    public void refreshToken(String tok, String secret, String sessionHandle){
        super.refreshToken(new YuiToken(tok, secret, sessionHandle));
    }

    protected static Token getAccessToken(Map<String, Object> customProperties){
        Token token = OAuthInterceptor.getAccessToken(customProperties);
        String sessionHandle =  (String) customProperties.get(YUI_OAUTH_SESSION_HANDLE);
        Validate.notEmpty(sessionHandle, "No default session handle set for interceptor. Please pass it into the custom properties (key=" + YUI_OAUTH_SESSION_HANDLE + ")");
        return new YuiToken(token, sessionHandle);
    }

    private static class YuiToken extends Token {
        private final String sessionHandle;

        private YuiToken(Token base, String sessionHandle) {
            this(base.getToken(), base.getSecret(), sessionHandle);
        }
        private YuiToken(String token, String secret, String sessionHandle) {
            super(token, secret);
            this.sessionHandle = sessionHandle;
        }

        public String getSessionHandle() {
            return sessionHandle;
        }
    }

}

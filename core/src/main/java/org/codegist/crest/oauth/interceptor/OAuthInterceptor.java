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

package org.codegist.crest.oauth.interceptor;

import org.codegist.common.lang.Validate;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;
import org.codegist.crest.RestService;
import org.codegist.crest.interceptor.RequestInterceptorAdapter;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.OAuthenticatorV10;
import org.codegist.crest.oauth.Token;

import java.util.Map;

import static org.codegist.crest.CRestProperty.*;

/**
 * Simple OAuth interceptor, required a valid pregenerated access token. Only support HMAC-SHA1 signature method.
 * <p>Can be configured either be constructor parameters, or indirectly via {@link org.codegist.crest.RequestContext#getProperties()} with the following key :
 * <p>OAuthInterceptor.{@link org.codegist.crest.CRestProperty#OAUTH_PARAM_DEST}
 * <p>OAuthInterceptor.{@link org.codegist.crest.CRestProperty#OAUTH_ACCESS_TOKEN}
 * <p>OAuthInterceptor.{@link org.codegist.crest.CRestProperty#OAUTH_ACCESS_TOKEN_SECRET}
 * <p>OAuthInterceptor.{@link org.codegist.crest.CRestProperty#OAUTH_CONSUMER_KEY}
 * <p>OAuthInterceptor.{@link org.codegist.crest.CRestProperty#OAUTH_CONSUMER_SECRET}
 *
 * @see org.codegist.crest.RequestContext
 * @see org.codegist.crest.CRestProperty#OAUTH_PARAM_DEST
 * @see org.codegist.crest.CRestProperty#OAUTH_ACCESS_TOKEN
 * @see org.codegist.crest.CRestProperty#OAUTH_ACCESS_TOKEN_SECRET
 * @see org.codegist.crest.CRestProperty#OAUTH_CONSUMER_KEY
 * @see org.codegist.crest.CRestProperty#OAUTH_CONSUMER_SECRET
 * @see org.codegist.crest.InterfaceContext#getProperties()
 * @see org.codegist.crest.DefaultCRest#DefaultCRest(org.codegist.crest.CRestContext)
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthInterceptor extends RequestInterceptorAdapter {

    private final OAuthenticator oauth;
    private volatile Token accessToken;

    public OAuthInterceptor(Map<String,Object> customProperties) {
        String consumerKey = (String) customProperties.get(OAUTH_CONSUMER_KEY);
        String consumerSecret = (String) customProperties.get(OAUTH_CONSUMER_SECRET);
        Validate.notEmpty(consumerKey, "No default consumer key set for interceptor. Please either construct the interceptor with the consumer key, or pass the consumer key into the context (key=" + OAUTH_CONSUMER_KEY + ")");
        Validate.notEmpty(consumerSecret, "No default consumer secret set for intecteptor. Please either construct the interceptor passing consumer and access secrect/token, or pass customer properties in the context (key=" + OAUTH_CONSUMER_SECRET + ")");
        Token consumerToken = new Token(consumerKey, consumerSecret);

        RestService restService = (RestService) customProperties.get(RestService.class.getName());
        this.oauth = new OAuthenticatorV10(restService, consumerToken, customProperties);

        String accessTok = (String) customProperties.get(OAUTH_ACCESS_TOKEN);
        String accessTokenSecret = (String) customProperties.get(OAUTH_ACCESS_TOKEN_SECRET);
        Map<String,String> accessTokenExtras = (Map<String,String>) customProperties.get(OAUTH_ACCESS_TOKEN_EXTRAS);
        Validate.notEmpty(accessTok, "No default access token key set for interceptor. Please either construct the interceptor with the access token key, or pass it into the context (key=" + OAUTH_ACCESS_TOKEN + ")");
        Validate.notEmpty(accessTokenSecret, "No default access token secret set for interceptor. Please either construct the interceptor with the access token secret, or pass it into the context (key=" + OAUTH_ACCESS_TOKEN_SECRET + ")");
        this.accessToken = new Token(accessTok, accessTokenSecret, accessTokenExtras);
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        oauth.sign(accessToken, builder);
        return true;
    }

    public void refreshAccessToken(){
        accessToken = oauth.refreshAccessToken(accessToken, "oauth_session_handle");
    }
}

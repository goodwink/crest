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

package org.codegist.crest.interceptor;

import org.codegist.common.lang.Validate;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;
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

    private volatile OAuthenticator oauth;

    public OAuthInterceptor(Map<String,Object> customProperties) {
        this(isWriteToHeader(customProperties), getConsumerToken(customProperties), getAccessToken(customProperties));
    }

    public OAuthInterceptor(boolean writeToHeaders, String consumerSecret, String consumerKey, String accessTokenSecret, String accessToken) {
        this(writeToHeaders, new Token(consumerKey, consumerSecret), new Token(accessToken, accessTokenSecret));
    }

    public OAuthInterceptor(boolean writeToHeaders, Token consumerToken, Token accessToken) {
        this.oauth = new OAuthenticatorV10(writeToHeaders, consumerToken, accessToken);
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        oauth.process(builder);
        return true;
    }



    protected static boolean isWriteToHeader(Map<String,Object> customProperties){
        return !"url".equalsIgnoreCase((String)customProperties.get(OAUTH_PARAM_DEST));
    }
    protected static Token getAccessToken(Map<String,Object> customProperties){
        String accessTok = (String) customProperties.get(OAUTH_ACCESS_TOKEN);
        String accessTokenSecret = (String) customProperties.get(OAUTH_ACCESS_TOKEN_SECRET);
        Validate.notEmpty(accessTok, "No default access token key set for interceptor. Please either construct the interceptor with the access token key, or pass it into the context (key=" + OAUTH_ACCESS_TOKEN + ")");
        Validate.notEmpty(accessTokenSecret, "No default access token secret set for interceptor. Please either construct the interceptor with the access token secret, or pass it into the context (key=" + OAUTH_ACCESS_TOKEN_SECRET + ")");
        return new Token(accessTok, accessTokenSecret);
    }
    protected static Token getConsumerToken(Map<String,Object> customProperties){
        String consumerKey = (String) customProperties.get(OAUTH_CONSUMER_KEY);
        String consumerSecret = (String) customProperties.get(OAUTH_CONSUMER_SECRET);
        Validate.notEmpty(consumerKey, "No default consumer key set for interceptor. Please either construct the interceptor with the consumer key, or pass the consumer key into the context (key=" + OAUTH_CONSUMER_KEY + ")");
        Validate.notEmpty(consumerSecret, "No default consumer secret set for intecteptor. Please either construct the interceptor passing consumer and access secrect/token, or pass customer properties in the context (key=" + OAUTH_CONSUMER_SECRET + ")");
        return new Token(consumerKey, consumerSecret);
    }

    public OAuthenticator getOAuthenticator(){
        return oauth;
    }

    public void refreshToken(Token accessToken){
        this.oauth = oauth.refreshAccessToken(accessToken);
    }

    
}

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

package org.codegist.crest;

import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.OAuthenticatorV10;
import org.codegist.crest.oauth.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthHelper {

    public static void main(String[] args) throws IOException {
        // Yahoo
        //OAuthHelper.doAccessTokenRetrievalWorkflow(
        // "",
        // "",
        // "https://api.login.yahoo.com/oauth/v2/get_request_token",
        // "https://api.login.yahoo.com/oauth/v2/get_token",
        // "https://api.login.yahoo.com/oauth/v2/request_auth?oauth_token=%s");

        // Twitter
        OAuthHelper.doAccessTokenRetrievalWorkflow(
                "",
                "",
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "http://api.twitter.com/oauth/authorize?oauth_token=%s");
    }


    private static void doAccessTokenRetrievalWorkflow(String consumerTok, String consumerSecret, final String requestUrl, final String accessUrl, String redirect) throws IOException {
        Token consumerToken = new Token(consumerTok, consumerSecret);
        Map<String, Object> config = new HashMap<String, Object>() {{
            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL, requestUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL, accessUrl);
        }};
        OAuthenticator oauth = new OAuthenticatorV10(new DefaultRestService(), consumerToken, config);

        Token tok = oauth.getRequestToken();
        System.out.println("RequestToken=" + tok);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("goto " + String.format(redirect, tok.getToken()));
        System.out.println("Input verifier :");
        Token accessToken = oauth.getAccessToken(tok, br.readLine());
        System.out.println("Token  =" + accessToken); 
    }
}

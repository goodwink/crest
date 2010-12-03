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

package org.codegist.crest.oauth;

import org.codegist.crest.DefaultRestService;
import org.codegist.crest.RestService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class OAuthAccessToken {
    public static void main(String[] args) throws IOException {
        RestService restService = new DefaultRestService();
        Token consumerToken = new Token("","");
        Map<String, Object> config = new HashMap<String, Object>() {{
            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL, "https://api.login.yahoo.com/oauth/v2/get_request_token");
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL, "https://api.login.yahoo.com/oauth/v2/get_token");
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_REFRESH_URL, "https://api.login.yahoo.com/oauth/v2/get_token");
        }};
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumerToken, config);

        Token tok = oauth.getRequestToken();
        System.out.println("RequestToken=" + tok);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("goto https://api.login.yahoo.com/oauth/v2/request_auth?oauth_token=" + tok.getToken());
        System.out.println("Input verifier :");
        Token accessToken = oauth.getAccessToken(tok, br.readLine());
        System.out.println("Token  =" + accessToken);
    }


}

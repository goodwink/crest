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

import org.codegist.crest.HttpRequest;
import org.codegist.crest.RestService;
import org.codegist.crest.oauth.OAuthenticatorV10.VariantProvider;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test values are taken from <a href="http://oauth.net/core/1.0/">http://oauth.net/core/1.0/</a>
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthenticatorV10Test {

    private final String requestTokUrl = "http://127.0.0.1";
    private final String accessTokUrl = "http://127.0.0.1";
    private final Token consumer = new Token("dpf43f3p2l4k3l03", "kd94hf93k423kf44");
    private final Token access = new Token("nnch734d00sl2jdk", "pfkkdhi9sl3r4s00");
    private final VariantProvider variantProvider = mock(VariantProvider.class);
    private final RestService restService = mock(RestService.class);
    {
        when(variantProvider.nonce()).thenReturn("kllo9940pd9333jh");
        when(variantProvider.timestamp()).thenReturn("1191242096");
    }


    @Test
    public void testGetRequestToken() throws IOException {
//        RestService restService = new DefaultRestService();
//        Map<String,Object> config = new HashMap<String, Object>(){{
//            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL, "https://api.login.yahoo.com/oauth/v2/get_request_token");
//            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL, "https://api.login.yahoo.com/oauth/v2/get_token");
//            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_REFRESH_URL, "https://api.login.yahoo.com/oauth/v2/get_token");
//        }};
//        Token consumer = new Token("dj0yJmk9UjNpdk1PM01WWkpWJmQ9WVdrOVNGSXhhV05CTlRJbWNHbzlNVEl4TVRBMU1ESTJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD05MQ--", "85ee749f97bc9d2390382c5b5309d31e73567b8c");
//        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config/*, variantProvider*/);
//        Token requestToken = oauth.getRequestToken();
//        System.out.println("RequestToken=" + requestToken);
//        System.out.println("goto https://api.login.yahoo.com/oauth/v2/request_auth?oauth_token=" + requestToken.getToken());
//        System.out.println("Verifier : ");
//        String verifier = null;
//        Token accessToken = oauth.getAccessToken(requestToken, verifier);
//        System.out.println("AccessToken=" + accessToken);
//        accessToken = oauth.refreshAccessToken(accessToken, "oauth_session_handle");
//        System.out.println("AccessToken=" + accessToken);
    }


    /**
     * Test with values from <a href="http://oauth.net/core/1.0/#anchor30">http://oauth.net/core/1.0/#anchor30</a>
     */
    @Test
    public void testAuthentificationHeaders() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, variantProvider);
        HttpRequest.Builder requestBuilder = new HttpRequest.Builder("http://photos.example.net/photos?file=vacation.jpg&size=original");

        oauth.sign(access, requestBuilder);
        HttpRequest request = requestBuilder.build();


        assertNotNull(request.getHeaders());
        assertEquals(1, request.getHeaders().size());
        assertNotNull(request.getHeaders().get("Authorization"));
        assertEquals("http://photos.example.net/photos?file=vacation.jpg&size=original", request.getUrlString(true));
        assertEquals("OAuth oauth_consumer_key=\"dpf43f3p2l4k3l03\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"1191242096\",oauth_nonce=\"kllo9940pd9333jh\",oauth_version=\"1.0\",oauth_token=\"nnch734d00sl2jdk\",oauth_signature=\"tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D\"", request.getHeaders().get("Authorization"));
    }
    /**
     * Test with values from <a href="http://oauth.net/core/1.0/#anchor30">http://oauth.net/core/1.0/#anchor30</a>
     */
    @Test
    public void testAuthentificationQueryString() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticator.CONFIG_OAUTH_PARAM_DEST, "url");
        }};
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        HttpRequest.Builder requestBuilder = new HttpRequest.Builder("http://photos.example.net/photos?file=vacation.jpg&size=original");

        oauth.sign(access, requestBuilder);
        HttpRequest request = requestBuilder.build();


        assertNotNull(request.getHeaders());
        assertEquals(0, request.getHeaders().size());
        assertEquals("http://photos.example.net/photos?file=vacation.jpg&size=original&oauth_consumer_key=dpf43f3p2l4k3l03&oauth_token=nnch734d00sl2jdk&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1191242096&oauth_nonce=kllo9940pd9333jh&oauth_version=1.0&oauth_signature=tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D", request.getUrlString(true));
    }


}

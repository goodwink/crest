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
import org.codegist.crest.HttpResponse;
import org.codegist.crest.RestService;
import org.codegist.crest.oauth.OAuthenticatorV10.VariantProvider;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Test values are taken from <a href="http://oauth.net/core/1.0/">http://oauth.net/core/1.0/</a>
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthenticatorV10Test {

    private final String requestTokUrl = "http://127.0.0.1/request";
    private final String accessTokUrl = "http://127.0.0.1/access";
    private final String refreshTokUrl = "http://127.0.0.1/refresh";
    private final Token consumer = new Token("dpf43f3p2l4k3l03", "kd94hf93k423kf44");
    private final Token access = new Token("nnch734d00sl2jdk", "pfkkdhi9sl3r4s00");
    private final VariantProvider variantProvider = mock(VariantProvider.class);
    private final RestService restService = mock(RestService.class);

    {
        when(variantProvider.nonce()).thenReturn("kllo9940pd9333jh");
        when(variantProvider.timestamp()).thenReturn("1191242096");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestTokenMissingParams(){
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, new HashMap<String, Object>(), variantProvider);
        oauth.getRequestToken();
    }
    @Test(expected = IllegalArgumentException.class)
    public void testRefreshTokenMissingParams(){
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, new HashMap<String, Object>(), variantProvider);
        oauth.refreshAccessToken(new Token("a","b"));
    }
    @Test(expected = IllegalArgumentException.class)
    public void testGetAccessTokenMissingParams(){
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, new HashMap<String, Object>(), variantProvider);
        oauth.getAccessToken(new Token("a","b"), "123");
    }
    static class HttpRequestMatcher extends ArgumentMatcher<HttpRequest> {
        private final HttpRequest control;

        HttpRequestMatcher(HttpRequest control) {
            this.control = control;
        }

        public boolean matches(Object req) {
            return control.equals(req);
        }
    }
    @Test
    public void testGetRequestTokenGET() throws IOException, URISyntaxException {
        RestService restService = mock(RestService.class);
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL, requestTokUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL_METHOD, "GET");
        }};
        HttpResponse response = mock(HttpResponse.class);
        when(response.asString()).thenReturn("");
        when(restService.exec(any(HttpRequest.class))).thenReturn(response);
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        oauth.getRequestToken();
        verify(restService).exec(argThat(new HttpRequestMatcher(new HttpRequest.Builder(requestTokUrl)
                .using("GET")
                .addQueryParam("oauth_consumer_key","dpf43f3p2l4k3l03")
                .addQueryParam("oauth_signature_method","HMAC-SHA1")
                .addQueryParam("oauth_timestamp","1191242096")
                .addQueryParam("oauth_nonce","kllo9940pd9333jh")
                .addQueryParam("oauth_version","1.0")
                .addQueryParam("oauth_callback","oob")
                .addQueryParam("oauth_signature","6TtAwFMnByClAdAxmA+feRdrtxA=")
                .build())));
    }
    @Test
    public void testGetRequestTokenPOST() throws IOException, URISyntaxException {
        RestService restService = mock(RestService.class);
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL, requestTokUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_REQUEST_URL_METHOD, "POST");
        }};
        HttpResponse response = mock(HttpResponse.class);
        when(response.asString()).thenReturn("");
        when(restService.exec(any(HttpRequest.class))).thenReturn(response);
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        oauth.getRequestToken();
        verify(restService).exec(argThat(new HttpRequestMatcher(new HttpRequest.Builder(requestTokUrl)
                .using("POST")
                .addFormParam("oauth_consumer_key","dpf43f3p2l4k3l03")
                .addFormParam("oauth_signature_method","HMAC-SHA1")
                .addFormParam("oauth_timestamp","1191242096")
                .addFormParam("oauth_nonce","kllo9940pd9333jh")
                .addFormParam("oauth_version","1.0")
                .addFormParam("oauth_callback","oob")
                .addFormParam("oauth_signature","KUkl3Z4v1zbpjyjtKdQ81nzWlkg=")
                .build())));
    }
    @Test
    public void testGetAccessTokenGET() throws IOException, URISyntaxException {
        RestService restService = mock(RestService.class);
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL, accessTokUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL_METHOD, "GET");
        }};
        HttpResponse response = mock(HttpResponse.class);
        when(response.asString()).thenReturn("");
        when(restService.exec(any(HttpRequest.class))).thenReturn(response);
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        oauth.getAccessToken(new Token("abc","cde"), "123");
        verify(restService).exec(argThat(new HttpRequestMatcher(new HttpRequest.Builder(accessTokUrl)
                .using("GET")
                .addQueryParam("oauth_consumer_key","dpf43f3p2l4k3l03")
                .addQueryParam("oauth_signature_method","HMAC-SHA1")
                .addQueryParam("oauth_timestamp","1191242096")
                .addQueryParam("oauth_nonce","kllo9940pd9333jh")
                .addQueryParam("oauth_version","1.0")
                .addQueryParam("oauth_token","abc")
                .addQueryParam("oauth_verifier","123")
                .addQueryParam("oauth_signature","3hSAQLbH48EoF/DCakzqqixn3q0=")
                .build())));
    }
    @Test
    public void testGetAccessTokenPOST() throws IOException, URISyntaxException {
        RestService restService = mock(RestService.class);
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL, accessTokUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_URL_METHOD, "POST");
        }};
        HttpResponse response = mock(HttpResponse.class);
        when(response.asString()).thenReturn("");
        when(restService.exec(any(HttpRequest.class))).thenReturn(response);
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        oauth.getAccessToken(new Token("abc","cde"), "123");
        verify(restService).exec(argThat(new HttpRequestMatcher(new HttpRequest.Builder(accessTokUrl)
                .using("POST")
                .addFormParam("oauth_consumer_key","dpf43f3p2l4k3l03")
                .addFormParam("oauth_signature_method","HMAC-SHA1")
                .addFormParam("oauth_timestamp","1191242096")
                .addFormParam("oauth_nonce","kllo9940pd9333jh")
                .addFormParam("oauth_version","1.0")
                .addFormParam("oauth_token","abc")
                .addFormParam("oauth_verifier","123")
                .addFormParam("oauth_signature","x8U4ouTFJDV+ITR5zjxw6HLZekI=")
                .build())));
    }
    @Test
    public void testRefreshAccessTokenGET() throws IOException, URISyntaxException {
        RestService restService = mock(RestService.class);
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_REFRESH_URL, refreshTokUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_REFRESH_URL_METHOD, "GET");
        }};
        HttpResponse response = mock(HttpResponse.class);
        when(response.asString()).thenReturn("");
        when(restService.exec(any(HttpRequest.class))).thenReturn(response);
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        oauth.refreshAccessToken(new Token("abc","cde", new HashMap<String, String>(){{put("extra","456");}}), "extra");
        verify(restService).exec(argThat(new HttpRequestMatcher(new HttpRequest.Builder(refreshTokUrl)
                .using("GET")
                .addQueryParam("oauth_consumer_key","dpf43f3p2l4k3l03")
                .addQueryParam("oauth_signature_method","HMAC-SHA1")
                .addQueryParam("oauth_timestamp","1191242096")
                .addQueryParam("oauth_nonce","kllo9940pd9333jh")
                .addQueryParam("oauth_version","1.0")
                .addQueryParam("oauth_token","abc")
                .addQueryParam("extra","456")
                .addQueryParam("oauth_signature","CLLlJYMnogkO3e1Z4OKnjcYaxSg=")
                .build())));
    }
    @Test
    public void testRefreshAccessTokenPOST() throws IOException, URISyntaxException {
        RestService restService = mock(RestService.class);
        Map<String,Object> config = new HashMap<String, Object>(){{
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_REFRESH_URL, refreshTokUrl);
            put(OAuthenticatorV10.CONFIG_TOKEN_ACCESS_REFRESH_URL_METHOD, "POST");
        }};
        HttpResponse response = mock(HttpResponse.class);
        when(response.asString()).thenReturn("");
        when(restService.exec(any(HttpRequest.class))).thenReturn(response);
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        oauth.refreshAccessToken(new Token("abc","cde", new HashMap<String, String>(){{put("extra","456");}}), "extra");
        verify(restService).exec(argThat(new HttpRequestMatcher(new HttpRequest.Builder(refreshTokUrl)
                .using("POST")
                .addFormParam("oauth_consumer_key","dpf43f3p2l4k3l03")
                .addFormParam("oauth_signature_method","HMAC-SHA1")
                .addFormParam("oauth_timestamp","1191242096")
                .addFormParam("oauth_nonce","kllo9940pd9333jh")
                .addFormParam("oauth_version","1.0")
                .addFormParam("oauth_token","abc")
                .addFormParam("extra","456")
                .addFormParam("oauth_signature","Ayymy4Mxku5qVba67IuyKWEZ8Zw=")
                .build())));
    }

    @Test
    public void testGenerateSignature() {
        OAuthenticatorV10 oauth = new OAuthenticatorV10(restService, new Token("token", "djr9rjt0jd78jf88"));
        assertEquals("djr9rjt0jd78jf88&jjd999tj88uiths3", oauth.generateSignature("jjd999tj88uiths3"));
        assertEquals("djr9rjt0jd78jf88&jjd99$tj88uiths3", oauth.generateSignature("jjd99$tj88uiths3"));
        assertEquals("djr9rjt0jd78jf88&", oauth.generateSignature(""));
    }


    /**
     * Test with values from <a href="http://oauth.net/core/1.0/#anchor30">http://oauth.net/core/1.0/#anchor30</a>
     */
    @Test
    public void testAuthentificationHeaders() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, new HashMap<String, Object>() {{
            put(OAuthenticatorV10.CONFIG_OAUTH_PARAM_DEST, "header");
        }}, variantProvider);
        HttpRequest.Builder requestBuilder = new HttpRequest.Builder("http://photos.example.net/photos?file=vacation.jpg&size=original");

        oauth.sign(access, requestBuilder);
        HttpRequest request = requestBuilder.build();


        assertNotNull(request.getHeaderParams());
        assertEquals(1, request.getHeaderParams().size());
        assertNotNull(request.getHeaderParams().get("Authorization"));
        assertEquals("http://photos.example.net/photos?file=vacation.jpg&size=original", request.getUrlString(true));
        assertEquals("OAuth oauth_consumer_key=\"dpf43f3p2l4k3l03\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"1191242096\",oauth_nonce=\"kllo9940pd9333jh\",oauth_version=\"1.0\",oauth_token=\"nnch734d00sl2jdk\",oauth_signature=\"tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D\"", request.getHeaderParams().get("Authorization"));
    }

    /**
     * Test with values from <a href="http://oauth.net/core/1.0/#anchor30">http://oauth.net/core/1.0/#anchor30</a>
     */
    @Test
    public void testAuthentificationQueryString() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        Map<String, Object> config = new HashMap<String, Object>() {{
            put(OAuthenticatorV10.CONFIG_OAUTH_PARAM_DEST, "url");
        }};
        OAuthenticator oauth = new OAuthenticatorV10(restService, consumer, config, variantProvider);
        HttpRequest.Builder requestBuilder = new HttpRequest.Builder("http://photos.example.net/photos?file=vacation.jpg&size=original");

        oauth.sign(access, requestBuilder);
        HttpRequest request = requestBuilder.build();


        assertNotNull(request.getHeaderParams());
        assertEquals(0, request.getHeaderParams().size());
        assertEquals("http://photos.example.net/photos?file=vacation.jpg&size=original&oauth_consumer_key=dpf43f3p2l4k3l03&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1191242096&oauth_nonce=kllo9940pd9333jh&oauth_version=1.0&oauth_token=nnch734d00sl2jdk&oauth_signature=tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D", request.getUrlString(true));
    }


}

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
import org.codegist.crest.oauth.OAuthenticatorV10.VariantProvider;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test values are taken from <a href="http://oauth.net/core/1.0/">http://oauth.net/core/1.0/</a>
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthenticatorV10Test {

    private final Token consumer = new Token("dpf43f3p2l4k3l03", "kd94hf93k423kf44");
    private final Token access = new Token("nnch734d00sl2jdk", "pfkkdhi9sl3r4s00");
    private final VariantProvider variantProvider = mock(VariantProvider.class);
    {
        when(variantProvider.nonce()).thenReturn("kllo9940pd9333jh");
        when(variantProvider.timestamp()).thenReturn("1191242096");
    }


    /**
     * Test with values from <a href="http://oauth.net/core/1.0/#anchor30">http://oauth.net/core/1.0/#anchor30</a>
     */
    @Test
    public void testAuthentificationHeaders() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        OAuthenticator oauth = new OAuthenticatorV10(true, consumer, access, variantProvider);
        HttpRequest.Builder requestBuilder = new HttpRequest.Builder("http://photos.example.net/photos?file=vacation.jpg&size=original");

        oauth.process(requestBuilder);
        HttpRequest request = requestBuilder.build();


        assertNotNull(request.getHeaders());
        assertEquals(1, request.getHeaders().size());
        assertNotNull(request.getHeaders().get("Authorization"));
        assertEquals("http://photos.example.net/photos?file=vacation.jpg&size=original", request.getUrlString(true));
        assertEquals("OAuth oauth_consumer_key=\"dpf43f3p2l4k3l03\",oauth_token=\"nnch734d00sl2jdk\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"1191242096\",oauth_nonce=\"kllo9940pd9333jh\",oauth_version=\"1.0\",oauth_signature=\"tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D\"", request.getHeaders().get("Authorization"));
    }
    /**
     * Test with values from <a href="http://oauth.net/core/1.0/#anchor30">http://oauth.net/core/1.0/#anchor30</a>
     */
    @Test
    public void testAuthentificationQueryString() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        OAuthenticator oauth = new OAuthenticatorV10(false, consumer, access, variantProvider);
        HttpRequest.Builder requestBuilder = new HttpRequest.Builder("http://photos.example.net/photos?file=vacation.jpg&size=original");

        oauth.process(requestBuilder);
        HttpRequest request = requestBuilder.build();


        assertNotNull(request.getHeaders());
        assertEquals(0, request.getHeaders().size());
        assertEquals("http://photos.example.net/photos?file=vacation.jpg&size=original&oauth_consumer_key=dpf43f3p2l4k3l03&oauth_token=nnch734d00sl2jdk&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1191242096&oauth_nonce=kllo9940pd9333jh&oauth_version=1.0&oauth_signature=tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D", request.getUrlString(true));
    }


}

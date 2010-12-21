/*
 * Copyright 2010 CodeGist.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpRequestTest {


    @Test
    public void testHttpRequestUriNoParams() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest request = new HttpRequest.Builder("http://127.0.0.1:8080/test").build();
        assertEquals(new URI("http://127.0.0.1:8080/test"), request.getUri());
        assertEquals(new URL("http://127.0.0.1:8080/test"), request.getUrl(true));
        assertEquals(("http://127.0.0.1:8080/test"), request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1:8080/test"), request.getUrl(false));
        assertEquals(("http://127.0.0.1:8080/test"), request.getUrlString(false));
        request = new HttpRequest.Builder("http://127.0.0.1:8080/test?").build();
        assertEquals(new URI("http://127.0.0.1:8080/test"), request.getUri());
        assertEquals(new URL("http://127.0.0.1:8080/test"), request.getUrl(true));
        assertEquals(("http://127.0.0.1:8080/test"), request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1:8080/test"), request.getUrl(false));
        assertEquals(("http://127.0.0.1:8080/test"), request.getUrlString(false));
    }
    @Test
    public void testHttpRequestUriWithParams() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest request = new HttpRequest.Builder("http://127.0.0.1:8080/test?p1=v1").addQueryParam("p2","v2").build();
        assertEquals(new URI("http://127.0.0.1:8080/test"), request.getUri());
        assertEquals(new URL("http://127.0.0.1:8080/test?p1=v1&p2=v2"), request.getUrl(true));
        assertEquals(("http://127.0.0.1:8080/test?p1=v1&p2=v2"), request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1:8080/test"), request.getUrl(false));
        assertEquals(("http://127.0.0.1:8080/test"), request.getUrlString(false));
    }
    @Test
    public void testHttpRequestEquals() throws URISyntaxException {
        HttpRequest request1 = HttpRequestBuilderTest.getFull().build();
        HttpRequest request2 = HttpRequestBuilderTest.getFull().build();
        assertEquals(request1, request2);
    }
    @Test
    public void testHttpRequestHashCode() throws URISyntaxException {
        HttpRequest request1 = HttpRequestBuilderTest.getFull().build();
        HttpRequest request2 = HttpRequestBuilderTest.getFull().build();
        assertEquals(request1.hashCode(), request2.hashCode());
    }

}

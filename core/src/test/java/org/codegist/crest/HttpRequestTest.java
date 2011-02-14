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
import java.util.LinkedHashMap;

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
    }
    @Test
    public void testHttpRequestUriWithParams() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest request = new HttpRequest.Builder("http://127.0.0.1:8080/test").addQueryParam("p2","v2").build();
        assertEquals(new URI("http://127.0.0.1:8080/test"), request.getUri());
        assertEquals(new URL("http://127.0.0.1:8080/test?p2=v2"), request.getUrl(true));
        assertEquals(("http://127.0.0.1:8080/test?p2=v2"), request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1:8080/test"), request.getUrl(false));
        assertEquals(("http://127.0.0.1:8080/test"), request.getUrlString(false));
    }

    private static final String PLACEHOLDERS_URI =  "http://127.0.0.1:8080/{p1}/{p2}/{p1}/test";

    @Test(expected = IllegalStateException.class)
    public void testHttpRequestWithUnresolvedPathPlaceholders() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        new HttpRequest.Builder(PLACEHOLDERS_URI)
                .addQueryParam("q1", "qv1")
                .addQueryParam("q", "qv2")
                .addQueryParam("q3", "qv3")
                .build();
    }

    @Test
    public void testHttpRequestPath() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest req = new HttpRequest.Builder("http://127.0.0.1:8080/{p1}///{p2}/{p1}////test///")
                .addPathParam("p1", "pv1")
                .addPathParam("p2", "pv2")
                .addQueryParam("q1", "qv1")
                .addQueryParam("q2", "qv2")
                .addQueryParam("q3", "")
                .addQueryParam("q4", "qv4")
                .build();
        assertEquals("http://127.0.0.1:8080/pv1/pv2/pv1/test/?q1=qv1&q2=qv2&q3=&q4=qv4", req.getUrlString(true));
    }


    @Test
    public void testHttpRequestWithPlaceholders() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest req = new HttpRequest.Builder(PLACEHOLDERS_URI)
                .addPathParam("p1", "pv1")
                .addPathParam("p2", "pv2")
                .addQueryParam("q1", "qv1")
                .addQueryParam("q2", "qv2")
                .addQueryParam("q3", "")
                .addQueryParam("q4", "qv4")
                .build();
        assertEquals(new URL("http://127.0.0.1:8080/pv1/pv2/pv1/test"), req.getUrl(false));
        assertEquals(new URI("http://127.0.0.1:8080/pv1/pv2/pv1/test"), req.getUri());
        assertEquals(new URL("http://127.0.0.1:8080/pv1/pv2/pv1/test?q1=qv1&q2=qv2&q3=&q4=qv4"), req.getUrl(true));
        assertEquals(new LinkedHashMap<String,String>(){{
            put("q1","qv1");
            put("q2","qv2");
            put("q3","");
            put("q4","qv4");
        }}, req.getQueryParams());

    }

}

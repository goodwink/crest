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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpRequestBuilderTest {


    @Test
    public void testTimeouts() throws URISyntaxException {
        HttpRequest request = new HttpRequest.Builder("http://test").timeoutAfter(100l).build();
        assertEquals(Long.valueOf(100l), request.getSocketTimeout());
        assertEquals(Long.valueOf(100l), request.getConnectionTimeout());
    }

    @Test
    public void testAccessMethods() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest.Builder request = getFull("http://127.0.0.1/{1}/{0}/?p1=v1&p2={0}&p3={1}&p20={1}");
        assertEquals("http://127.0.0.1/v3/v2/", request.getBaseUri());
        assertEquals("utf-8", request.getEncoding());
        assertEquals("PUT", request.getMeth());
        assertEquals(new URL("http://127.0.0.1/v3/v2/?p1=v1&p2=v2&p3=v3&p20=v3&p4=v4&p5=v5&p6=v6"), request.getUrl(true));
        assertEquals(new URL("http://127.0.0.1/v3/v2/"), request.getUrl(false));
        assertEquals(("http://127.0.0.1/v3/v2/?p1=v1&p2=v2&p3=v3&p20=v3&p4=v4&p5=v5&p6=v6"), request.getUrlString(true));
        assertEquals(("http://127.0.0.1/v3/v2/"), request.getUrlString(false));
        assertEquals(Arrays.asList("p2"), request.getQueryParamNameByPlaceholderIndex(0));
        assertEquals(Arrays.asList("p3","p20"), request.getQueryParamNameByPlaceholderIndex(1));
    }


    @Test
    public void testFullBuild() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest request = getFull().build();
        assertEquals("PUT", request.getMeth());
        assertEquals(new URI("http://127.0.0.1/v3/v2/"), request.getUri());
        assertEquals("http://127.0.0.1/v3/v2/?p1=v1&p2=v2&p3=v3&p4=v4&p5=v5&p6=v6", request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1/v3/v2/?p1=v1&p2=v2&p3=v3&p4=v4&p5=v5&p6=v6"), request.getUrl(true));
        assertEquals(Long.valueOf(10l), request.getConnectionTimeout());
        assertEquals(Long.valueOf(20l), request.getSocketTimeout());
        assertEquals("utf-8", request.getEncoding());
        assertEquals(new LinkedHashMap<String, String>() {{
            put("h1", "vh1");
            put("h2", "vh2");
            put("h3", "vh3");
            put("h4", "vh4");
        }}, request.getHeaders());
        assertEquals(new LinkedHashMap<String, String>() {{
            put("p1", "v1");
            put("p2", "v2");
            put("p3", "v3");
            put("p4", "v4");
            put("p5", "v5");
            put("p6", "v6");
        }}, request.getQueryParams());
        assertEquals(new LinkedHashMap<String, Object>() {{
            put("b1", "vb1");
            put("b2", "vb2");
            put("b3", "vb3");
            put("b4", "vb4");
        }}, request.getBodyParams());
    }

    @Test
    public void testFullBuild2() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest request = getFull()
                .addQueryParams(new LinkedHashMap<String, String>() {{
                    put("dropped1", "v4");
                    put("dropped2", "v5");
                }})
                .addQueryParam("dropped3", "v6")
                .setQueryString(new LinkedHashMap<String, String>() {{
                    put("p3", "newv3");
                    put("p4", "v4");
                    put("p5", "v5");
                }}).build();
        assertEquals("PUT", request.getMeth());
        assertEquals(new URI("http://127.0.0.1/v3/v2/"), request.getUri());
        assertEquals("http://127.0.0.1/v3/v2/?p3=newv3&p4=v4&p5=v5", request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1/v3/v2/?p3=newv3&p4=v4&p5=v5"), request.getUrl(true));
        assertEquals(Long.valueOf(10l), request.getConnectionTimeout());
        assertEquals(Long.valueOf(20l), request.getSocketTimeout());
        assertEquals("utf-8", request.getEncoding());
        assertEquals(Charset.forName("utf-8"), request.getEncodingAsCharset());
        assertEquals(new LinkedHashMap<String, String>() {{
            put("h1", "vh1");
            put("h2", "vh2");
            put("h3", "vh3");
            put("h4", "vh4");
        }}, request.getHeaders());
        assertEquals(new LinkedHashMap<String, String>() {{
            put("p3", "newv3");
            put("p4", "v4");
            put("p5", "v5");
        }}, request.getQueryParams());
        assertEquals(new LinkedHashMap<String, Object>() {{
            put("b1", "vb1");
            put("b2", "vb2");
            put("b3", "vb3");
            put("b4", "vb4");
        }}, request.getBodyParams());
    }

    @Test
    public void testDefaultBuild1() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest.Builder builder = new HttpRequest.Builder("http://127.0.0.1", "ISO-8859-1");
        HttpRequest request = builder.build();
        assertEquals("GET", request.getMeth());
        assertEquals(new URI("http://127.0.0.1"), request.getUri());
        assertEquals("http://127.0.0.1", request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1"), request.getUrl(true));
        assertNull(null, request.getConnectionTimeout());
        assertNull(null, request.getSocketTimeout());
        assertEquals("ISO-8859-1", request.getEncoding());
        assertEquals(Charset.forName("ISO-8859-1"), request.getEncodingAsCharset());
        assertEquals(Collections.<Object, Object>emptyMap(), request.getHeaders());
        assertEquals(Collections.<Object, Object>emptyMap(), request.getQueryParams());
        assertEquals(Collections.<Object, Object>emptyMap(), request.getBodyParams());
    }

    @Test
    public void testDefaultBuild2() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
        HttpRequest.Builder builder = new HttpRequest.Builder("http://127.0.0.1", "iso");
        HttpRequest request = builder.build();
        assertEquals("GET", request.getMeth());
        assertEquals(new URI("http://127.0.0.1"), request.getUri());
        assertEquals("http://127.0.0.1", request.getUrlString(true));
        assertEquals(new URL("http://127.0.0.1"), request.getUrl(true));
        assertNull(null, request.getConnectionTimeout());
        assertNull(null, request.getSocketTimeout());
        assertEquals("iso", request.getEncoding());
        assertEquals(Collections.<Object, Object>emptyMap(), request.getHeaders());
        assertEquals(Collections.<Object, Object>emptyMap(), request.getQueryParams());
        assertEquals(Collections.<Object, Object>emptyMap(), request.getBodyParams());
    }

    @Test(expected = NullPointerException.class)
    public void testDefaultBuildFailure1() throws URISyntaxException {
        new HttpRequest.Builder(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDefaultBuildFailure2() throws URISyntaxException {
        new HttpRequest.Builder(null, "utf-8");
    }


    static HttpRequest.Builder getFull() throws URISyntaxException {
        return getFull("http://127.0.0.1/{1}/{0}/?p1=v1&p2={0}&p3={1}");
    }
    static HttpRequest.Builder getFull(String url) throws URISyntaxException {
        return new HttpRequest.Builder(url, "utf-8")

                .addBodyParams(new LinkedHashMap<String, Object>() {{
                    put("dropped1", "value2");
                    put("dropped2", "value3");
                }})
                .addBodyParam("dropped3", "value4")
                .setBodyParams(new LinkedHashMap<String, Object>() {{
                    put("b1", "vb1");
                }})
                .addBodyParams(new LinkedHashMap<String, Object>() {{
                    put("b2", "vb2");
                    put("b3", "vb3");
                }})
                .addBodyParam("b4", "vb4")
                .addQueryParams(new LinkedHashMap<String, String>() {{
                    put("p4", "v4");
                    put("p5", "v5");
                }})
                .addQueryParam("p6", "v6")

                .addHeaders(new LinkedHashMap<String, String>() {{
                    put("dropped1", "vh2");
                    put("dropped2", "vh3");
                }})
                .addHeader("dropped3", "vh4")
                .setHeaders(new LinkedHashMap<String, String>() {{
                    put("h1", "vh1");
                }})
                .addHeaders(new LinkedHashMap<String, String>() {{
                    put("h2", "vh2");
                    put("h3", "vh3");
                }})
                .addHeader("h4", "vh4")

                .timeoutConnectionAfter(10l)
                .timeoutSocketAfter(20l)
                .using("PUT")
                .replacePlaceholderInUri(1, "v3")
                .replacePlaceholderInUri(0, "v2");
    }

}

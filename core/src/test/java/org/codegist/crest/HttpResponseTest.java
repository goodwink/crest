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

import org.codegist.common.io.IOs;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpResponseTest {

    @Test
    public void testResponseValues() throws UnsupportedEncodingException {
        HttpRequest req = mock(HttpRequest.class);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Type", Arrays.asList("text/javascript; charset=utf-8"));
        headers.put("Content-Encoding", Arrays.asList("my-encoding"));

        HttpResponse res = new HttpResponse(req, 200, headers, null);
        assertEquals(req, res.getRequest());
        assertEquals(Charset.forName("utf-8"), res.getCharset());
        assertEquals("my-encoding", res.getContentEncoding());
        assertEquals("text/javascript", res.getMimeType());
        assertEquals("text/javascript; charset=utf-8", res.getHeader("Content-Type").get(0));

    }

    @Test
    public void testContentType() throws IOException {
        HttpResponse res = buildResponseForStream("charset=utf-8");
        assertEquals(Charset.forName("utf-8"), res.getCharset());
        assertEquals("text/html", res.getMimeType());

        res = buildResponseForStream(";charset=utf-8");
        assertEquals(Charset.forName("utf-8"), res.getCharset());
        assertEquals("text/html", res.getMimeType());

        res = buildResponseForStream("text/javascript; charset=utf-8");
        assertEquals(Charset.forName("utf-8"), res.getCharset());
        assertEquals("text/javascript", res.getMimeType());

        res = buildResponseForStream("text/javascript;charset=utf-8");
        assertEquals(Charset.forName("utf-8"), res.getCharset());
        assertEquals("text/javascript", res.getMimeType());

        res = buildResponseForStream("charset=ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), res.getCharset());
        assertEquals("text/html", res.getMimeType());

        res = buildResponseForStream("");
        assertEquals(Charset.forName("ISO-8859-1"), res.getCharset());
        assertEquals("text/html", res.getMimeType());

        res = buildResponseForStream("text/javascript;");
        assertEquals(Charset.forName("ISO-8859-1"), res.getCharset());
        assertEquals("text/javascript", res.getMimeType());

        res = buildResponseForStream("text/javascript");
        assertEquals(Charset.forName("ISO-8859-1"), res.getCharset());
        assertEquals("text/javascript", res.getMimeType());
    }

    @Test
    public void testGZip() throws IOException {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Encoding", Arrays.asList("gzip"));
        final String original = "data";

        HttpResponse response = new HttpResponse(mock(HttpRequest.class), 201, headers, new HttpResource() {
            private final InputStream stream;
            {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                GZIPOutputStream out = new GZIPOutputStream(bout);
                out.write(original.getBytes("ISO-8859-1"));
                out.close();
                stream = new ByteArrayInputStream(bout.toByteArray());
            }
            public InputStream getContent() throws HttpException {
                return stream;
            }

            public void release() throws HttpException {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new HttpException(e);
                }
            }
        });
        String data = IOs.toString(response.asReader());
        assertEquals(original, data);
    }

    @Test
    public void testMultipleReadsSuccess() throws IOException {
        HttpResponse response = buildResponseForStream("data".getBytes("utf-8"), "charset=utf-8");
        assertEquals("data", response.asString());
        assertEquals("data", response.asString());
    }

    @Test
    public void testMultipleReadsFailure1() throws IOException {
        HttpResponse response = buildResponseForStream("data".getBytes("utf-8"), "charset=utf-8");
        assertEquals("data", IOs.toString(response.asStream()));
        assertEquals("", response.asString());
    }

    @Test
    public void testMultipleReadsFailure2() throws IOException {
        HttpResponse response = buildResponseForStream("data".getBytes("utf-8"), "charset=utf-8");
        assertEquals("data", IOs.toString(response.asReader()));
        assertEquals("", response.asString());
    }

    @Test
    public void testMultipleReadsFailure3() throws IOException {
        HttpResponse response = buildResponseForStream("data".getBytes("utf-8"), "charset=utf-8");
        assertEquals("data", IOs.toString(response.asReader()));
        assertEquals("", IOs.toString(response.asReader()));
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleReadsFailure4() throws IOException {
        HttpResponse response = buildResponseForStream("data".getBytes("utf-8"), "charset=utf-8");
        assertEquals("data", response.asString());
        response.asReader();
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleReadsFailure5() throws IOException {
        HttpResponse response = buildResponseForStream("data".getBytes("utf-8"), "charset=utf-8");
        assertEquals("data", response.asString());
        response.asStream();
    }


    @Test
    public void testAsStreamSuccess1() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "charset=utf-8");
        String data = IOs.toString(response.asStream(), response.getCharset());
        assertEquals(original, data);
    }

    @Test
    public void testAsStreamSuccess2() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "charset=ISO-8859-1");
        String data = IOs.toString(response.asStream(), response.getCharset());
        assertEquals(original, data);
    }

    @Test
    public void testAsStreamSuccess3() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "");
        String data = IOs.toString(response.asStream(), response.getCharset());
        assertEquals(original, data);
    }

    @Test
    public void testAsStreamFailure1() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "charset=utf-8");
        String data = IOs.toString(response.asStream(), response.getCharset());
        assertFalse(original.equals(data));
    }

    @Test
    public void testAsStreamFailure2() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "charset=ISO-8859-1");
        String data = IOs.toString(response.asStream(), response.getCharset());
        assertFalse(original.equals(data));
    }

    @Test
    public void testAsStreamFailure3() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "");
        String data = IOs.toString(response.asStream(), response.getCharset());
        assertFalse(original.equals(data));
    }

    @Test
    public void testAsReaderSuccess1() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "charset=utf-8");
        String data = IOs.toString(response.asReader());
        assertEquals(original, data);
    }

    @Test
    public void testAsReaderSuccess2() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "charset=ISO-8859-1");
        String data = IOs.toString(response.asReader());
        assertEquals(original, data);
    }

    @Test
    public void testAsReaderSuccess3() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "");
        String data = IOs.toString(response.asReader());
        assertEquals(original, data);
    }

    @Test
    public void testAsReaderFailure1() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "charset=utf-8");
        String data = IOs.toString(response.asReader());
        assertFalse(original.equals(data));
    }

    @Test
    public void testAsReaderFailure2() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "charset=ISO-8859-1");
        String data = IOs.toString(response.asReader());
        assertFalse(original.equals(data));
    }

    @Test
    public void testAsReaderFailure3() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "");
        String data = IOs.toString(response.asReader());
        assertFalse(original.equals(data));
    }

    @Test
    public void testAsStringSuccess1() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "charset=utf-8");
        assertEquals(original, response.asString());
    }

    @Test
    public void testAsStringSuccess2() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "charset=ISO-8859-1");
        assertEquals(original, response.asString());
    }

    @Test
    public void testAsStringSuccess3() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "");
        assertEquals(original, response.asString());
    }

    @Test
    public void testAsStringFailure1() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("ISO-8859-1"), "charset=utf-8");
        assertFalse(original.equals(response.asString()));
    }

    @Test
    public void testAsStringFailure2() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "charset=ISO-8859-1");
        assertFalse(original.equals(response.asString()));
    }

    @Test
    public void testAsStringFailure3() throws IOException {
        String original = "hello world£";
        HttpResponse response = buildResponseForStream(original.getBytes("utf-8"), "");
        assertFalse(original.equals(response.asString()));
    }


    private static HttpResponse buildResponseForStream(String contentType) throws IOException {
        return buildResponseForStream(null, contentType);
    }

    private static HttpResponse buildResponseForStream(byte[] data, String contentType) throws IOException {
        HttpRequest request = mock(HttpRequest.class);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Type", Arrays.asList(contentType));
        final InputStream stream = data != null ? new ByteArrayInputStream(data) : null;
        return new HttpResponse(request, 212, headers, stream == null ? null : (new HttpResource() {
            public InputStream getContent() throws HttpException {
                return stream;
            }

            public void release() throws HttpException {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new HttpException(e);
                }
            }
        }));
    }
}

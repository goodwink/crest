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

import org.codegist.common.collect.Maps;
import org.codegist.common.io.Files;
import org.codegist.common.io.IOs;
import org.codegist.common.net.Urls;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public abstract class RestServiceContractTest {

    private ServletTester tester;
    private String baseUrl;

    public abstract RestService getRestService();

    @Before
    public void setup() throws Exception {
        tester = new ServletTester();
        tester.setContextPath("/");
        tester.addServlet(SimpleMethodsServlet.class, "/test/simple");
        tester.addServlet(UploadFileServlet.class, "/test/upload/file");
        tester.addServlet(UploadInputStreamServlet.class, "/test/upload/inputstream");
        tester.addServlet(UploadMixedServlet.class, "/test/upload/mixed");
        tester.addFilter(org.mortbay.servlet.MultiPartFilter.class, "/*", 0);
        tester.start();
        baseUrl = tester.createSocketConnector(true);
    }

    @After
    public void after() throws Exception {
        tester.stop();
    }

    private static void write(HttpServletRequest req, HttpServletResponse resp, String val, int code) throws IOException, ServletException {
        resp.setStatus(code);
        if (val == null) return;
        PrintWriter w = null;
        try {
            w = resp.getWriter();
            w.write(val);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    public static class UploadMixedServlet extends HttpServlet {
        protected void doPostOrPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertTrue(req.getContentType().startsWith("multipart/form-data;"));
            for (Map.Entry<String, Object> entry : SimpleMethodsServlet.EXPECTED_SIMPLE_BODY.entrySet()) {
                assertEquals(entry.getValue().toString(), req.getParameter(entry.getKey()));
            }
            for (Map.Entry<String, Object> file : UploadFileServlet.EXPECTED_FILES.entrySet()) {
                assertNotNull(req.getAttribute(file.getKey()));
                File upload = (File) req.getAttribute(file.getKey());
                upload.deleteOnExit();
                assertTrue(Arrays.equals(Files.toByteArray((File) file.getValue()), Files.toByteArray(upload)));
            }
            for (Map.Entry<String, Object> file : UploadInputStreamServlet.EXPECTED_INPUTSTREAM.entrySet()) {
                assertNotNull(req.getAttribute(file.getKey()));
                File upload = (File) req.getAttribute(file.getKey());
                upload.deleteOnExit();
                assertTrue(Arrays.equals(IOs.toByteArray(RestServiceContractTest.class.getResourceAsStream((String) file.getValue()), true), Files.toByteArray(upload)));
            }

            for (Map.Entry<String, String> entry : SimpleMethodsServlet.EXPECTED_SIMPLE_QUERY.entrySet()) {
                assertEquals(entry.getValue(), req.getParameter(entry.getKey()));
            }
            for (Map.Entry<String, String> entry : SimpleMethodsServlet.EXPECTED_SIMPLE_HEADERS.entrySet()) {
                assertEquals(entry.getValue(), req.getHeader(entry.getKey()));
            }
            RestServiceContractTest.write(req, resp, "OK", 200);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPostOrPut(req, resp);
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPostOrPut(req, resp);
        }
    }



    public static class UploadFileServlet extends HttpServlet {

        private static File copyToFS(String name, InputStream is) throws IOException {
            File f = File.createTempFile(name, null);
            f.deleteOnExit();
            FileOutputStream out = new FileOutputStream(f);
            IOs.copy(is, out);
            out.close();
            return f;
        }

        static final Map<String, Object> EXPECTED_FILES = Maps.unmodifiable(new HashMap<String, Object>() {{
            try {
                put("uffile1", copyToFS("UploadTest1.txt", RestServiceContractTest.class.getResourceAsStream("UploadTest1.txt")));
                put("uffile2", copyToFS("upload.gif", RestServiceContractTest.class.getResourceAsStream("upload.gif")));
            } catch (Exception e) {
                throw new CRestException(e);
            }
        }});

//        @Override
//        public void destroy() {
//            for (Map.Entry<String, Object> file : EXPECTED_FILES.entrySet()) {
//                ((File)file.getValue()).delete();
//            }
//        }

        protected void doPostOrPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertTrue(req.getContentType().startsWith("multipart/form-data;"));
            for (Map.Entry<String, Object> fileEntry : EXPECTED_FILES.entrySet()) {
                assertNotNull(req.getAttribute(fileEntry.getKey()));
                File upload = (File) req.getAttribute(fileEntry.getKey());
                upload.deleteOnExit();
                assertTrue(Arrays.equals(Files.toByteArray((File) fileEntry.getValue()), Files.toByteArray(upload)));
            }
            RestServiceContractTest.write(req, resp, "OK", 200);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPostOrPut(req, resp);
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPostOrPut(req, resp);
        }
    }

    public static class UploadInputStreamServlet extends HttpServlet {

        static final Map<String, Object> EXPECTED_INPUTSTREAM = Maps.unmodifiable(new HashMap<String, Object>() {{
            try {
                put("usfile1", "UploadTest1.txt");
                put("usfile2", "upload.gif");
            } catch (Exception e) {
                throw new CRestException(e);
            }
        }});

        protected void doPostOrPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertTrue(req.getContentType().startsWith("multipart/form-data;"));
            for (Map.Entry<String, Object> fileEntry : EXPECTED_INPUTSTREAM.entrySet()) {
                assertNotNull(req.getAttribute(fileEntry.getKey()));
                File upload = (File) req.getAttribute(fileEntry.getKey());
                upload.deleteOnExit();
                assertTrue(Arrays.equals(IOs.toByteArray(RestServiceContractTest.class.getResourceAsStream((String) fileEntry.getValue()), true), Files.toByteArray(upload)));
            }
            RestServiceContractTest.write(req, resp, "OK", 200);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPostOrPut(req, resp);
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPostOrPut(req, resp);
        }
    }

    public static class SimpleMethodsServlet extends HttpServlet {

        static final Map<String, Object> EXPECTED_SIMPLE_BODY = Maps.unmodifiable(new HashMap<String, Object>() {{
            put("body1", "bodys£ tring");
            put("body2", 15);
        }});
        static final Map<String, String> EXPECTED_SIMPLE_QUERY = Maps.unmodifiable(new HashMap<String, String>() {{
            put("query1", "query string");
            put("query2", "£12");
        }});
        static final Map<String, String> EXPECTED_SIMPLE_HEADERS = Maps.unmodifiable(new HashMap<String, String>() {{
            put("header1", "my header");
            put("header2", "my header 32");
        }});

        private void writeSimpleReceived(HttpServletRequest req, HttpServletResponse resp, int expectedStatusCode) throws IOException, ServletException {
            write(req, resp, null, expectedStatusCode);
        }

        private void writeSimpleReceived(HttpServletRequest req, HttpServletResponse resp, String meth) throws IOException, ServletException {
            write(req, resp, meth + ".simple.received", 200);
        }


        private void write(HttpServletRequest req, HttpServletResponse resp, String val, int code) throws IOException, ServletException {

            if (Arrays.asList("POST", "PUT").contains(req.getMethod())) {
                Map<String, String> body = Urls.parseQueryString(IOs.toString(req.getInputStream(), req.getCharacterEncoding()), req.getCharacterEncoding());
                assertEquals("application/x-www-form-urlencoded; charset=utf-8", req.getContentType().toLowerCase());
                for (Map.Entry<String, Object> entry : EXPECTED_SIMPLE_BODY.entrySet()) {
                    assertEquals(entry.getValue().toString(), body.get(entry.getKey()));
                }
            }

            for (Map.Entry<String, String> entry : EXPECTED_SIMPLE_QUERY.entrySet()) {
                assertEquals(entry.getValue(), req.getParameter(entry.getKey()));
            }
            for (Map.Entry<String, String> entry : EXPECTED_SIMPLE_HEADERS.entrySet()) {
                assertEquals(entry.getValue(), req.getHeader(entry.getKey()));
            }

            RestServiceContractTest.write(req, resp, val, code);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            writeSimpleReceived(req, resp, "get");
        }

        @Override
        protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            writeSimpleReceived(req, resp, 200);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            writeSimpleReceived(req, resp, "post");
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            writeSimpleReceived(req, resp, "put");
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            writeSimpleReceived(req, resp, "delete");
        }
    }


    /**
     * Test that the given rest interface correctly passes all queryString/body/headers
     *
     * @throws Exception
     */
    @Test
    public void testSimpleRequests() throws Exception {
        for (HttpMethod meth : HttpMethod.values()) {
            String tag = "HttpMethod failed : " + meth;
            HttpResponse res = getRestService().exec(new HttpRequest.Builder(baseUrl + "/test/simple")
                    .using(meth)
                    .addBodyParams(SimpleMethodsServlet.EXPECTED_SIMPLE_BODY)
                    .addQueryParams(SimpleMethodsServlet.EXPECTED_SIMPLE_QUERY)
                    .addHeaders(SimpleMethodsServlet.EXPECTED_SIMPLE_HEADERS)
                    .build());
            assertNotNull(tag, res);
            assertEquals(tag, 200, res.getStatusCode());
            if (!HttpMethod.HEAD.equals(meth)) {
                assertEquals(tag, meth.toString().toLowerCase() + ".simple.received", res.asString());
            }
        }
    }

    @Test
    public void testUploadRequest_File() throws Exception {
        HttpMethod[] meths = new HttpMethod[]{HttpMethod.PUT, HttpMethod.POST};
        for (HttpMethod m : meths) {
            HttpResponse res = getRestService().exec(new HttpRequest.Builder(baseUrl + "/test/upload/file")
                    .using(m)
                    .addBodyParams(UploadFileServlet.EXPECTED_FILES)
                    .build());
            assertEquals(200, res.getStatusCode());
            assertEquals("OK", res.asString());
        }
    }

    @Test
    public void testUploadRequest_InputStream() throws Exception {
        HttpMethod[] meths = new HttpMethod[]{HttpMethod.PUT, HttpMethod.POST};
        for (HttpMethod m : meths) {
            HttpRequest.Builder b = new HttpRequest.Builder(baseUrl + "/test/upload/inputstream").using(m);

            for (Map.Entry<String, Object> e : UploadInputStreamServlet.EXPECTED_INPUTSTREAM.entrySet()) {
                b.addBodyParam(e.getKey(), RestServiceContractTest.class.getResourceAsStream(e.getValue().toString()));
            }

            HttpResponse res = getRestService().exec(b.build());
            assertEquals(200, res.getStatusCode());
            assertEquals("OK", res.asString());
        }
    }

    @Test
    public void testUploadRequest_Mix() throws Exception {
        HttpMethod[] meths = new HttpMethod[]{HttpMethod.PUT, HttpMethod.POST};
        for (HttpMethod m : meths) {
            HttpRequest.Builder b = new HttpRequest.Builder(baseUrl + "/test/upload/mixed")
                    .using(m)
                    .addQueryParams(SimpleMethodsServlet.EXPECTED_SIMPLE_QUERY)
                    .addHeaders(SimpleMethodsServlet.EXPECTED_SIMPLE_HEADERS)
                    .addBodyParams(SimpleMethodsServlet.EXPECTED_SIMPLE_BODY)
                    .addBodyParams(UploadFileServlet.EXPECTED_FILES);
            for (Map.Entry<String, Object> e : UploadInputStreamServlet.EXPECTED_INPUTSTREAM.entrySet()) {
                b.addBodyParam(e.getKey(), RestServiceContractTest.class.getResourceAsStream(e.getValue().toString()));
            }
            HttpResponse res = getRestService().exec(b.build());
            assertEquals(200, res.getStatusCode());
            assertEquals("OK", res.asString());
        }
    }
}

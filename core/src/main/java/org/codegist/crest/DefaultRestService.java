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

import org.codegist.common.io.IOs;
import org.codegist.common.lang.Randoms;
import org.codegist.common.log.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Simple RestService implementation based on JDK's {@link java.net.HttpURLConnection}.
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 * @see java.net.HttpURLConnection
 */
public class DefaultRestService implements RestService {

    private final static String MULTIPART = "multipart/form-data; boundary=";
    private final static String USER_AGENT = "CodeGist-CRest Agent";
    private static final Logger logger = Logger.getLogger(DefaultRestService.class);

    public HttpResponse exec(HttpRequest request) throws HttpException {
        HttpURLConnection connection = null;
        boolean inError = false;
        try {
            connection = toHttpURLConnection(request);
            logger.debug("%4s %s", request.getMeth(), connection.getURL());
            logger.trace(request);
            if (connection.getResponseCode() != 200) {
                throw new HttpException(connection.getResponseMessage(), new HttpResponse(request, connection.getResponseCode(), connection.getHeaderFields()));
            }
            HttpResponse response = new HttpResponse(request, connection.getResponseCode(), connection.getHeaderFields(), new HttpResourceImpl(connection));
            logger.trace("HTTP Response %s", response);
            return response;
        } catch (HttpException e) {
            inError = true;
            throw e;
        } catch (Throwable e) {
            inError = true;
            throw new HttpException(e, new HttpResponse(request, -1));
        } finally {
            if (inError) {
                if (connection != null) connection.disconnect();
            }
        }
    }

    static HttpURLConnection toHttpURLConnection(HttpRequest request) throws IOException {
        URL url = request.getUrl(true);
        HttpURLConnection con = newConnection(url);

        con.setRequestMethod(request.getMeth());
        if (request.getConnectionTimeout() != null && request.getConnectionTimeout() >= 0)
            con.setConnectTimeout(request.getConnectionTimeout().intValue());
        if (request.getSocketTimeout() != null && request.getSocketTimeout() >= 0)
            con.setReadTimeout(request.getSocketTimeout().intValue());

        if (request.getHeaderParams() != null) {
            for (Map.Entry<String, String> header : request.getHeaderParams().entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        if ("PUT".equals(request.getMeth()) || "POST".equals(request.getMeth())) {
            if (Params.isForUpload(request.getFormParams())) {
                String boundary = Randoms.randomAlphaNumeric(16) + System.currentTimeMillis();
                con.setRequestProperty("Content-Type", MULTIPART + boundary);
                if (request.getFormParams() != null) {
                    boundary = "--" + boundary;
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    DataOutputStream out = new DataOutputStream(os);

                    for (Map.Entry<String, Object> param : request.getFormParams().entrySet()) {
                        InputStream upload = null;
                        String name = null;
                        if (param.getValue() instanceof InputStream) {
                            upload = (InputStream) param.getValue();
                            name = param.getKey();
                        } else if (param.getValue() instanceof File) {
                            upload = new FileInputStream((File) param.getValue());
                            name = ((File) param.getValue()).getName();
                        }

                        if (upload != null) {
                            out.writeBytes(boundary + "\r\n");
                            out.writeBytes("Content-Disposition: form-data; name=\"" + param.getKey() + "\"; filename=\"" + name + "\"\r\n");
                            out.writeBytes("Content-Type: Content-Type: application/octet-stream\r\n\r\n");
                            BufferedInputStream in = null;
                            try {
                                in = (BufferedInputStream) (upload instanceof BufferedInputStream ? upload : new BufferedInputStream(upload));
                                IOs.copy(in, out);
                                out.writeBytes("\r\n");
                            } finally {
                                IOs.close(in);
                            }
                        } else if (param.getValue() != null) {
                            out.writeBytes(boundary + "\r\n");
                            out.writeBytes("Content-Disposition: form-data; name=\"" + param.getKey() + "\"\r\n");
                            out.writeBytes("Content-Type: text/plain; charset=" + request.getEncoding() + "\r\n\r\n");
                            out.write(param.getValue().toString().getBytes(request.getEncoding()));
                            out.writeBytes("\r\n");
                        }
                    }
                    out.writeBytes(boundary + "--\r\n");
                    out.writeBytes("\r\n");
                }
            } else {
                byte[] data = new byte[0];
                if (request.getFormParams() != null) {
                    data = Params.encodeParams(request.getFormParams(), request.getEncoding()).getBytes(request.getEncoding());
                }
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + request.getEncoding());
                con.setRequestProperty("Content-Length", Integer.toString(data.length));
                if (data.length > 0) {
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    DataOutputStream out = new DataOutputStream(os);
                    out.write(data);
                    os.flush();
                    os.close();
                }
            }
        }

        return con;
    }

    protected static HttpURLConnection newConnection(URL url) throws IOException {
        HttpURLConnection con;
        con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);
        return con;
    }

    private class HttpResourceImpl implements HttpResource {

        private final HttpURLConnection connection;

        public HttpResourceImpl(HttpURLConnection connection) {
            this.connection = connection;
        }

        public InputStream getContent() throws HttpException{
            try {
                return connection.getInputStream();
            } catch (IOException e) {
                throw new HttpException(e);
            }
        }

        public void release() throws HttpException{
            connection.disconnect();
        }
    }
}

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
import org.codegist.common.io.IOs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpResponse {

    private final HttpRequest request;
    private final InputStream inputStream;
    private final Map<String, List<String>> headers;
    private final int statusCode;
    private String responseString = null;


    public HttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, null);

    }
    public HttpResponse(HttpRequest request, int statusCode, Map<String, List<String>> headers) {
        this(request, statusCode, headers, null, null);
    }

    public HttpResponse(HttpRequest request, int statusCode, Map<String, List<String>> headers, InputStream inputStream, String contentEncoding) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = Maps.unmodifiable(headers);
        if ("gzip".equals(contentEncoding)) {
            try {
                this.inputStream = new GZIPInputStream(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.inputStream = inputStream;
        }
    }

    /**
     * @return The original request
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * @return The response reader.
     * @throws IllegalStateException if {@link org.codegist.crest.HttpResponse#asString()} has already been called
     */
    public Reader asReader() throws IllegalStateException {
        if (inputStream == null) return null;
        if (responseString != null) {
            throw new IllegalStateException("Stream as already been consumed");
        }
        return new InputStreamReader(inputStream, request.getEncodingAsCharset());
    }

    /**
     * @return The response input stream.
     * @throws IllegalStateException if {@link org.codegist.crest.HttpResponse#asString()} has already been called
     */
    public InputStream asStream() {
        if (inputStream == null) return null;
        if (responseString != null) {
            throw new IllegalStateException("Stream as already been consumed");
        }
        return inputStream;
    }

    /**
     * Returns the response as string. Calling this method will consume the response stream and any call to {@link HttpResponse#asReader()}  or {@link org.codegist.crest.HttpResponse#asStream()} will throw an IllegalStateException.
     * <p>Can only be called if the reponse stream hasn't been consumed.
     *
     * @return the response as a string
     */
    public String asString() {
        if (inputStream == null) return null;
        if (responseString == null) {
            try {
                responseString = IOs.toString(inputStream, request.getEncoding(), true);
            } catch (IOException e) {
                throw new HttpException(e, this);
            }
        }
        return responseString;
    }

    public List<String> getHeader(String name) {
        List<String> header = headers.get(name);
        return header != null ? Collections.unmodifiableList(header) : Collections.<String>emptyList();
    }

    /**
     * @return Http status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Close the response.
     */
    public void close() {
        IOs.close(inputStream);
    }
}

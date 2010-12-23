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
import org.codegist.common.lang.Strings;
import org.codegist.common.lang.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Http response for the a HttpRequest.
 * <p>Response charset and mime type are retrieved on the Content-Type header.
 * <p>If no valid charset and mimetype are found, it defaults respectively with ISO-8859-1 and text/html
 * <p>If the response is GZipped, the Content-Encoding header must be set to gzip.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpResponse {
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
    private static final Pattern CONTENT_TYPE_PARSER = Pattern.compile("^(?:([^;=]+)?\\s*;?\\s*charset=(.*+))|(?:([^;=]+)\\s*;?\\s*)$");
    private final HttpRequest request;
    private final InputStream inputStream;
    private final Map<String, List<String>> headers;
    private final int statusCode;
    private final String contentEncoding;
    private final String mimeType;
    private final Charset charset;

    private String responseString = null;

    public HttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, null);
    }

    public HttpResponse(HttpRequest request, int statusCode, Map<String, List<String>> headers) {
        this(request, statusCode, headers, null);
    }

    /**
     *
     *
     * @param request     The original request
     * @param statusCode  the response status code
     * @param headers     response headers.
     * @param resource underlying http resource
     */
    public HttpResponse(HttpRequest request, int statusCode, Map<String, List<String>> headers, HttpResource resource) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = Maps.unmodifiable(headers);
        this.contentEncoding = getFirstHeaderFor(this.headers, "Content-Encoding");
        InputStream stream = resource != null ? new HttpResourceInputStream(resource) : null;
        if (resource != null && "gzip".equalsIgnoreCase(contentEncoding)) {
            try {
                this.inputStream = new GZIPInputStream(stream);
            } catch (IOException e) {
                throw new HttpException(e);
            }
        } else {
            this.inputStream = stream;
        }
        String[] contentTypeGroups = Strings.extractGroups(CONTENT_TYPE_PARSER, getFirstHeaderFor(this.headers, "Content-Type"));
        if (contentTypeGroups.length == 0) {
            this.mimeType = DEFAULT_MIME_TYPE;
            this.charset = DEFAULT_CHARSET;
        } else {
            this.mimeType = Strings.defaultIfBlank(contentTypeGroups[1], Strings.defaultIfBlank(contentTypeGroups[3], DEFAULT_MIME_TYPE));
            this.charset = Charset.forName(Strings.defaultIfBlank(contentTypeGroups[2], DEFAULT_CHARSET.name()));
        }
    }

    private static String getFirstHeaderFor(Map<String, List<String>> headers, String name) {
        List<String> contentType = headers.get(name);
        if (contentType == null || contentType.isEmpty()) return "";
        return Strings.defaultIfBlank(contentType.get(0), "");
    }

    public String getMimeType() {
        return mimeType;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return The original request
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Get the response reader using the response charset (extracted from response header.)
     *
     * @return The response reader.
     * @throws IllegalStateException if {@link org.codegist.crest.HttpResponse#asString()} has already been called
     */
    public Reader asReader() throws IllegalStateException {
        if (inputStream == null) return null;
        if (responseString != null) {
            throw new IllegalStateException("Stream as already been consumed");
        }
        return new InputStreamReader(inputStream, charset);
    }

    /**
     * Get the response input stream. Use {@link HttpResponse#getCharset} to decode it.
     *
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
                responseString = IOs.toString(inputStream, charset, true);
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

    public String toString() {
        return new ToStringBuilder(this)
                .append("statusCode", statusCode)
                .append("contentEncoding", contentEncoding)
                .append("mimeType", mimeType)
                .append("charset", charset)
                .append("headers", headers)
                .append("request", request)
                .toString();
    }
}


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
import org.codegist.common.lang.EqualsBuilder;
import org.codegist.common.lang.HashCodeBuilder;
import org.codegist.common.lang.ToStringBuilder;
import org.codegist.common.net.Urls;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpRequest {
    private final HttpMethod meth;
    private final URI uri;
    private final Long socketTimeout;
    private final Long connectionTimeout;
    private final String encoding;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Map<String, Object> bodyParams;

    private HttpRequest(HttpMethod meth, URI uri, Long socketTimeout, Long connectionTimeout, String encoding, Map<String, String> headers, Map<String, String> queryParams, Map<String, Object> bodyParams) {
        this.meth = meth;
        this.uri = uri;
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.encoding = encoding;
        this.headers = Collections.unmodifiableMap(headers);
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.bodyParams = Collections.unmodifiableMap(bodyParams);
    }

    public HttpMethod getMeth() {
        return meth;
    }

    public URI getUri() {
        return uri;
    }

    public String getUrlString(boolean includeQueryString) throws MalformedURLException, UnsupportedEncodingException {
        if (!includeQueryString || queryParams.isEmpty()) return uri.toString();
        return uri.toString() + "?" + Urls.buildQueryString(queryParams, encoding);
    }

    public URL getUrl(boolean includeQueryString) throws MalformedURLException, UnsupportedEncodingException {
        return new URL(getUrlString(includeQueryString));
    }

    public Long getSocketTimeout() {
        return socketTimeout;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getEncoding() {
        return encoding;
    }

    public Charset getEncodingAsCharset() {
        try {
            return Charset.forName(getEncoding());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, Object> getBodyParams() {
        return bodyParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest that = (HttpRequest) o;
        return new EqualsBuilder()
                .append(bodyParams, that.bodyParams)
                .append(connectionTimeout, that.connectionTimeout)
                .append(encoding, that.encoding)
                .append(headers, that.headers)
                .append(meth, that.meth)
                .append(queryParams, that.queryParams)
                .append(socketTimeout, that.socketTimeout)
                .append(uri, that.uri)
                .equals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(uri)
                .append(socketTimeout)
                .append(connectionTimeout)
                .append(encoding)
                .append(headers)
                .append(queryParams)
                .append(bodyParams)
                .hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("meth", meth)
                .append("uri", uri)
                .append("socketTimeout", socketTimeout)
                .append("connectionTimeout", connectionTimeout)
                .append("encoding", encoding)
                .append("headers", headers)
                .append("queryParams", queryParams)
                .append("bodyParams", bodyParams)
                .toString();
    }


    /**
     * Handy builder for HttpRequest objects.
     * <p>The default call :
     * <code><pre>
     * HttpRequest request = new HttpRequest.Builder("http://127.0.0.1").build();
     * </pre></code>
     * <p>Will create an GET utf-8 HttpRequest object.
     */
    public static class Builder {
        static final String ENCODING = "utf-8";
        static final HttpMethod METH = HttpMethod.GET;
        private HttpMethod meth = METH;
        private String baseUri;
        private Map<String, String> headers;
        private Long socketTimeout = null;
        private Long connectionTimeout = null;
        private String encoding = ENCODING;
        private Map<String, String> queryString;
        private Map<String, Object> bodyParams;
        private Map<String, List<String>> queryStringParamsReverse;

        /**
         * Creates a GET request pointing to the given url
         *
         * @param uriString Base url to use
         * @throws URISyntaxException Invalid url
         * @see Builder#pointsTo(String)
         */
        public Builder(String uriString) throws URISyntaxException {
            pointsTo(uriString);
        }

        /**
         * Creates a GET request pointing to the given url
         *
         * @param uriString Base url to use
         * @param encoding  Url encoding
         * @throws URISyntaxException Invalid url
         * @see Builder#pointsTo(String,String)
         */
        public Builder(String uriString, String encoding) throws URISyntaxException {
            pointsTo(uriString, encoding);
        }

        public HttpRequest build() throws URISyntaxException {
            return new HttpRequest(
                    meth,
                    new URI(baseUri),
                    socketTimeout,
                    connectionTimeout,
                    encoding,
                    headers != null ? headers : new LinkedHashMap<String, String>(),
                    queryString != null ? queryString : new LinkedHashMap<String, String>(),
                    bodyParams != null ? bodyParams : new LinkedHashMap<String, Object>()
            );
        }
                        
    
        private static final Pattern FIX_URL_PATTERN = Pattern.compile("\\{(\\d+)\\}");

        /**
         * Sets the url the request will point to using the default encoding (utf-8)
         * <p>Can contains a predefined query string
         *
         * @param uriString Url the request will point to
         * @return current builder
         * @throws URISyntaxException If the uriString is not a valid URL
         * @see Builder#pointsTo(String,String)
         */
        public Builder pointsTo(String uriString) throws URISyntaxException {
            return pointsTo(uriString, encoding);
        }

        /**
         * Sets the url the request will point to.
         * <p>Can contains a predefined query string
         * <p>This value can contain placeholders that points to method arguments. eg http://localhost:8080/my-path/{2}/{0}/{2}.json?my-param={1}.
         * <p>Any placeholder can then be replaced by a value using {@link Builder#replacePlaceholderInUri(int, String)}
         *
         * @param uriString Url the request will point to
         * @param encoding  Request encoding
         * @return current builder
         * @throws URISyntaxException If the uriString is not a valid URL
         * @see Builder#replacePlaceholderInUri(int, String)
         */
        public Builder pointsTo(String uriString, String encoding) throws URISyntaxException {
            uriString = FIX_URL_PATTERN.matcher(uriString).replaceAll("\\($1\\)");
            URI uri = new URI(uriString);
            String baseUri = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
            this.encoding = encoding;
            this.queryString = uri.getRawQuery() != null ? Urls.parseQueryString(uri.getRawQuery(), encoding) : new LinkedHashMap<String, String>();
            this.queryStringParamsReverse = Maps.reverse(queryString);
            this.baseUri = baseUri;
            return this;
        }

        /**
         * Return the current url include or not the query string
         *
         * @param includeQueryString Flag to indicate to include or not the query string
         * @return the url as a string
         * @throws UnsupportedEncodingException not supported parameter encoding
         */
        public String getUrlString(boolean includeQueryString) throws UnsupportedEncodingException {
            if (!includeQueryString) return baseUri;
            return baseUri + "?" + Urls.buildQueryString(queryString, encoding);
        }

        /**
         * Return the current url include or not the query string
         *
         * @param includeQueryString Flag to indicate to include or not the query string
         * @return the url
         * @throws MalformedURLException        invalid url
         * @throws UnsupportedEncodingException not supported parameter encoding
         */
        public URL getUrl(boolean includeQueryString) throws MalformedURLException, UnsupportedEncodingException {
            return new URL(getUrlString(includeQueryString));
        }

        /**
         * Returns the query string parameter name list by placeholder index.
         *
         * @param index the index of the place holder to retrieve the parameter names from
         * @return parameter name list of the given placeholder index
         * @see Builder#pointsTo(String, String)
         * @see Builder#replacePlaceholderInUri(int, String)
         */
        public List<String> getQueryParamNameByPlaceholderIndex(int index) {
            return queryStringParamsReverse.get("(" + index + ")");
        }

        /**
         * Replace all url placeholder at the given index with the given value.
         * <p>NB: any placeholder that belong to the query string will be URL-encoded. Other placeholder will be set as given.
         * <p>Given an url as http://localhost:8080/my-path/{2}/{0}/{2}.json?my-param={1}.
         * <p/>
         * <p>Calling this method with index=1,value="hello world" will gives :
         * <p>Given an url as http://localhost:8080/my-path/{2}/{0}/{2}.json?my-param=hello%20world.
         * <p/>
         * <p>Calling this method with index=0,value="hello world" will gives :
         * <p>Given an url as http://localhost:8080/my-path/{2}/hello world/{2}.json?my-param={1}.
         * <p/>
         * <p>Calling this method with index=2,value="hi/there" will gives :
         * <p>Given an url as http://localhost:8080/my-path/hi/there/{0}/hi/there.json?my-param={1}.
         * <p/>
         * <p>NB : You can put placeholder inside a predefined query string parameter as well, eg :
         * <p>http://localhost:8080/my-path.json?my-preformatted-param={1}%20{0}%20with%20formatting%20{3}.
         * <p>When doing so, the preformatted parameter should already by pre-encoded, and the merged values won't be encoded.
         *
         * @param index The placeholder index
         * @param value The value to merge
         * @return current builder
         */
        public Builder replacePlaceholderInUri(int index, String value) {
            List<String> paramNames = getQueryParamNameByPlaceholderIndex(index);
            if (paramNames != null && !paramNames.isEmpty()) {
                for (String param : paramNames) {
                    addQueryParam(param, value);// Can safely add it
                }
            }
            // params can either belongs to uri path (eg : http://localhost:8080/my/dynamic/path/{1}/{0}/...)
            // don't add it to the requestBuilder yet, baseUri string map can still contain placeholders
            Pattern p = Pattern.compile("\\(" + index + "\\)");
            baseUri = p.matcher(baseUri).replaceAll(value);
            Map<String, String> copy = new HashMap<String, String>();
            for (Map.Entry<String, String> param : queryString.entrySet()) {
                String key = p.matcher(param.getKey()).replaceAll(value);
                String val = p.matcher(param.getValue()).replaceAll(value);
                copy.put(key, val);
            }
            queryString = copy;
            return this;
        }


        /**
         * @param timeout connection and socket timeout used for the resulting request.
         * @return current builder
         */
        public Builder timeoutAfter(Long timeout) {
            return timeoutConnectionAfter(timeout).timeoutSocketAfter(timeout);
        }

        /**
         * @param timeout socket timeout used for the resulting request.
         * @return current builder
         */
        public Builder timeoutSocketAfter(Long timeout) {
            this.socketTimeout = timeout;
            return this;
        }

        /**
         * @param timeout connection timeout used for the resulting request.
         * @return current builder
         */
        public Builder timeoutConnectionAfter(Long timeout) {
            this.connectionTimeout = timeout;
            return this;
        }

        /**
         * @param meth Http method to use to the resulting request.
         * @return current builder
         */
        public Builder using(HttpMethod meth) {
            this.meth = meth;
            return this;
        }

        /**
         * Adds a request header to the resulting request's headers
         *
         * @param key   Header name
         * @param value Header value
         * @return current builder
         */
        public Builder addHeader(String key, Object value) {
            if (headers == null) headers = new LinkedHashMap<String, String>();
            headers.put(key, value != null ? value.toString() : null);
            return this;
        }

        /**
         * Sets the resulting request's headers to the given map
         *
         * @param headers request headers map
         * @return current builder
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * Adds the the given map to the resulting request's headers
         *
         * @param headers request headers map
         * @return current builder
         */
        public Builder addHeaders(Map<String, String> headers) {
            if (this.headers == null)
                this.headers = new LinkedHashMap<String, String>(headers);
            else
                this.headers.putAll(headers);
            return this;
        }

        /**
         * Adds a parameter to the resulting request's query string
         *
         * @param key   query string parameter name
         * @param value query string parameter value
         * @return current builder
         */
        public Builder addQueryParam(String key, String value) {
            if (queryString == null) queryString = new LinkedHashMap<String, String>();
            queryString.put(key, value);
            return this;
        }

        /**
         * Sets the resulting request's query string parameters to the given map
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder setQueryString(Map<String, String> params) {
            this.queryString = params;
            return this;
        }

        /**
         * Adds the given map to the resulting request's query string parameter
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder addQueryParams(Map<String, String> params) {
            if (this.queryString == null)
                this.queryString = new LinkedHashMap<String, String>(params);
            else
                this.queryString.putAll(params);
            return this;
        }

        /**
         * Adds a body parameter to the resulting request's body parameters
         *
         * @param key   query string parameter name
         * @param value query string parameter value
         * @return current builder
         */
        public Builder addBodyParam(String key, Object value) {
            if (bodyParams == null) bodyParams = new LinkedHashMap<String, Object>();
            bodyParams.put(key, value);
            return this;
        }

        /**
         * Sets the resulting request's body to the given map
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder setBodyParams(Map<String, Object> params) {
            this.bodyParams = params;
            return this;
        }

        /**
         * Adds the given map to the resulting request's body parameter
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder addBodyParams(Map<String, Object> params) {
            if (this.bodyParams == null)
                this.bodyParams = new LinkedHashMap<String, Object>(params);
            else
                this.bodyParams.putAll(params);
            return this;
        }

        public Map<String, List<String>> getQueryStringParamsReverse() {
            return queryStringParamsReverse;
        }

        public Map<String, Object> getBodyParams() {
            return bodyParams;
        }

        public Map<String, String> getQueryString() {
            return queryString;
        }

        public HttpMethod getMeth() {
            return meth;
        }

        public String getBaseUri() {
            return baseUri;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Long getSocketTimeout() {
            return socketTimeout;
        }

        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public String getEncoding() {
            return encoding;
        }
    }
}

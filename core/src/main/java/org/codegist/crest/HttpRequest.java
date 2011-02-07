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

import org.codegist.common.lang.EqualsBuilder;
import org.codegist.common.lang.HashCodeBuilder;
import org.codegist.common.lang.Strings;
import org.codegist.common.lang.ToStringBuilder;
import org.codegist.common.net.Urls;
import org.codegist.crest.config.Destination;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpRequest {
    private final String meth;
    private final URI uri;
    private final Long socketTimeout;
    private final Long connectionTimeout;
    private final String encoding;
    private final Map<String, String> headerParams;
    private final Map<String, String> queryParams;
    private final Map<String, Object> formParams;

    private HttpRequest(String meth, URI uri, Long socketTimeout, Long connectionTimeout, String encoding, Map<String, String> headerParams, Map<String, String> queryParams, Map<String, Object> formParams) {
        this.meth = meth;
        this.uri = uri;
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.encoding = encoding;
        this.headerParams = Collections.unmodifiableMap(headerParams);
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.formParams = Collections.unmodifiableMap(formParams);
    }

    public String getMeth() {
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

    public Map<String, String> getHeaderParams() {
        return headerParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, Object> getFormParams() {
        return formParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest that = (HttpRequest) o;
        return new EqualsBuilder()
                .append(formParams, that.formParams)
                .append(connectionTimeout, that.connectionTimeout)
                .append(encoding, that.encoding)
                .append(headerParams, that.headerParams)
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
                .append(headerParams)
                .append(queryParams)
                .append(formParams)
                .hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("meth", meth)
                .append("uri", uri)
                .append("socketTimeout", socketTimeout)
                .append("connectionTimeout", connectionTimeout)
                .append("encoding", encoding)
                .append("headerParams", headerParams)
                .append("queryParams", queryParams)
                .append("formParams", formParams)
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
        private static final Pattern PLACEHOLDER_CONTENT_PATTERN = Pattern.compile("[^\\s\\}]+");
        private static final Pattern SINGLE_PLACEHOLDER_PATTERN = Pattern.compile("[\\{\\(](" + PLACEHOLDER_CONTENT_PATTERN.pattern() + ")[\\}\\)]");
        private static final Pattern CONTAINS_PLACEHOLDER_PATTERN = Pattern.compile(".*" + SINGLE_PLACEHOLDER_PATTERN + ".*");
        static final String ENCODING = "utf-8";
        static final String METH = "GET";
        private String meth = METH;
        private String baseUri;
        private Long socketTimeout = null;
        private Long connectionTimeout = null;
        private String encoding = ENCODING;
        private final LinkedHashMap<String, String> headerParams = new LinkedHashMap<String, String>();
        private final LinkedHashMap<String, String> queryParams = new LinkedHashMap<String, String>();
        private final LinkedHashMap<String, String> pathParams = new LinkedHashMap<String, String>();
        private final LinkedHashMap<String, Object> formParams = new LinkedHashMap<String, Object>();

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
                    new URI(buildBaseUriString()),
                    socketTimeout,
                    connectionTimeout,
                    encoding,
                    headerParams,
                    queryParams,
                    formParams
            );
        }

        private static boolean hasUnresolvedPlaceholders(String value){
            return CONTAINS_PLACEHOLDER_PATTERN.matcher(value).matches();
        }

        private String buildBaseUriString(){
            String baseUri = this.baseUri;
            for(Map.Entry<String, String> param : pathParams.entrySet()){
                String name = param.getKey();
                String value = param.getValue();
                baseUri = baseUri.replaceAll("\\(" + Pattern.quote(name) + "\\)", value);
            }
            if(hasUnresolvedPlaceholders(baseUri)) {
                throw new IllegalStateException("Not all path parameters have been provided for base uri '" + this.baseUri + "'! (current built baseUri='" + baseUri + "')");
            }
            return baseUri;
        }

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
         * <p>This value can contain placeholders that points to method arguments. eg http://localhost:8080/my-path/{my-param-name}/{p2}.json
         *
         * @param uriString Url the request will point to - No query string allowed
         * @param encoding  Request encoding
         * @return current builder
         * @throws URISyntaxException If the uriString is not a valid URL
         */
        public Builder pointsTo(String uriString, String encoding) throws URISyntaxException {
            String fixed = SINGLE_PLACEHOLDER_PATTERN.matcher(uriString).replaceAll("\\($1\\)");
            URI uri = new URI(fixed);
            String baseUri = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
            this.encoding = encoding;
            this.baseUri = baseUri;
            if(Strings.isNotBlank(uri.getRawQuery())) {
                throw new IllegalArgumentException("Given uri contains a query string:" + uriString);
            }
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
            String uri = buildBaseUriString();
            if (!includeQueryString || queryParams.isEmpty()) return uri;
            return uri + "?" + Urls.buildQueryString(queryParams, encoding);
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
        public Builder using(String meth) {
            this.meth = meth;
            return this;
        }

        /**
         * Adds a request header to the resulting request's headerParams
         *
         * @param name   Header name
         * @param value Header value
         * @return current builder
         */
        public Builder addHeaderParam(String name, Object value) {
            headerParams.put(name, value != null ? value.toString() : null);
            return this;
        }

        /**
         * Sets the resulting request's headerParams to the given map
         *
         * @param headers request headerParams map
         * @return current builder
         */
        public Builder setHeaderParams(Map<String, String> headers) {
            this.headerParams.clear();
            return addHeaderParams(headers);
        }

        /**
         * Adds the the given map to the resulting request's headerParams
         *
         * @param headers request headerParams map
         * @return current builder
         */
        public Builder addHeaderParams(Map<String, String> headers) {
            this.headerParams.putAll(headers);
            return this;
        }

        /**
         * Adds a parameter to the resulting request's path string
         *
         * @param name   path parameter name
         * @param value path parameter value
         * @return current builder
         */
        public Builder addPathParam(String name, String value) {
            this.pathParams.put(name,value);
            return this;
        }

        /**
         * Sets the resulting request's path parameters to the given map
         *
         * @param params path parameters map
         * @return current builder
         */
        public Builder setPathParams(Map<String, String> params) {
            this.pathParams.clear();
            return addPathParams(params);
        }

        /**
         * Adds the given map to the resulting request's path parameters
         *
         * @param params path parameters map
         * @return current builder
         */
        public Builder addPathParams(Map<String, String> params) {
            this.pathParams.putAll(params);
            return this;
        }

        /**
         * Adds a parameter to the resulting request's query string.
         * If this parameter name correspond to a placeholder name (cotnained in a query string parameter value), then it will replace the placeholder value without adding a new query string parameter
         * Placeholder in query string can be set as follow:
         * <p>eg: ?p={0}-formatted-param-{name}
         * <p>Can be set with:
         * <code><pre>
         * builder.setQueryParamPlaceholder("0", "myValue")
         *        .setQueryParamPlaceholder("name", "mySecondValue")
         * </pre></code>
         * @param name query string parameter name or placeholder name
         * @param value query string parameter value or placeholder name
         * @return current builder
         */
        public Builder addQueryParam(String name, String value) {
            queryParams.put(name, value);
            return this;
        }

        /**
         * Sets the resulting request's query string parameters to the given map
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder setQueryParams(Map<String, String> params) {
            this.queryParams.clear();
            return addQueryParams(params);
        }

        /**
         * Adds the given map to the resulting request's query string parameter
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder addQueryParams(Map<String, String> params) {
            this.queryParams.putAll(params);
            return this;
        }

        /**
         * Adds a body parameter to the resulting request's body parameters
         *
         * @param name   query string parameter name
         * @param value query string parameter value
         * @return current builder
         */
        public Builder addFormParam(String name, Object value) {
            formParams.put(name, value);
            return this;
        }

        /**
         * Sets the resulting request's body to the given map
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder setFormParams(Map<String, Object> params) {
            this.formParams.clear();
            return addFormParams(params);
        }

        /**
         * Adds the given map to the resulting request's body parameter
         *
         * @param params query string parameters map
         * @return current builder
         */
        public Builder addFormParams(Map<String, Object> params) {
            this.formParams.putAll(params);
            return this;
        }

        /**
         * Adds a parameter to the given destination in the final http request
         * @param name name of the parameter
         * @param value value of the parameter
         * @param dest parameter destination
         * @return current builder
         */
        public Builder addParam(String name, Object value, Destination dest) {
            switch(dest){
                case FORM: return addFormParam(name, value);
                case PATH: return addPathParam(name, value.toString());
                case QUERY: return addQueryParam(name, value.toString());
                case HEADER: return addHeaderParam(name, value.toString());
                default: throw new IllegalStateException("shouldn't be here!");
            }
        }

        public String getMeth() {
            return meth;
        }

        public String getBaseUri() {
            return buildBaseUriString();
        }

        public Map<String, String> getHeaderParams() {
            return headerParams;
        }

        public Map<String, Object> getFormParams() {
            return formParams;
        }

        public Map<String, String> getQueryParams() {
            return queryParams;
        }

        public Map<String, String> getPathParams() {
            return pathParams;
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

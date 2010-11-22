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

package org.codegist.crest.interceptor;

import org.codegist.common.collect.Maps;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;

import java.util.Map;

/**
 * Interceptor that add some arbitrary query string, body and/or headers parameters to the requests.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class RequestParamDefaultsInterceptor extends RequestInterceptorAdapter {

    private final Map<String, String> queryString;
    private final Map<String, String> headers;
    private final Map<String, Object> body;

    public RequestParamDefaultsInterceptor(Map<String, String> queryString, Map<String, String> headers, Map<String, Object> body) {
        this.queryString = Maps.unmodifiable(queryString);
        this.headers = Maps.unmodifiable(headers);
        this.body = Maps.unmodifiable(body);
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        builder
                .addHeaders(headers)
                .addQueryParams(queryString)
                .addBodyParams(body);
        return true;
    }
}

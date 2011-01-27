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

import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;

/**
 * Simple composite request interceptor that delegate notifications to a predefined list of interceptors.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class CompositeRequestInterceptor implements RequestInterceptor {
    private final RequestInterceptor[] interceptors;

    public CompositeRequestInterceptor(RequestInterceptor... interceptors) {
        this.interceptors = interceptors;
    }

    public void beforeParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) throws Exception {
        for (RequestInterceptor interceptor : interceptors) {
            if (interceptor == null) continue;
            interceptor.beforeParamsInjectionHandle(builder, context);
        }
    }

    public void afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) throws Exception {
        for (RequestInterceptor interceptor : interceptors) {
            interceptor.afterParamsInjectionHandle(builder, context);
        }
    }

    public RequestInterceptor[] getInterceptors() {
        return interceptors.clone();
    }
}

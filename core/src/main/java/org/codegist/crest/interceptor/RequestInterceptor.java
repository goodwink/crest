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
 * A request interceptor is notified before and after the parameters have been added to the request.
 * <p>It can be used to cancel a request from being fired by returning false, or arbitrary modify the request.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface RequestInterceptor {

    /**
     * Called after general parameter have been added to the request, but before parameters are injected into it.
     *
     * @param builder The current http request being build
     * @param context The current request context
     * @return a flag indicating whether to continue or cancel the current request.
     */
    boolean beforeParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context);


    /**
     * Called after parameters have been injected into the request.
     *
     * @param builder The current http request being build
     * @param context The current request context
     * @return a flag indicating whether to continue or cancel the current request.
     */
    boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context);

}

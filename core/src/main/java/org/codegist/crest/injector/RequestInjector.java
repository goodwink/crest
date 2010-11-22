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

package org.codegist.crest.injector;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.ParamContext;

/**
 * A request injector is used to inject any method parameter values in the http request before it gets fired. Can modify the http request as wanted.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface RequestInjector {

    /**
     * Injects the current param into the request.
     *
     * @param builder Current http request being build.
     * @param context The current param context holding the value of the current method argument and all other context objects.
     */
    void inject(HttpRequest.Builder builder, ParamContext context);

}

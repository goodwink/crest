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

/**
 * Error handler gets invoked when an exception occurs during each request lifecyle :
 * <p>- Generation : involves RequestInterceptors, RequestInjectors, Serializer.
 * <p>- Firing : Any non 200 response status code while fire an exception.
 * <p>- Response handling : involves ResponseHandlers.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface ErrorHandler {

    /**
     * @param context Current response context. Inner HttpResponse if not guaranteed to be available as an error could have occured during request generation lifecycle step.
     * @param e       Exception occured
     * @param <T>     Expected return type
     * @return any value of the expected error type when exception is ignored
     * @throws Exception Any thrown exception while be delegated to the client using the relative rest interface.
     * @see ErrorHandler
     */
    <T> T handle(ResponseContext context, Exception e) throws Exception;

}

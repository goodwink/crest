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

import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.interceptor.RequestInterceptorAdapter;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class Stubs {

    

    public static class RestService1 implements RestService {
        public static final RestService1 INSTANCE = new RestService1();
        public HttpResponse exec(HttpRequest request) throws HttpException {
            return null;
        }
    }

    public static class RequestInterceptor1 extends RequestInterceptorAdapter {
    }

    public static class RequestInterceptor2 extends RequestInterceptorAdapter {
    }

    public static class RequestInterceptor3 extends RequestInterceptorAdapter {
    }

    public static class RequestParameterInjector1 implements Injector {
       
        public void inject(HttpRequest.Builder builder, ParamContext context) {

        }
    }

    public static class RequestParameterInjector2 implements Injector {
       
        public void inject(HttpRequest.Builder builder, ParamContext context) {

        }
    }

    public static class RequestParameterInjector3 implements Injector {
       
        public void inject(HttpRequest.Builder builder, ParamContext context) {

        }
    }

    public static class Serializer1 implements org.codegist.crest.serializer.Serializer {
        public static final Serializer1 INSTANCE = new Serializer1();
       
        public String serialize(Object value) {
            return null;
        }
    }

    public static class Serializer2 implements org.codegist.crest.serializer.Serializer {
        public static final Serializer2 INSTANCE = new Serializer2();
       
        public String serialize(Object value) {
            return null;
        }
    }

    public static class Serializer3 implements org.codegist.crest.serializer.Serializer {
        public static final Serializer3 INSTANCE = new Serializer3();
       
        public String serialize(Object value) {
            return null;
        }
    }


    public static class RetryHandler1 implements RetryHandler {
       
        public boolean retry(ResponseContext response, Exception exception, int retryNumber) {
            return false;
        }
    }

    public static class RetryHandler2 implements RetryHandler {
       
        public boolean retry(ResponseContext response, Exception exception, int retryNumber) {
            return false;
        }
    }


    public static class ResponseHandler1 implements ResponseHandler {
       
        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    public static class ResponseHandler2 implements ResponseHandler {
       
        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    public static class ResponseHandler3 implements ResponseHandler {
       
        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    public static class ErrorHandler1 implements ErrorHandler {
       
        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }

    public static class ErrorHandler2 implements ErrorHandler {
       
        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }

    public static class ErrorHandler3 implements ErrorHandler {
       
        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }
}

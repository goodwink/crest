package org.codegist.crest;

import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.RequestInterceptorAdapter;

public class Stubs {

    public static class RequestInterceptor1 extends RequestInterceptorAdapter {
    }

    public static class RequestInterceptor2 extends RequestInterceptorAdapter {
    }

    public static class RequestInterceptor3 extends RequestInterceptorAdapter {
    }

    public static class RequestParameterInjector1 implements RequestInjector {
        @Override
        public void inject(HttpRequest.Builder builder, ParamContext context) {

        }
    }

    public static class RequestParameterInjector2 implements RequestInjector {
        @Override
        public void inject(HttpRequest.Builder builder, ParamContext context) {

        }
    }

    public static class RequestParameterInjector3 implements RequestInjector {
        @Override
        public void inject(HttpRequest.Builder builder, ParamContext context) {

        }
    }

    public static class Serializer1 implements org.codegist.crest.serializer.Serializer {
        @Override
        public String serialize(ParamContext context) {
            return null;
        }
    }

    public static class Serializer2 implements org.codegist.crest.serializer.Serializer {
        @Override
        public String serialize(ParamContext context) {
            return null;
        }
    }

    public static class Serializer3 implements org.codegist.crest.serializer.Serializer {
        @Override
        public String serialize(ParamContext context) {
            return null;
        }
    }

    public static class ResponseHandler1 implements ResponseHandler {
        @Override
        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    public static class ResponseHandler2 implements ResponseHandler {
        @Override
        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    public static class ResponseHandler3 implements ResponseHandler {
        @Override
        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    public static class ErrorHandler1 implements ErrorHandler {
        @Override
        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }

    public static class ErrorHandler2 implements ErrorHandler {
        @Override
        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }

    public static class ErrorHandler3 implements ErrorHandler {
        @Override
        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }
}

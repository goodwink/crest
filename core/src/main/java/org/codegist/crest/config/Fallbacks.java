package org.codegist.crest.config;

import org.codegist.crest.*;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.RequestInterceptor;

/**
 * Annotation can't contains null value as a default. This class hold poison values used as NULL by the {@link org.codegist.crest.config.AnnotationDrivenInterfaceConfigFactory}
 */
public final class Fallbacks {
    private Fallbacks() {
    }

    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public static final String FALLBACK_STRING = "$__@FALL+BACK@__$";

    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public static final long FALLBACK_LONG = -1l;


    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public final static class FallbackRequestParameterInjector implements RequestInjector {
        private FallbackRequestParameterInjector() {
        }

        public void inject(HttpRequest.Builder builder, ParamContext context) {
        }
    }

    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public final static class FallbackSerializer implements org.codegist.crest.serializer.Serializer {
        private FallbackSerializer() {
        }

        public String serialize(ParamContext context) {
            return null;
        }
    }

    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public final static class FallbackResponseHandler implements ResponseHandler {
        private FallbackResponseHandler() {
        }

        public Object handle(ResponseContext responseContext) {
            return null;
        }
    }

    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public final static class FallbackErrorHandler implements ErrorHandler {
        private FallbackErrorHandler() {
        }

        public <T> T handle(ResponseContext context, Exception e) throws Exception {
            return null;
        }
    }

    /**
     * @see org.codegist.crest.config.Fallbacks
     */
    public final static class FallbackMethodInterceptor implements RequestInterceptor {
        private FallbackMethodInterceptor() {
        }

        public boolean beforeParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
            return false;
        }

        public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
            return false;
        }
    }
}

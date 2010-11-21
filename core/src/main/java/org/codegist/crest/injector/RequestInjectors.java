package org.codegist.crest.injector;

import org.codegist.crest.annotate.RestInjector;
import org.codegist.crest.config.Fallbacks;

public final class RequestInjectors {
    private RequestInjectors() {
    }

    /**
     * @param o Target
     * @return The request injector set for the given class. Returns null if not found or if the annotated request inject is {@link org.codegist.crest.config.Fallbacks.FallbackRequestParameterInjector}
     * @see RequestInjectors#getAnnotatedInjectorFor(Class)
     */
    public static Class<? extends RequestInjector> getAnnotatedInjectorFor(Object o) {
        return getAnnotatedInjectorFor(o.getClass());
    }

    /**
     * Get the request injector annotation value from the given type.
     *
     * @param type Type where looking up for the {@link RestInjector}
     * @return The request injector set for the given class. Returns null if not found or if the annotated request inject is {@link org.codegist.crest.config.Fallbacks.FallbackRequestParameterInjector}
     * @see org.codegist.crest.annotate.RestInjector
     * @see org.codegist.crest.config.Fallbacks.FallbackRequestParameterInjector
     */
    public static Class<? extends RequestInjector> getAnnotatedInjectorFor(Class<?> type) {
        if (type == null) return null;
        RestInjector injectorAnn = type.getAnnotation(RestInjector.class);
        if (injectorAnn != null && !Fallbacks.FallbackRequestParameterInjector.class.equals(injectorAnn.value())) {
            return injectorAnn.value();
        }
        return null;
    }
}

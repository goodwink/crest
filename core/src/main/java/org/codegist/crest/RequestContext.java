package org.codegist.crest;

import org.codegist.crest.config.MethodConfig;
import org.codegist.crest.config.ParamConfig;

import java.lang.reflect.Method;

/**
 * Context for any request, passed to request's interceptors.
 *
 * @see org.codegist.crest.interceptor.RequestInterceptor
 */
public interface RequestContext extends InterfaceContext {

    MethodConfig getMethodConfig();

    ParamConfig getParamConfig(int index);

    Object getArgValue(int index);

    String getArgSerialized(int index);

    int getArgCount();

    Method getMethod();

    /**
     * @return Method's call arguments.
     */
    Object[] getArgs();

}

package org.codegist.crest;

import org.codegist.crest.config.ParamConfig;

/**
 * Context object passed during the parameter injection process
 *
 * @param <V> Parameter type argument
 * @see org.codegist.crest.injector.RequestInjector
 */
public interface ParamContext<V> extends RequestContext {

    /**
     * Returns whether the given parameter is for url or body
     *
     * @return true if is for url
     */
    boolean isForUrl();

    ParamConfig getParamConfig();

    V getArgValue();

    String getArgSerialized();

    /**
     * @return Index of the current method call argument
     */
    int getIndex();

}

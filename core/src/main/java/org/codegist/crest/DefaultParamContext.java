package org.codegist.crest;

import org.codegist.crest.config.Destination;
import org.codegist.crest.config.ParamConfig;

/**
 * Default internal immutable implementation of ParamContext
 */
class DefaultParamContext<V> extends DefaultRequestContext implements ParamContext {

    private final int index;

    public DefaultParamContext(RequestContext methodContext, int index) {
        super(methodContext);
        this.index = index;
    }

    public boolean isForUrl() {
        return HttpMethod.GET.equals(getMethodConfig().getHttpMethod()) || Destination.URL.equals(getParamConfig().getDestination());
    }

    public ParamConfig getParamConfig() {
        return getParamConfig(index);
    }

    public V getArgValue() {
        return (V) getArgValue(index);
    }

    public String getArgSerialized() {
        return getArgSerialized(index);
    }

    /**
     * @return Index of the current method call argument
     */
    public int getIndex() {
        return index;
    }

}

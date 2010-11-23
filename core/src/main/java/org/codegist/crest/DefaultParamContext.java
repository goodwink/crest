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

import org.codegist.crest.config.Destination;
import org.codegist.crest.config.ParamConfig;

/**
 * Default internal immutable implementation of ParamContext
 * @author Laurent Gilles (laurent.gilles@codegist.org)
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

    public V getRawValue() {
        return (V) getRawValue(index);
    }

    public String getSerializedValue() {
        return getSerializedValue(index);
    }

    /**
     * @return Index of the current method call argument
     */
    public int getIndex() {
        return index;
    }

}

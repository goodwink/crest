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

import org.codegist.common.lang.Strings;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.ParamContext;
import org.codegist.crest.Params;

/**
 * Default request injector used by CRest.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DefaultInjector implements Injector {


    /**
     * <p> Serialize the given parameter using its preconfigured serializer and inject the result either :
     * <p> * as a new query string parameter if no name is provided and the parameter is meant to be used as a {@link org.codegist.crest.config.Destination#URL} parameter
     * <p> * merged in the request placeholder if a name is provided and the parameter is meant to be used as a {@link org.codegist.crest.config.Destination#URL} parameter
     * <p> * as a body parameter with or without name. No more than one body parameter can be added without name.
     * <p> If no serialized has been specified for the current param then see the default serializer documentation {@link org.codegist.crest.config.ParamConfig#DEFAULT_SERIALIZER}
     *
     * @param builder The current request beeing build
     * @param context The current method parameter being injected.
     * @see org.codegist.crest.config.ParamConfig#DEFAULT_SERIALIZER
     */
    @Override
    public void inject(HttpRequest.Builder builder, ParamContext context) {
        if (Params.isForUpload(context.getRawValue())) {
            // add it raw
            builder.addBodyParam(context.getParamConfig().getName(), context.getRawValue());
        } else {
            String paramValue = context.getSerializedValue();
            if (Strings.isBlank(paramValue)) return;

            if (context.isForUrl()) {
                if (Strings.isBlank(context.getParamConfig().getName())) {
                    builder.replacePlaceholderInUri(context.getIndex(), paramValue);
                } else {
                    builder.addQueryParam(context.getParamConfig().getName(), paramValue);
                }
            } else {
                // Can safely add it
                builder.addBodyParam(context.getParamConfig().getName(), paramValue);
            }
        }
    }
}

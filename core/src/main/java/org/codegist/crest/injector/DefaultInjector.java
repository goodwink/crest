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
     * <p> * The parameter is either a {@link java.io.InputStream} or {@link java.io.File}, then it is added to the request body
     * <p> * Otherwise the parameter is serialized with its configured serializer, and the serializer value added to the request. If serialized value is empty, then parameter default value is used.
     * <p> If no serialized has been specified for the current param then see the default serializer documentation {@link org.codegist.crest.config.MethodParamConfig#DEFAULT_SERIALIZER}
     *
     * @param builder The current request beeing build
     * @param context The current method parameter being injected.
     * @see org.codegist.crest.config.MethodParamConfig#DEFAULT_SERIALIZER
     */
    public void inject(HttpRequest.Builder builder, ParamContext context) {
        String name = context.getParamConfig().getName();
        if(Strings.isBlank(name)) {
            throw new IllegalStateException("Parameter name must be provided! (param method=" + context.getMethod() + ",index=" + context.getIndex() + ")");
        }
        if (Params.isForUpload(context.getRawValue())) {
            // add it raw
            builder.addFormParam(name, context.getRawValue());
        } else {
            String value = context.getSerializedValue();
            if(Strings.isBlank(value)) {
                value = context.getParamConfig().getDefaultValue();
            }
            builder.addParam(name, value, context.getParamConfig().getDestination());
        }
    }
}

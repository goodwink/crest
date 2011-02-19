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

package org.codegist.crest.handler;

import org.codegist.crest.ResponseContext;
import org.codegist.crest.serializer.Deserializer;

/**
 * Default response handler that either marshall the response or return server raw response following the rules below :
 * <p>- A method with a java.lang.String return type is considerer as expecting the raw server response only when no marshaller have been set in the custom properties. When conditions are met, the result will be the string representing the raw response.
 * <p>- Marshalling occurs only when a marshaller have been set in the custom properties (key="org.codegist.common.marshal.Marshaller")
 * <p>- Response is just ignored for voids methods.
 *
 * @see org.codegist.crest.InterfaceContext#getProperties()
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DefaultResponseHandler implements ResponseHandler {

    public final Object handle(ResponseContext context) {
        if (context.getExpectedType().toString().equals("void")) return null;

        Deserializer deserializer = context.getDeserializer();
        if (deserializer != null) {
            return deserializer.deserialize(context.getResponse().asReader(), context.getExpectedGenericType());
        }else{
            // if no marshaller has been set in the configuration, check that return type is String and return the response as string.
            if (String.class.equals(context.getExpectedType())) {
                return context.getResponse().asString();
            } else {
                throw new IllegalStateException("Method do no have a Deserializer and its return type is not a valid raw type.");
            }
        }
    }
}

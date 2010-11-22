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

package org.codegist.crest.serializer;

import org.codegist.crest.ParamContext;

/**
 * Simple serializer that returns the toString() value of the given value or empty string if null.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ToStringParamSerializer implements Serializer {
    /**
     * Simple serializer that returns the toString() value of the given value or empty string if null.
     *
     * @param context Context of the serialization
     * @return serialized value of param
     */
    @Override
    public String serialize(ParamContext context) {
        return context.getArgValue() != null ? context.getArgValue().toString() : "";
    }
}

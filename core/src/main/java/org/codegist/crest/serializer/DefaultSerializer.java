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

import java.util.Collection;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DefaultSerializer implements Serializer {

    private final ToStringParamSerializer toStringSerializer;
    private final ArrayParamSerializer arrayParamSerializer;

    public DefaultSerializer() {
        toStringSerializer = new ToStringParamSerializer();
        arrayParamSerializer = new ArrayParamSerializer(toStringSerializer);
    }

    public DefaultSerializer(String listSeparator) {
        toStringSerializer = new ToStringParamSerializer();
        arrayParamSerializer = new ArrayParamSerializer(toStringSerializer, listSeparator);
    }

    /**
     * Serialize the given object calling toString() on it.
     * <p>If the given object is a Collection or an Array, the serialization result will be a coma-separated list of toString() calls for all inner non colletion/array values, otherwise if inner values are collection/arrays themself, it recurses.
     * <p>eg :
     * <code><pre>
     * List<String[]> o = Arrays.asList(new String[][]{{"1","2"},{"3","4"},{"5","6"}});
     * String s = new DefaultSerializer().serialize(null, o);
     * // s = 1,2,3,4,5,6
     * </pre></code>
     *
     * @param context
     * @return Serialized argument value
     */
    @Override
    public String serialize(ParamContext context) {
        if (context.getArgValue() == null) return null;
        if (context.getArgValue().getClass().isArray() || context.getArgValue() instanceof Collection) {
            return arrayParamSerializer.serialize(context);
        } else {
            return toStringSerializer.serialize(context);
        }
    }

}

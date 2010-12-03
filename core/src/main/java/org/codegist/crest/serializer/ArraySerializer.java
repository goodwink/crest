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

import org.codegist.common.lang.Strings;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple array serializer that serialize arrays and collections into a String.
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ArraySerializer<T> implements Serializer<T> {

    /**
     * Default array item separator
     */
    public static final String DEFAULT_SEPARATOR = ",";

    private static final Serializer DEFAULT_ITEM_SERIALIZER = new ToStringSerializer();

    final String separator;
    final Serializer itemSerializer;

    public ArraySerializer() {
        this(DEFAULT_ITEM_SERIALIZER, DEFAULT_SEPARATOR);
    }

    public ArraySerializer(Serializer itemSerializer) {
        this(itemSerializer, DEFAULT_SEPARATOR);
    }

    public ArraySerializer(Serializer itemSerializer, String separator) {
        this.separator = separator;
        this.itemSerializer = itemSerializer;
    }

    @Override
    public String serialize(T value) {
        if (value == null) {
            return "";
        } else if (value.getClass().isArray())
            return serializeArray(value);
        else if (value instanceof Collection)
            return serialize((Collection) value);
        else
            throw new IllegalArgumentException("Param should be an array or a collection");
    }

    private String serializeArray(Object params) {
        List<Object> o = new ArrayList<Object>();
        int len = Array.getLength(params); // Can't cast, params can be an array of primitives.
        for (int i = 0; i < len; i++) o.add(Array.get(params, i));
        return serialize(o);
    }

    private String serialize(Collection params) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object p : params) {
            String s = itemSerializer.serialize(p);
            if (Strings.isNotBlank(s)) {
                sb.append(s);
            }
            if (++i < params.size()) sb.append(separator);
        }
        return sb.toString();
    }
}

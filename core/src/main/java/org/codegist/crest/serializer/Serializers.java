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

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.Strings;
import org.codegist.common.reflect.Types;
import org.codegist.crest.CRestProperty;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @see Serializers#getFor(java.util.Map, java.lang.reflect.Type) 
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public final class Serializers {

    private static final Serializer TOSTRING_SERIALIZER = new ToStringSerializer();

    private Serializers() {
        
    }

    /**
     * <p>Handy method to retrieve a serializer instance for the given Type using the given customProperties following the behavior described by {@link org.codegist.crest.CRest} for amethod argument serialization.
     * <p>The returned serializer is:
     * <p> - a serializer from the map if the type match
     * <p> - otherwise an instance of {@link org.codegist.crest.serializer.DateSerializer} if no serializer for the given type has been found in the map and the type is a {@link java.util.Date} that serialize to ISO-8601 date format by default.
     * <p> - otherwise an instance of {@link org.codegist.crest.serializer.ArraySerializer} if the type happens to be either a Array or a Collection. The collection/array items serializer selection follows the same rules a stated before
     * <p> - otherwise an instance of {@link org.codegist.crest.serializer.ToStringSerializer} if no serializer for the given type has been found in the map
     * <p>
     * <p>The custom properties can customize the default behavior, it may contain values mapped with the following keys:
     * <p>  - {@link org.codegist.crest.CRestProperty#SERIALIZER_CUSTOM_SERIALIZER_MAP}
     * <p>  - {@link org.codegist.crest.CRestProperty#SERIALIZER_LIST_SEPARATOR}
     * <p>  - {@link org.codegist.crest.CRestProperty#SERIALIZER_DATE_FORMAT}
     * @param customProperties    Map of default serializer per Type
     * @param type              Type to get the serializer for
     * @return the serializer
     */
    public static Serializer getFor(Map<String,Object> customProperties, Type type) {
        customProperties = Maps.defaultsIfNull(customProperties);
        Map<Type,Serializer> serializerMap = Maps.defaultsIfNull((Map<Type,Serializer>) customProperties.get(CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP));
        
        Class<?> typeCls = Types.getClass(type);
        if (typeCls == null) throw new IllegalStateException("Generic type information missing! (" + type + ")");
        boolean isCollection = false;
        if (typeCls.isArray()) {
            type = typeCls.getComponentType();
            isCollection = true;
        } else if (Collection.class.isAssignableFrom(typeCls)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
            isCollection = true;
        }

        Serializer s = serializerMap.get(type);
        s = s != null ? s : chooseDefault(customProperties, type);
        if (isCollection) {
            String separator = Strings.defaultIfBlank((String) customProperties.get(CRestProperty.SERIALIZER_LIST_SEPARATOR), ArraySerializer.DEFAULT_SEPARATOR);
            return new ArraySerializer(s, separator);
        } else {
            return s;
        }
    }

    private static Serializer chooseDefault(Map<String,Object> customProperties, Type type){
        Class<?> typeCls = Types.getClass(type);
        if(Date.class.isAssignableFrom(typeCls)) {
            return new DateSerializer(customProperties);
        }else if(Boolean.class.isAssignableFrom(typeCls) || boolean.class.isAssignableFrom(typeCls)) {
            return new BooleanSerializer(customProperties);
        }else{
            return TOSTRING_SERIALIZER;
        }
    }
}

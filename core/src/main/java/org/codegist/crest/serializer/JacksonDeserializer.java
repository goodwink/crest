/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.serializer;

import org.codegist.common.lang.Validate;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public class JacksonDeserializer implements Deserializer {

    public static final String USER_OBJECT_MAPPER_PROP = JacksonDeserializer.class.getName() + "#user-object-mapper";
    public static final String DESERIALIZATION_CONFIG_MAP_PROP = JacksonDeserializer.class.getName() + "#deserialization-config-map";

    private final ObjectMapper jackson;

    public JacksonDeserializer() {
        this(new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    public JacksonDeserializer(ObjectMapper jackson) {
        Validate.notNull(jackson, "ObjectMapper must not be null");
        this.jackson = jackson;
    }

    public JacksonDeserializer(Map<String, Object> config) {
        Validate.notNull(config, "Config must not be null");
        ObjectMapper mapper = null;
        if(config.containsKey(USER_OBJECT_MAPPER_PROP)) {
            mapper = (ObjectMapper) config.get(USER_OBJECT_MAPPER_PROP);
        }else{
            mapper = new ObjectMapper();
        }
        if(config.containsKey(DESERIALIZATION_CONFIG_MAP_PROP)) {
            Map<String, Boolean> deserializationConfig = (Map<String, Boolean>) config.get(DESERIALIZATION_CONFIG_MAP_PROP);
            for (Map.Entry<String, Boolean> entry : deserializationConfig.entrySet()) {
                mapper = mapper.configure(DeserializationConfig.Feature.valueOf(entry.getKey()), entry.getValue());
            }
        }else{
            mapper = mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        this.jackson = mapper;
    }

    public <T> T deserialize(Reader reader, Type type) {
        try {
            return jackson.<T>readValue(reader, TypeFactory.type(type));
        } catch (IOException e) {
            throw new DeserializerException(e);
        }
    }
}

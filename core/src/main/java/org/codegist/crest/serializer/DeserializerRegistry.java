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

import org.codegist.crest.CRestException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public class DeserializerRegistry {

    private final Map<String, Object> customProperties;
    private final Map<String, Class<? extends Deserializer>> mimeTypeRegistry;

    private DeserializerRegistry(Map<String, Object> customProperties, Map<String, Class<? extends Deserializer>> mimeTypeRegistry) {
        this.customProperties = customProperties;
        this.mimeTypeRegistry = mimeTypeRegistry;
    }

    public Deserializer getByMimeType(String mimeType){
        try {
            return mimeTypeRegistry.get(mimeType).getConstructor(Map.class).newInstance(customProperties);
        } catch (Exception e) {
            throw new CRestException(e);
        }
    }

    public static class Builder {
        private final Map<String, Class<? extends Deserializer>> mimeTypeRegistry = new HashMap<String, Class<? extends Deserializer>>();

        public DeserializerRegistry build(Map<String, Object> customProperties){
            return new DeserializerRegistry(customProperties, mimeTypeRegistry);
        }

        public Builder register(Class<? extends Deserializer> deserializer, String... mimeTypes){
            for(String mt : mimeTypes){
                mimeTypeRegistry.put(mt, deserializer);
            }
            return this;
        }

    }
}

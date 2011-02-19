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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public class DeserializerFactory {

    private final Map<String, Object> mimeTypeRegistry;
    private final Map<String, Deserializer> deserializersCache = new HashMap<String, Deserializer>();

    private DeserializerFactory(Map<String, Object> mimeTypeRegistry) {
        this.mimeTypeRegistry = mimeTypeRegistry;
    }

    public synchronized Deserializer buildForMimeType(String mimeType) {
        Deserializer deserializer = deserializersCache.get(mimeType);
        if (deserializer == null) {
            deserializersCache.put(mimeType, deserializer = build(mimeType));
        }
        return deserializer;
    }

    private Deserializer build(String mimeType) {
        Object deserializer = mimeTypeRegistry.get(mimeType);
        if (deserializer == null) throw new CRestException("No deserializer bound to mime type: " + mimeType);
        if (deserializer instanceof Deserializer) {
            return (Deserializer) deserializer;
        } else if (deserializer instanceof DeserializerDescriptor) {
            return ((DeserializerDescriptor) deserializer).instanciate();
        } else {
            throw new IllegalStateException("Shouldn't be here");
        }
    }

    public static class Builder {
        private final Map<String, Object> mimeTypeRegistry = new HashMap<String, Object>();

        public DeserializerFactory build() {
            return new DeserializerFactory(mimeTypeRegistry);
        }

        public Builder register(Class<? extends Deserializer> deserializer, Map<String, Object> deserializerConfig, String... mimeTypes) {
            for (String mt : mimeTypes) {
                mimeTypeRegistry.put(mt, new DeserializerDescriptor(deserializer, deserializerConfig));
            }
            return this;
        }

        public Builder register(Deserializer deserializer, String... mimeTypes) {
            for (String mt : mimeTypes) {
                mimeTypeRegistry.put(mt, deserializer);
            }
            return this;
        }

    }

    private static class DeserializerDescriptor {
        final Class<? extends Deserializer> deserializerClass;
        final Map<String, Object> config;

        private DeserializerDescriptor(Class<? extends Deserializer> deserializerClass, Map<String, Object> config) {
            this.deserializerClass = deserializerClass;
            this.config = config;
        }

        public Deserializer instanciate() {
            try {
                return deserializerClass.getConstructor(Map.class).newInstance(config);
            } catch (InvocationTargetException e) {
                throw new DeserializerException(e.getMessage(), e.getCause());
            } catch (NoSuchMethodException e) {
                try {
                    return deserializerClass.getConstructor().newInstance();
                } catch (InvocationTargetException e1) {
                    throw new DeserializerException(e1.getMessage(), e1.getCause());
                } catch (Exception e1) {
                    throw new DeserializerException("Deserializer " + deserializerClass + " doesn't have neither default contructor or a Map argument constructor!", e1);
                }
            } catch (Exception e) {
                throw new DeserializerException(e);
            }
        }
    }
}

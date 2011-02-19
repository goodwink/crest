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

import org.codegist.common.collect.Maps;
import org.codegist.common.reflect.Types;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.io.Reader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public class SimpleXmlDeserializer implements Deserializer {
    
    public static final String STRICT_PROP = SimpleXmlDeserializer.class.getName() + "#strict";
    public static final String USER_SERIALIZER_PROP = SimpleXmlDeserializer.class.getName() + "#user-serializer";
    public static final String DATE_FORMAT_PROP = SimpleXmlDeserializer.class.getName() + "#date.format";
    public static final String BOOLEAN_FORMAT_PROP = SimpleXmlDeserializer.class.getName() + "#boolean.format";

    public static final boolean DEFAULT_STRICT = true;
    private final boolean strict;
    private final org.simpleframework.xml.Serializer serializer;

    public SimpleXmlDeserializer() {
        this(new Persister(), DEFAULT_STRICT);
    }
    public SimpleXmlDeserializer(Serializer serializer, boolean strict) {
        this.serializer = serializer;
        this.strict = strict;
    }

    public SimpleXmlDeserializer(Map<String,Object> config) {
        config = Maps.defaultsIfNull(config);
        if(config.containsKey(STRICT_PROP)) {
            this.strict = (Boolean) config.get(STRICT_PROP);
        }else{
            this.strict = DEFAULT_STRICT;
        }
        if(config.containsKey(USER_SERIALIZER_PROP)) {
            this.serializer = (Serializer) config.get(USER_SERIALIZER_PROP);
        }else{
            MatcherRegistry.Builder registry = new MatcherRegistry.Builder();
            if(config.containsKey(DATE_FORMAT_PROP)) {
                registry.bind(Date.class, new DateMatcher((String) config.get(DATE_FORMAT_PROP)));
            }
            if(config.containsKey(BOOLEAN_FORMAT_PROP)) {
                String trueVal = ((String)config.get(BOOLEAN_FORMAT_PROP)).split(":")[0];
                String falseVal = ((String)config.get(BOOLEAN_FORMAT_PROP)).split(":")[1];
                registry.bind(Boolean.class, new BooleanMatcher(trueVal, falseVal));
            }
            if(registry.hasTransformers()) {
                this.serializer = new Persister(registry.build());
            }else{
                this.serializer = new Persister();
            }

        }
    }
    
    
    public <T> T deserialize(Reader reader, Type type) throws DeserializerException {
        try {
            return serializer.read((Class<T>) Types.getClass(type), reader, strict);
        } catch (Exception e) {
            throw new DeserializerException(e);
        }
    }

    public static class MatcherRegistry implements Matcher {
        private final Map<Class, Transform> transformerMap;

        private MatcherRegistry(Map<Class, Transform> transformerMap) {
            this.transformerMap = transformerMap;
        }

        private static class Builder {
            private final Map<Class, Transform> transformerMap = new HashMap<Class, Transform>();

            public <T> Builder bind(Class<T> clazz, Transform<T> transform) {
                transformerMap.put(clazz, transform);
                return this;
            }

            public boolean hasTransformers(){
                return !transformerMap.isEmpty();
            }

            public MatcherRegistry build() {
                return new MatcherRegistry(transformerMap);
            }
        }

        public Transform match(Class type) throws Exception {
            return transformerMap.get(type);
        }
    }

    public static class BooleanMatcher implements Transform<Boolean> {
        private final String trueVal;
        private final String falseVal;

        private BooleanMatcher(String trueVal, String falseVal) {
            this.trueVal = trueVal;
            this.falseVal = falseVal;
        }

        public Boolean read(String value) throws Exception {
            return !falseVal.equals(value);
        }

        public String write(Boolean value) throws Exception {
            return Boolean.TRUE.equals(value) ? trueVal : falseVal;
        }
    }

    public static class DateMatcher implements Transform<Date> {
        private final DateFormat DF;

        private DateMatcher(String format) {
            this.DF = new SimpleDateFormat(format);
        }

        public synchronized Date read(String value) throws Exception {
            return DF.parse(value);
        }

        public synchronized String write(Date value) throws Exception {
            return DF.format(value);
        }
    }
}

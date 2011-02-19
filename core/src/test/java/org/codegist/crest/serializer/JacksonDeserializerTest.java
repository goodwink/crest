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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author laurent.gilles@codegist.org
 */
public class JacksonDeserializerTest {
    @org.junit.Test
    public void testConstructor1() {
        JacksonDeserializer deserializer = new JacksonDeserializer();
        Test t = deserializer.deserialize(new StringReader("{\"test\":1,\"aa\":2}"), Test.class);
        assertEquals(1, t.test);
    }
    @org.junit.Test
    public void testConstructor2() throws IOException {
        JacksonDeserializer deserializer = new JacksonDeserializer(getMapper());
        Test t = deserializer.deserialize(new StringReader("{\"test\":1,\"aa\":2}"), Test.class);
        assertEquals(111, t.test);
    }
    @org.junit.Test(expected = IllegalArgumentException.class)
    public void testConstructor2Null() throws IOException {
        new JacksonDeserializer((ObjectMapper) null);
    }

    @org.junit.Test
    public void testConstructor3_CustomObjectMapper() throws IOException {
        Map<String,Object> m = new HashMap<String,Object>();
        m.put(JacksonDeserializer.USER_OBJECT_MAPPER_PROP, getMapper());
        JacksonDeserializer deserializer = new JacksonDeserializer(m);
        Test t = deserializer.deserialize(new StringReader("{\"test\":1,\"aa\":2}"), Test.class);
        assertEquals(111, t.test);
    }
    @org.junit.Test
    public void testConstructor3_CustomDeserializationConfig() throws IOException {
        Map<String,Object> m = new HashMap<String,Object>();
        m.put(JacksonDeserializer.DESERIALIZATION_CONFIG_MAP_PROP, new HashMap<String, Boolean>());
        JacksonDeserializer deserializer = new JacksonDeserializer(m);
        try {
            deserializer.deserialize(new StringReader("{\"test\":1,\"aa\":2}"), Test.class);
            fail("should have failed");
        } catch (Exception e) {
        }

    }
    @org.junit.Test(expected = IllegalArgumentException.class)
    public void testConstructor3Null() throws IOException {
        new JacksonDeserializer((Map)null);
    }

    private static ObjectMapper getMapper() throws IOException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.readValue(any(Reader.class), any(JavaType.class))).thenReturn(new Test(111));
        when(mapper.configure(any(DeserializationConfig.Feature.class), anyBoolean())).thenReturn(mapper);
        return mapper;
    }

    static class Test {

        @JsonProperty("test")
        int test;

        Test() {

        }
        Test(int test) {
            this.test = test;
        }

        public int getTest() {
            return test;
        }

        public void setTest(int test) {
            this.test = test;
        }
    }
}

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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ArraySerializerTest {

    private final Serializer itemSerializer = new Serializer() {
        public String serialize(Object value) {
            return String.valueOf(value);
        }
    };
    
    @Test(expected = NullPointerException.class)
    public void testNull(){
        new ArraySerializer(itemSerializer).serialize(null);
    }
    @Test
    public void testDefaultEmpty(){
        Serializer s = new ArraySerializer(itemSerializer);
        assertEquals("", s.serialize(new int[0]));
        assertEquals("", s.serialize(new ArrayList()));
    }
    
    @Test
    public void testDefault1(){
        Serializer s = new ArraySerializer(itemSerializer);
        assertEquals("4,5,6", s.serialize(new int[]{4,5,6}));
        assertEquals("4,5,6", s.serialize(Arrays.asList(4,5,6)));
    }

    @Test
    public void testDefault2(){
        Serializer s = new ArraySerializer(new Serializer() {

            public String serialize(Object value) {
                return  "[" + String.valueOf(value) + "]";
            }
        });
        assertEquals("[4],[5],[6]", s.serialize(new int[]{4,5,6}));
        assertEquals("[4],[5],[6]", s.serialize(Arrays.asList(4,5,6)));
    }

    @Test
    public void testCustom1(){
        Serializer s = new ArraySerializer(itemSerializer, "()");
        assertEquals("4()5()6", s.serialize(new int[]{4,5,6}));
        assertEquals("4()5()6", s.serialize(Arrays.asList(4,5,6)));
    }

    @Test
    public void testCustom2(){
        Serializer s = new ArraySerializer(new Serializer() {
            public String serialize(Object value) {
                return  "[" + String.valueOf(value) + "]";
            }
        }, "()");
        assertEquals("[4]()[5]()[6]", s.serialize(new int[]{4,5,6}));
        assertEquals("[4]()[5]()[6]", s.serialize(Arrays.asList(4,5,6)));
    }



}

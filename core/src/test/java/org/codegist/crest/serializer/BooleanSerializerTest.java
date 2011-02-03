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

import org.codegist.crest.CRestProperty;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class BooleanSerializerTest {
    
    @Test(expected = NullPointerException.class)
    public void testNull(){
        new BooleanSerializer().serialize(null);
    }
    @Test
    public void testDefaults1(){
        test(new BooleanSerializer(), "true", "false");
    }
    @Test
    public void testDefaults2(){
        test(new BooleanSerializer(new HashMap<String, Object>()), "true", "false");
    }
    @Test
    public void testCustom1(){
        test(new BooleanSerializer("a","b"), "a", "b");
    }
    @Test
    public void testCustom2(){
        test(new BooleanSerializer(new HashMap<String, Object>(){{
            put(CRestProperty.SERIALIZER_BOOLEAN_FALSE, "fff");
            put(CRestProperty.SERIALIZER_BOOLEAN_TRUE, "ttt");
        }}), "ttt", "fff");
    }

    public static void test(BooleanSerializer serializer, String expectedTrue, String expectedFalse){
        assertEquals(expectedTrue, serializer.serialize(true));
        assertEquals(expectedFalse, serializer.serialize(false));
        assertEquals(expectedTrue, serializer.serialize(Boolean.TRUE));
        assertEquals(expectedFalse, serializer.serialize(Boolean.FALSE));
    }
}

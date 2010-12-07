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

import org.codegist.common.reflect.Types;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

import static org.codegist.crest.CRestProperty.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class SerializersTest {

    @Test
    public void testDateSerialization(){
        Serializer datesSerializer = Serializers.getFor(null, Date[].class);
        Serializer dateSerializer = Serializers.getFor(null, Date.class);
        assertEquals("2010-11-23T14:28:14GMT", dateSerializer.serialize(new Date(1290522494365l)));
        assertEquals("2010-11-23T14:28:14GMT,2010-12-05T04:14:54GMT", datesSerializer.serialize(new Date[]{new Date(1290522494365l),new Date(1291522494365l)}));

        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_DATE_FORMAT, "dd/yyyy/MM");
        }};
        datesSerializer = Serializers.getFor(customProperties, Date[].class);
        dateSerializer = Serializers.getFor(customProperties, Date.class);
        assertEquals("23/2010/11", dateSerializer.serialize(new Date(1290522494365l)));
        assertEquals("23/2010/11-05/2010/12", datesSerializer.serialize(new Date[]{new Date(1290522494365l),new Date(1291522494365l)}));

        customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_DATE_FORMAT, "Second");
        }};
        datesSerializer = Serializers.getFor(customProperties, Date[].class);
        dateSerializer = Serializers.getFor(customProperties, Date.class);
        assertEquals("1290522494", dateSerializer.serialize(new Date(1290522494365l)));
        assertEquals("1290522494-1291522494", datesSerializer.serialize(new Date[]{new Date(1290522494365l),new Date(1291522494365l)}));
    }
    @Test
    public void testSerialization(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(int.class, new IntSerializer());
                put(boolean.class, new BooleanSerializer());
            }});
        }};
        Serializer intSerializer = Serializers.getFor(customProperties, int[].class);
        Serializer boolSerializer = Serializers.getFor(customProperties, boolean[].class);
        assertEquals("2-4-6-8", intSerializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", intSerializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", intSerializer.serialize(new Integer[]{Integer.valueOf(1),Integer.valueOf(2),Integer.valueOf(3),Integer.valueOf(4)}));
        assertEquals("2-4-6-8", intSerializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
        assertEquals("2-4-6-8", intSerializer.serialize(Arrays.<Integer>asList(Integer.valueOf(1),Integer.valueOf(2),Integer.valueOf(3),Integer.valueOf(4))));
        assertEquals("1-0", boolSerializer.serialize(new boolean[]{true,false}));
        assertEquals("1-0", boolSerializer.serialize(new Boolean[]{Boolean.TRUE,Boolean.FALSE}));
        assertEquals("1-0", boolSerializer.serialize(Arrays.<Boolean>asList(Boolean.TRUE,Boolean.FALSE)));
    }

    @Test
    public void testSerialization1(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
        }};
        Serializer serializer = Serializers.getFor(customProperties, int[].class);
        assertEquals("1-2-3-4", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("1-2-3-4", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("1-2-3-4", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
        serializer = Serializers.getFor(null, int[].class);
        assertEquals("1,2,3,4", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("1,2,3,4", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("1,2,3,4", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerialization2(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(int.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, int[].class);

        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerialization3(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, Integer[].class);
        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerialization4(){
        Serializer serializer = Serializers.getFor(null, Types.newType(List.class, Integer.class));
        assertEquals("1,2,3,4", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("1,2,3,4", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("1,2,3,4", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerialization5(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties,  Types.newType(List.class, Integer.class));
        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerialization6(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties,  Types.newType(List.class, Integer.class));
        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerializer1() {
        Serializer serializer = Serializers.getFor(null, int.class);
        assertEquals(ToStringSerializer.class, serializer.getClass());
    }

    @Test
    public void testSerializerArray1() {
        Serializer serializer = Serializers.getFor(null, int[].class);
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ToStringSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals(",", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerArray2() {

        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(int.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, int[].class);
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(IntSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerArray3() {

        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(int.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, int[][].class);
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ToStringSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerArray4() {

        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(int.class, new IntSerializer());
                put(int[].class, new ArraySerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, int[][].class);
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ArraySerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }


   @Test
    public void testSerializerList1() {

        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
        }};
        Serializer serializer = Serializers.getFor(customProperties, Types.newType(List.class, Object.class));
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ToStringSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerList2() {
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, Types.newType(List.class, Integer.class));
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(IntSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerList3() {
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, Types.newType(List.class, Types.newType(List.class, Integer.class)));
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ToStringSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerList4() {
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(SERIALIZER_LIST_SEPARATOR, "-");
            put(SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
                put(Types.newType(List.class, Integer.class), new ArraySerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, Types.newType(List.class, Types.newType(List.class, Integer.class)));
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ArraySerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    private static class IntSerializer implements Serializer<Integer> {
        @Override
        public String serialize(Integer value) {
            return String.valueOf(value * 2);
        }
    }
    private static class BooleanSerializer implements Serializer<Boolean> {
        @Override
        public String serialize(Boolean value) {
            return value ? "1" : "0";
        }
    }
}

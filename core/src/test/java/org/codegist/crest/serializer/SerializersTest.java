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
import org.codegist.crest.config.ParamConfig;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class SerializersTest {

    @Test
    public void testDateSerilization(){
        Serializer datesSerializer = Serializers.getFor(null, Date[].class);
        Serializer dateSerializer = Serializers.getFor(null, Date.class);
        assertEquals("1290522494365", dateSerializer.serialize(new Date(1290522494365l)));
        assertEquals("1290522494365,1291522494365", datesSerializer.serialize(new Date[]{new Date(1290522494365l),new Date(1291522494365l)}));

        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(DateSerializer.DATEFORMAT_PROP, "dd/yyyy/MM");
        }};
        datesSerializer = Serializers.getFor(customProperties, Date[].class);
        dateSerializer = Serializers.getFor(customProperties, Date.class);
        assertEquals("23/2010/11", dateSerializer.serialize(new Date(1290522494365l)));
        assertEquals("23/2010/11-05/2010/12", datesSerializer.serialize(new Date[]{new Date(1290522494365l),new Date(1291522494365l)}));

        customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(DateSerializer.DATEFORMAT_TYPE_PROP, DateSerializer.FormatType.Second);
        }};
        datesSerializer = Serializers.getFor(customProperties, Date[].class);
        dateSerializer = Serializers.getFor(customProperties, Date.class);
        assertEquals("1290522494", dateSerializer.serialize(new Date(1290522494365l)));
        assertEquals("1290522494-1291522494", datesSerializer.serialize(new Date[]{new Date(1290522494365l),new Date(1291522494365l)}));
    }
    @Test
    public void testSerilization(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
    public void testSerilization1(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
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
    public void testSerilization2(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
                put(int.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, int[].class);

        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerilization3(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties, Integer[].class);
        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerilization4(){
        Serializer serializer = Serializers.getFor(null, Types.newType(List.class, Integer.class));
        assertEquals("1,2,3,4", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("1,2,3,4", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("1,2,3,4", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerilization5(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
                put(Integer.class, new IntSerializer());
            }});
        }};
        Serializer serializer = Serializers.getFor(customProperties,  Types.newType(List.class, Integer.class));
        assertEquals("2-4-6-8", serializer.serialize(new int[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(new Integer[]{1,2,3,4}));
        assertEquals("2-4-6-8", serializer.serialize(Arrays.<Integer>asList(1,2,3,4)));
    }

    @Test
    public void testSerilization6(){
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
            put(ArraySerializer.SEPARATOR_PROP, "-");
        }};
        Serializer serializer = Serializers.getFor(customProperties, Types.newType(List.class, Object.class));
        assertEquals(ArraySerializer.class, serializer.getClass());
        assertEquals(ToStringSerializer.class, ((ArraySerializer) serializer).itemSerializer.getClass());
        assertEquals("-", ((ArraySerializer) serializer).separator);
    }

    @Test
    public void testSerializerList2() {
        Map<String,Object> customProperties = new HashMap<String, Object>(){{
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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
            put(ArraySerializer.SEPARATOR_PROP, "-");
            put(ParamConfig.DEFAULT_SERIALIZERS_MAP_PROP, new HashMap<Type, Serializer>() {{
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

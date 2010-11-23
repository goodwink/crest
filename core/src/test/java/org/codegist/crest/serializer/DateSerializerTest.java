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

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateSerializerTest {

    @Test
    public void testDateFormatNull(){
        DateSerializer serializer = new DateSerializer();
        assertEquals("", serializer.serialize(null));
    }
    @Test
    public void testDateFormat(){
        DateSerializer serializer = new DateSerializer("dd/MM/yyyy");
        assertEquals("23/11/2010", serializer.serialize(new Date(1290524180273l)));
    }
    @Test
    public void testDateFormatMillis(){
        DateSerializer serializer = new DateSerializer(DateSerializer.FormatType.Millis);
        assertEquals("1290524180273", serializer.serialize(new Date(1290524180273l)));
    }
    @Test
    public void testDateFormatSeconds(){
        DateSerializer serializer = new DateSerializer(DateSerializer.FormatType.Second);
        assertEquals("1290524180", serializer.serialize(new Date(1290524180273l)));
    }
    @Test
    public void testDateFormatMinutes(){
        DateSerializer serializer = new DateSerializer(DateSerializer.FormatType.Minutes);
        assertEquals("21508736", serializer.serialize(new Date(1290524180273l)));
    }
    @Test
    public void testDateFormatHours(){
        DateSerializer serializer = new DateSerializer(DateSerializer.FormatType.Hours);
        assertEquals("358478", serializer.serialize(new Date(1290524180273l)));
    }
    @Test
    public void testDateFormatDays(){
        DateSerializer serializer = new DateSerializer(DateSerializer.FormatType.Days);
        assertEquals("14936", serializer.serialize(new Date(1290524180273l)));
    }
}

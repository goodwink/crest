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
public class ToStringSerializerTest {

    @Test
    public void test(){
        Serializer serializer = new ToStringSerializer();
        assertEquals("", serializer.serialize(null));
        assertEquals("", serializer.serialize(""));
        assertEquals(" ", serializer.serialize(" "));
        assertEquals("1", serializer.serialize(Integer.valueOf(1)));
        assertEquals("false", serializer.serialize(false));
        assertEquals("hello", serializer.serialize(new Object(){
            @Override
            public String toString() {
                return "hello";
            }
        }));
    }
}

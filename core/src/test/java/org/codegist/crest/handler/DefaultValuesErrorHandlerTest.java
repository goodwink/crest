/*
 * Copyright 2010 CodeGist.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.handler;

import org.codegist.crest.ResponseContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DefaultValuesErrorHandlerTest {

    @Test
    public void testDefaultValue() throws Exception {
        DefaultValuesErrorHandler handler = new DefaultValuesErrorHandler();
        assertEquals(Byte.valueOf((byte)0), handler.handle(getResponseContext(byte.class), mock(Exception.class)));
        assertEquals(Short.valueOf((short)0), handler.handle(getResponseContext(short.class), mock(Exception.class)));
        assertEquals(Integer.valueOf(0), handler.handle(getResponseContext(int.class), mock(Exception.class)));
        assertEquals(Long.valueOf(0), handler.handle(getResponseContext(long.class), mock(Exception.class)));
        assertEquals(Float.valueOf(0), handler.handle(getResponseContext(float.class), mock(Exception.class)));
        assertEquals(Double.valueOf(0d), handler.handle(getResponseContext(double.class), mock(Exception.class)));
        assertEquals(Boolean.valueOf(false), handler.handle(getResponseContext(boolean.class), mock(Exception.class)));
        assertEquals(Character.valueOf((char)0), handler.handle(getResponseContext(char.class), mock(Exception.class)));
        assertNull(handler.handle(getResponseContext(Object.class), mock(Exception.class)));
        assertNull(handler.handle(getResponseContext(char[].class), mock(Exception.class)));
    }

    private static ResponseContext getResponseContext(Class type) throws NoSuchMethodException {
        ResponseContext responseContext = mock(ResponseContext.class);
        when(responseContext.getExpectedGenericType()).thenReturn(type);
        return responseContext;
    }
}

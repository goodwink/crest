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

package org.codegist.crest.handler;

import org.codegist.crest.CRestProperty;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class MaxAttemptRetryHandlerTest {

    @Test
    public void testDefault(){
        MaxAttemptRetryHandler handler = new MaxAttemptRetryHandler();
        assertFalse(handler.retry(null, null, 0));
        assertFalse(handler.retry(null, null, 1));
    }
    @Test
    public void testDefault2(){
        MaxAttemptRetryHandler handler = new MaxAttemptRetryHandler(new HashMap<String, Object>());
        assertFalse(handler.retry(null, null, 0));
        assertFalse(handler.retry(null, null, 1));
    }
    @Test
    public void testCustom1(){
        MaxAttemptRetryHandler handler = new MaxAttemptRetryHandler(3);
        assertTrue(handler.retry(null, null, 0));
        assertTrue(handler.retry(null, null, 1));
        assertTrue(handler.retry(null, null, 2));
        assertFalse(handler.retry(null, null, 3));
        assertFalse(handler.retry(null, null, 4));
    }
    @Test
    public void testCustom2(){
        MaxAttemptRetryHandler handler = new MaxAttemptRetryHandler(new HashMap<String, Object>(){{
            put(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS, "3");
        }});
        assertTrue(handler.retry(null, null, 0));
        assertTrue(handler.retry(null, null, 1));
        assertTrue(handler.retry(null, null, 2));
        assertFalse(handler.retry(null, null, 3));
        assertFalse(handler.retry(null, null, 4));
    }


}

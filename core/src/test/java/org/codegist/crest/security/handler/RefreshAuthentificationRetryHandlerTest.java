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

package org.codegist.crest.security.handler;

import org.codegist.crest.CRestProperty;
import org.codegist.crest.HttpException;
import org.codegist.crest.HttpResponse;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.security.AuthentificationManager;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class RefreshAuthentificationRetryHandlerTest {

    @Test(expected = NullPointerException.class)
    public void testInvalidArguments(){
        new RefreshAuthentificationRetryHandler(null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArguments2(){
        new RefreshAuthentificationRetryHandler(new HashMap<String, Object>());
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArguments3(){
        new RefreshAuthentificationRetryHandler(new HashMap<String, Object>(){{
            put(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS, "10");
        }});
    }
    @Test
    public void testRetry(){
        RefreshAuthentificationRetryHandler handler = new RefreshAuthentificationRetryHandler(new HashMap<String, Object>(){{
            put(AuthentificationManager.class.getName(), mock(AuthentificationManager.class));
        }});
        assertTrue(handler.retry(mock(ResponseContext.class), getHttpException(401), 1));
        assertFalse(handler.retry(mock(ResponseContext.class), getHttpException(401), 2));
    }
    @Test
    public void testRetryOverrideMax(){
        RefreshAuthentificationRetryHandler handler = new RefreshAuthentificationRetryHandler(new HashMap<String, Object>(){{
            put(AuthentificationManager.class.getName(), mock(AuthentificationManager.class));
            put(CRestProperty.HANDLER_RETRY_MAX_ATTEMPTS, "2");
        }});
        assertTrue(handler.retry(mock(ResponseContext.class), getHttpException(401), 1));
        assertTrue(handler.retry(mock(ResponseContext.class), getHttpException(401), 2));
        assertFalse(handler.retry(mock(ResponseContext.class), getHttpException(401), 3));
    }

    @Test
    public void testNoRetry(){
        RefreshAuthentificationRetryHandler handler = new RefreshAuthentificationRetryHandler(new HashMap<String, Object>(){{
            put(AuthentificationManager.class.getName(), mock(AuthentificationManager.class));
        }});
        assertFalse(handler.retry(mock(ResponseContext.class), getHttpException(500), 1));
        assertFalse(handler.retry(mock(ResponseContext.class), getHttpException(500), 2));
        assertFalse(handler.retry(mock(ResponseContext.class), mock(Exception.class), 1));
        assertFalse(handler.retry(mock(ResponseContext.class), mock(Exception.class), 2));
    }


    private static Exception getHttpException(int code){
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(code);
        HttpException validException = mock(HttpException.class);
        when(validException.getResponse()).thenReturn(response);
        return validException;
    }
}

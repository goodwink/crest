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

package org.codegist.crest.security.interceptor;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;
import org.codegist.crest.security.AuthentificationManager;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class AuthentificationInterceptorTest {

    @Test(expected = NullPointerException.class)
    public void testInvalidArguments(){
        new AuthentificationInterceptor((Map<String, Object>)null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArguments2(){
        new AuthentificationInterceptor(new HashMap<String, Object>());
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArguments3(){
        new AuthentificationInterceptor((AuthentificationManager)null);
    }

    @Test
    public void testIntercept() throws Exception {
        HttpRequest.Builder request = mock(HttpRequest.Builder.class);
        RequestContext context = mock(RequestContext.class);
        AuthentificationManager manager = mock(AuthentificationManager.class);
        AuthentificationInterceptor interceptor = new AuthentificationInterceptor(manager);
        interceptor.beforeParamsInjectionHandle(request, context);
        verify(manager, times(0)).sign(request);
        interceptor.afterParamsInjectionHandle(request, context);
        verify(manager, times(1)).sign(request);
    }


}

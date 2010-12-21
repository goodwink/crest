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

package org.codegist.crest.security;

import org.codegist.common.lang.Pair;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.Token;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthentificationManagerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testOAuthentificationManagerNoAuthenticator(){
        new OAuthentificationManager(null,null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testOAuthentificationManagerNoToken(){
        new OAuthentificationManager(mock(OAuthenticator.class),null);
    }
    @Test
    public void testOAuthentificationManagerSign(){
        OAuthenticator authenticator = mock(OAuthenticator.class);
        Token accessToken = mock(Token.class);
        HttpRequest.Builder request = mock(HttpRequest.Builder.class);
        Pair<String,String>[] properties = (Pair<String,String>[]) new Pair[]{mock(Pair.class), mock(Pair.class)};
        OAuthentificationManager manager = new OAuthentificationManager(authenticator, accessToken);
        manager.sign(request, properties);
        verify(authenticator).sign(accessToken, request, properties);
    }
    @Test
    public void testOAuthentificationManagerRefresh(){
        OAuthenticator authenticator = mock(OAuthenticator.class);
        Token accessToken = mock(Token.class);
        OAuthentificationManager manager = new OAuthentificationManager(authenticator, accessToken);
        manager.refresh();
        verify(authenticator).refreshAccessToken(accessToken, "oauth_session_handle");
    }
}

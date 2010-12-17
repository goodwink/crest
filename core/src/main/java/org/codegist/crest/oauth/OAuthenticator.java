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

package org.codegist.crest.oauth;

import org.codegist.common.lang.Pair;
import org.codegist.crest.HttpRequest;


/**
 * OAuth authentificator interface
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface OAuthenticator {

    /**
     * Signs the given request using the given access token and the optional additional oauth headers.
     * @param accessToken Access token to be used
     * @param request request to be signed
     * @param extraAuthHeaders optional header to be added in the oauth authentification headers
     */
    void sign(Token accessToken, HttpRequest.Builder request, Pair<String,String>... extraAuthHeaders);

    /**
     * Fires a get request token to the preconfigured url
     * @return A new request token
     */
    Token getRequestToken();

    /**
     * Exchanges the given request token with a new access token using the given verifier
     * @param requestToken request token to exchange
     * @param verifier verifier
     * @return new access token
     */
    Token getAccessToken(Token requestToken, String verifier);

    /**
     * Refreshs the given access token if it has expired. Include optional extra oauth header from the extra field of the token.
     * @param accessToken expired access token
     * @param includeExtras extras field name from the given token to include in the request
     * @see org.codegist.crest.oauth.Token#getExtras()
     * @return a new access token
     */
    Token refreshAccessToken(Token accessToken, String... includeExtras);

}

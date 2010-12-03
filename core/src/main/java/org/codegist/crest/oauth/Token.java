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

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.ToStringBuilder;

import java.util.Map;

public class Token {

    private final String token;
    private final String secret;
    private final Map<String,String> extras;

    public Token(String token, String secret) {
        this(token, secret, null);
    }
    public Token(String token, String secret, Map<String,String> extras) {
        this.token = token;
        this.secret = secret;
        this.extras = Maps.unmodifiable(extras);
    }

    public String getToken() {
        return token;
    }

    public String getSecret() {
        return secret;
    }

    public String getExtra(String name){
        return extras.get(name);
    }

    public Map<String,String> getExtras(){
        return extras;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("token", token)
                .append("secret", secret)
                .append("extras", extras)
                .toString();
    }
}

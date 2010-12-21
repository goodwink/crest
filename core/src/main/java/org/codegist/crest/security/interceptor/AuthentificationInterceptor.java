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

package org.codegist.crest.security.interceptor;

import org.codegist.common.lang.Validate;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.RequestContext;
import org.codegist.crest.interceptor.RequestInterceptorAdapter;
import org.codegist.crest.security.AuthentificationManager;

import java.util.Map;

/**
 * Authentification interceptor.
 * <p>Requires an {@link org.codegist.crest.security.AuthentificationManager} instance to be present in the custom properties.
 */
public class AuthentificationInterceptor extends RequestInterceptorAdapter {

    private final AuthentificationManager authentificationManager;

    public AuthentificationInterceptor(Map<String, Object> customProperties) {
        this((AuthentificationManager) customProperties.get(AuthentificationManager.class.getName()));
    }
    public AuthentificationInterceptor(AuthentificationManager authentificationManager) {
        Validate.notNull(authentificationManager, "No authentification manager found, please pass it in the properties (key=" + AuthentificationManager.class.getName() + ")");
        this.authentificationManager = authentificationManager;
    }

    @Override
    public void afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        authentificationManager.sign(builder);
    }

}

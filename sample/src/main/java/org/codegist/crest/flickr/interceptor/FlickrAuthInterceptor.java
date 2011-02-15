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

package org.codegist.crest.flickr.interceptor;

import org.codegist.common.codec.Hex;
import org.codegist.common.lang.Validate;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.Params;
import org.codegist.crest.RequestContext;
import org.codegist.crest.interceptor.RequestInterceptorAdapter;

import java.security.MessageDigest;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class FlickrAuthInterceptor extends RequestInterceptorAdapter {

    public static final String APP_SECRET_PROP = FlickrAuthInterceptor.class.getName() + "#app.secret";
    public static final String API_KEY_PROP = FlickrAuthInterceptor.class.getName() + "#api.key";
    public static final String AUTH_TOKEN_PROP = FlickrAuthInterceptor.class.getName() + "#auth.token";

    private final String appSecret;
    private final String apiKey;
    private final String authToken;

    public FlickrAuthInterceptor(Map<String, Object> properties) {
        this.appSecret = (String) properties.get(APP_SECRET_PROP);
        this.apiKey = (String) properties.get(API_KEY_PROP);
        this.authToken = (String) properties.get(AUTH_TOKEN_PROP);

        Validate.notEmpty(this.appSecret, "App secret is required, please pass it in the properties (key=" + APP_SECRET_PROP + ")");
        Validate.notEmpty(this.apiKey, "API key is required, please pass it in the properties (key=" + API_KEY_PROP + ")");
        Validate.notEmpty(this.authToken, "Authentification token is required, please pass it in the properties (key=" + AUTH_TOKEN_PROP + ")");
    }

    @Override
    public void afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) throws Exception {
        StringBuilder sb = new StringBuilder(appSecret);

        if (isForBody(builder.getMeth())) {
            builder.addFormParam("api_key", apiKey);
            builder.addFormParam("auth_token", authToken);
        } else {
            builder.addQueryParam("api_key", apiKey);
            builder.addQueryParam("auth_token", authToken);
        }

        SortedMap<String, String> map = new TreeMap<String, String>(builder.getQueryParams());
        if (builder.getFormParams() != null) {
            for (Map.Entry<String, Object> param : builder.getFormParams().entrySet()) {
                if (Params.isForUpload(param.getValue())) continue;
                map.put(param.getKey(), String.valueOf(param.getValue()));
            }
        }

        for (Map.Entry<String, String> param : map.entrySet()) sb.append(param.getKey()).append(param.getValue());

        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(sb.toString().getBytes());
        String hash = Hex.encodeAsString(digest.digest());
        if (isForBody(builder.getMeth())) {
            builder.addFormParam("api_sig", hash);
        } else {
            builder.addQueryParam("api_sig", hash);
        }
    }

    private boolean isForBody(String meth) {
        return HttpRequest.HTTP_POST.equals(meth) || HttpRequest.HTTP_PUT.equals(meth);
    }
}

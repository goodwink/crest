package org.codegist.crest.flickr.interceptor;

import org.codegist.common.codec.Hex;
import org.codegist.crest.*;
import org.codegist.crest.interceptor.RequestInterceptorAdapter;

import java.security.MessageDigest;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class FlickrAuthInterceptor extends RequestInterceptorAdapter {

    public static final String APP_SECRET_PROP = FlickrAuthInterceptor.class.getName() + "#app.secret";
    public static final String API_KEY_PROP = FlickrAuthInterceptor.class.getName() + "#api.key";
    public static final String AUTH_TOKEN_PROP = FlickrAuthInterceptor.class.getName() + "#auth.token";

    private boolean isForBody(HttpMethod m) {
        return HttpMethod.POST.equals(m) || HttpMethod.PUT.equals(m);
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        StringBuilder sb = new StringBuilder((String) context.getCustomProperties().get(APP_SECRET_PROP));
        String apiKey = (String) context.getCustomProperties().get(API_KEY_PROP);
        String authToken = (String) context.getCustomProperties().get(AUTH_TOKEN_PROP);

        if (isForBody(builder.getMeth())) {
            builder.addBodyParam("api_key", apiKey);
            builder.addBodyParam("auth_token", authToken);
        } else {
            builder.addQueryParam("api_key", apiKey);
            builder.addQueryParam("auth_token", authToken);
        }

        SortedMap<String, String> map = new TreeMap<String, String>(builder.getQueryString());
        if (builder.getBodyParams() != null) {
            for (Map.Entry<String, Object> param : builder.getBodyParams().entrySet()) {
                if (Params.isForUpload(param.getValue())) continue;
                map.put(param.getKey(), String.valueOf(param.getValue()));
            }
        }

        for (Map.Entry<String, String> param : map.entrySet()) sb.append(param.getKey()).append(param.getValue());

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(sb.toString().getBytes());
            String hash = Hex.encodeHex(digest.digest());
            if (isForBody(builder.getMeth())) {
                builder.addBodyParam("api_sig", hash);
            } else {
                builder.addQueryParam("api_sig", hash);
            }
            return true;
        } catch (Exception e) {
            throw new CRestException(e);
        }
    }


}

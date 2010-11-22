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

package org.codegist.crest.interceptor;

import org.codegist.common.codec.Base64;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.Params;
import org.codegist.crest.RequestContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Simple OAuth interceptor, required a valid pregenerated access token. Only support HMAC-SHA1 signature method.
 * <p>Can be configured either be constructor parameters, or indirectly via {@link org.codegist.crest.RequestContext#getCustomProperties()} with the following key :
 * <p>OAuthInterceptor.{@link OAuthInterceptor#OAUTH_PARAM_DEST_PROP}
 * <p>OAuthInterceptor.{@link OAuthInterceptor#CONSUMER_SECRET_PROP}
 * <p>OAuthInterceptor.{@link OAuthInterceptor#CONSUMER_KEY_PROP}
 * <p>OAuthInterceptor.{@link OAuthInterceptor#ACCESS_TOKEN_SECRET_PROP}
 * <p>OAuthInterceptor.{@link OAuthInterceptor#ACCESS_TOKEN_PROP}
 *
 * @see org.codegist.crest.RequestContext
 * @see OAuthInterceptor#OAUTH_PARAM_DEST_PROP
 * @see OAuthInterceptor#CONSUMER_SECRET_PROP
 * @see OAuthInterceptor#ACCESS_TOKEN_SECRET_PROP
 * @see OAuthInterceptor#ACCESS_TOKEN_PROP
 * @see org.codegist.crest.CRestContext#getCustomProperties()
 * @see org.codegist.crest.DefaultCRest#DefaultCRest(org.codegist.crest.CRestContext)
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthInterceptor extends RequestInterceptorAdapter {

    private final static String SIGN_METH = "HMAC-SHA1";
    private final static String SIGN_METH_4_J = "HmacSHA1";
    private final static Random RDM = new SecureRandom();

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to specify where should the authentification parameter be added in the request, either in the URL or in the headers.
     * <p>Expects a value of type {@link OAuthParamDest}.
     *
     * @see OAuthParamDest
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    public static final String OAUTH_PARAM_DEST_PROP = OAuthInterceptor.class.getName() + "#oauth.param.dest";

    /**
     * Use this parameter in the {@link org.codegist.crest.RequestContext#getCustomProperties()} to specify the preconfigured consumer secret.
     * <p>Expects a string.
     *
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    public static final String CONSUMER_SECRET_PROP = OAuthInterceptor.class.getName() + "#consumer.secret";

    /**
     * Use this parameter in the {@link org.codegist.crest.RequestContext#getCustomProperties()} to specify the preconfigured consumer key.
     * <p>Expects a string value.
     *
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    public static final String CONSUMER_KEY_PROP = OAuthInterceptor.class.getName() + "#consumer.key";

    /**
     * Use this parameter in the {@link org.codegist.crest.RequestContext#getCustomProperties()} to specify the preconfigured access token key.
     * <p>Expects a string.
     *
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    public static final String ACCESS_TOKEN_PROP = OAuthInterceptor.class.getName() + "#access.token.key";

    /**
     * Use this parameter in the {@link org.codegist.crest.RequestContext#getCustomProperties()} to specify the preconfigured access token secret.
     * <p>Expects a string.
     *
     * @see org.codegist.crest.CRestContext#getCustomProperties()
     */
    public static final String ACCESS_TOKEN_SECRET_PROP = OAuthInterceptor.class.getName() + "#access.token.secret";

    public static enum OAuthParamDest {
        URL, HEADERS
    }


    private volatile OAuthToken accessToken;

    public OAuthInterceptor() {

    }

    public OAuthInterceptor(OAuthParamDest oAuthParamDest, String consumerSecret, String consumerKey, String accessTokenSecret, String accessToken) {
        this.accessToken = new OAuthToken(accessToken, accessTokenSecret, consumerKey, consumerSecret, SIGN_METH_4_J, oAuthParamDest);
    }

    @Override
    public boolean afterParamsInjectionHandle(HttpRequest.Builder builder, RequestContext context) {
        OAuthToken accessToken = getAccessToken(context);
        String enc = context.getConfig().getEncoding();
        Map<String, String> authParams = new LinkedHashMap<String, String>();
        authParams.put("oauth_nonce", String.valueOf(System.currentTimeMillis() / 1000 + RDM.nextInt()));
        authParams.put("oauth_signature_method", SIGN_METH);
        authParams.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        authParams.put("oauth_consumer_key", accessToken.getConsumerKey());
        authParams.put("oauth_version", "1.0");
        authParams.put("oauth_token", accessToken.getToken());
        authParams.putAll(getParamsForAuth(builder));
        authParams.put("oauth_signature", generateSignature(SIGN_METH_4_J, accessToken, builder.getMeth() + "&" + encode(constructRequestURL(builder.getBaseUri()), enc) + "&" + encode(encodeParams(authParams, enc, "&", true, false), enc)));

        if (OAuthParamDest.HEADERS.equals(accessToken.getDest())) {
            builder.addHeader("Authorization", "OAuth " + encodeParams(authParams, enc, ",", false, true, "oauth_callback"));
        } else if (OAuthParamDest.URL.equals(accessToken.getDest())) {
            builder.addQueryParams(authParams);
        }

        return true;
    }


    private OAuthToken getAccessToken(RequestContext context) {
        if (accessToken != null) return accessToken;  // No need for more synchronization, can be done twice, better than synchronizing for every requests.
        if (!context.getCustomProperties().containsKey(ACCESS_TOKEN_PROP)) {
            throw new IllegalStateException("No default access token key set for interceptor. Please either construct the interceptor with the access token key, or pass it into the context (key=" + ACCESS_TOKEN_PROP + ")");
        } else if (!context.getCustomProperties().containsKey(ACCESS_TOKEN_SECRET_PROP)) {
            throw new IllegalStateException("No default access token secret set for interceptor. Please either construct the interceptor with the access token secret, or pass it into the context (key=" + ACCESS_TOKEN_SECRET_PROP + ")");
        } else if (!context.getCustomProperties().containsKey(CONSUMER_KEY_PROP)) {
            throw new IllegalStateException("No default consumer key set for interceptor. Please either construct the interceptor with the consumer key, or pass the consumer key into the context (key=" + CONSUMER_KEY_PROP + ")");
        } else if (!context.getCustomProperties().containsKey(CONSUMER_SECRET_PROP)) {
            throw new IllegalStateException("No default consumer secret set for intecteptor. Please either construct the interceptor passing consumer and access secrect/token, or pass customer properties in the context (key=" + CONSUMER_SECRET_PROP + ")");
        }
        OAuthParamDest dest = (OAuthParamDest) context.getCustomProperties().get(OAUTH_PARAM_DEST_PROP);
        return accessToken = new OAuthToken(
                (String) context.getCustomProperties().get(ACCESS_TOKEN_PROP),
                (String) context.getCustomProperties().get(ACCESS_TOKEN_SECRET_PROP),
                (String) context.getCustomProperties().get(CONSUMER_KEY_PROP),
                (String) context.getCustomProperties().get(CONSUMER_SECRET_PROP),
                SIGN_METH_4_J,
                dest != null ? dest : OAuthParamDest.HEADERS
        );
    }


    private static Map<String, String> getParamsForAuth(HttpRequest.Builder builder) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        if (builder.getQueryString() != null) {
            params.putAll(builder.getQueryString());
        }
        if (builder.getBodyParams() != null)
            for (Map.Entry<String, Object> entry : builder.getBodyParams().entrySet()) {
                if (Params.isForUpload(entry.getValue())) continue;
                params.put(entry.getKey(), entry.getValue().toString());
            }

        return params;
    }

    public static List<Param> toParams(Map<String, String> httpParams) {
        List<Param> p = new ArrayList<Param>();
        for (Map.Entry<String, String> param : httpParams.entrySet()) {
            p.add(new Param(param.getKey(), param.getValue()));
        }
        return p;
    }

    /**
     * The Signature Base String includes the request absolute URL, tying the signature to a specific endpoint. The URL used in the Signature Base String MUST include the scheme, authority, and path, and MUST exclude the query and fragment as defined by [RFC3986] section 3.<br>
     * If the absolute request URL is not available to the Service Provider (it is always available to the Consumer), it can be constructed by combining the scheme being used, the HTTP Host header, and the relative HTTP request URL. If the Host header is not available, the Service Provider SHOULD use the host name communicated to the Consumer in the documentation or other means.<br>
     * The Service Provider SHOULD document the form of URL used in the Signature Base String to avoid ambiguity due to URL normalization. Unless specified, URL scheme and authority MUST be lowercase and include the port number; http default port 80 and https default port 443 MUST be excluded.<br>
     * <br>
     * For example, the request:<br>
     * HTTP://Example.com:80/resource?id=123<br>
     * Is included in the Signature Base String as:<br>
     * http://example.com/resource
     *
     * @param url the url to be normalized
     * @return the Signature Base String
     * @see <a href="http://oauth.net/core/1.0#rfc.section.9.1.2">OAuth Core - 9.1.2.  Construct Request URL</a>
     */
    private static String constructRequestURL(String url) {
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int slashIndex = url.indexOf("/", 8);
        String baseURL = url.substring(0, slashIndex).toLowerCase();
        int colonIndex = baseURL.indexOf(":", 8);
        if (-1 != colonIndex) {
            // url contains port number
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                // http default port 80 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                // http default port 443 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        url = baseURL + url.substring(slashIndex);

        return url;
    }

    private static String encodeParams(Map<String, String> httpParams, String encoding, String sep, boolean sort, boolean quote) {
        return encodeParams(httpParams, encoding, sep, sort, quote, new String[0]);
    }

    private static String encodeParams(Map<String, String> httpParams, String encoding, String sep, boolean sort, boolean quote, String... ignore) {
        StringBuffer buf = new StringBuffer();
        List<Param> list = toParams(httpParams);
        String format = quote ? "\"%s\"" : "%s";
        if (sort) {
            Collections.sort(list);
        }
        List<String> ignores = Arrays.asList(ignore);
        for (Param p : list) {
            if (ignores.contains(p.name)) continue;
            if (buf.length() != 0) {
                buf.append(sep);
            }
            buf.append(String.format(format, encode(p.name, encoding)));
            buf.append("=");
            buf.append(String.format(format, encode(p.value, encoding)));
        }
        return buf.toString();
    }

    /**
     * @param value string to be encoded
     * @return encoded string
     * @see <a href="http://wiki.oauth.net/TestCases">OAuth / TestCases</a>
     * @see <a href="http://groups.google.com/group/oauth/browse_thread/thread/a8398d0521f4ae3d/9d79b698ab217df2?hl=en&lnk=gst&q=space+encoding#9d79b698ab217df2">Space encoding - OAuth | Google Groups</a>
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC 3986 - Uniform Resource Identifier (URI): Generic Syntax - 2.1. Percent-Encoding</a>
     */
    public static String encode(String value, String encoding) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, encoding);
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuffer buf = new StringBuffer(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    private String generateSignature(String signatureMethod, OAuthToken accessToken, String data) {
        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance(signatureMethod);
            mac.init(accessToken.getSecretKeySpec());
            byteHMAC = mac.doFinal(data.getBytes());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException ignore) {
            // should never happen
        }
        return Base64.encodeToString(byteHMAC, false);
    }


    private static class Param implements Comparable<Param> {
        final String name;
        final String value;

        Param(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public int compareTo(Param o) {
            int i = name.compareTo(o.name);
            return i != 0 ? i : value.compareTo(o.value);
        }
    }

    public static class OAuthToken {

        private final String token;
        private final String tokenSecret;

        private final String consumerKey;
        private final String consumerSecret;

        private final SecretKeySpec secretKeySpec;

        private final OAuthParamDest dest;

        public OAuthToken(String token, String tokenSecret, String consumerKey, String consumerSecret, String signatureMethod, OAuthParamDest dest) {
            this.token = token;
            this.tokenSecret = tokenSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.dest = dest;
            String oauthSignature = encode(consumerSecret, "utf-8") + "&" + encode(tokenSecret, "utf-8");
            secretKeySpec = new SecretKeySpec(oauthSignature.getBytes(), signatureMethod);
        }

        public String getToken() {
            return token;
        }

        public String getTokenSecret() {
            return tokenSecret;
        }

        public String getConsumerKey() {
            return consumerKey;
        }

        public String getConsumerSecret() {
            return consumerSecret;
        }

        public OAuthParamDest getDest() {
            return dest;
        }

        public SecretKeySpec getSecretKeySpec() {
            return secretKeySpec;
        }

    }
}

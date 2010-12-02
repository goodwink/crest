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

import org.codegist.common.codec.Base64;
import org.codegist.common.lang.Objects;
import org.codegist.crest.CRestException;
import static org.codegist.common.net.Urls.*;
import org.codegist.crest.HttpMethod;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.Params;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.*;

public class OAuthenticatorV10 implements OAuthenticator {

    private final static String ENC = "UTF-8";
    private final static String SIGN_METH = "HMAC-SHA1";
    private final static String SIGN_METH_4_J = "HmacSHA1";
    private final VariantProvider variant;

    private final Token consumerToken;
    private final Token accessToken;
    private final boolean writeToHeaders;
    private final SecretKeySpec secretKeySpec;


    public OAuthenticatorV10(boolean writeToHeaders, Token consumerToken, Token accessToken, VariantProvider variant) {
        this.consumerToken = consumerToken;
        this.accessToken = accessToken;
        this.writeToHeaders = writeToHeaders;
        this.variant = variant;
        try {
            this.secretKeySpec = new SecretKeySpec((consumerToken.getSecret() + "&" + accessToken.getSecret()).getBytes(ENC), SIGN_METH_4_J);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthException(e);
        }
    }

    public OAuthenticatorV10(boolean writeToHeaders, Token consumerToken, Token accessToken) {
        this(writeToHeaders, consumerToken, accessToken, new DefaultVariantProvider());
    }

    public OAuthenticator refreshAccessToken(Token accessToken) {
        return new OAuthenticatorV10(writeToHeaders, consumerToken, accessToken);
    }

    public void process(HttpRequest.Builder request, Param... extraHeaders) {
        List<Param> extraHeadersList = Arrays.asList(Objects.defaultIfNull(extraHeaders, new Param[0]));
        try {
            process(request, extraHeadersList);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthException(e);
        }
    }

    private void process(HttpRequest.Builder request, List<Param> extraHeaders) throws UnsupportedEncodingException {
        List<Param> oauthParams = newBaseOAuthParams(); // generate base oauth params
        oauthParams.addAll(extraHeaders);

        // Generate params for signature, these params contains the query string and body params on top of the already existing one.
        List<Param> signatureParams = new ArrayList<Param>();
        signatureParams.addAll(oauthParams);
        signatureParams.addAll(extractOAuthParams(request));

        String signature = generateSignature(request, signatureParams);

        // Add signature to the base param list
        oauthParams.add(new Param("oauth_signature", signature));

        if (writeToHeaders) {
            request.addHeader("Authorization", generateOAuthHeader(oauthParams));
        } else {
            for(Param p : oauthParams ){
                request.addQueryParam(p.getName(), p.getValue());
            }
        }
    }

    private String generateOAuthHeader(List<Param> oauthParams) throws UnsupportedEncodingException {
        return "OAuth " + encodeParams(oauthParams, ",", true);
    }

    private List<Param> newBaseOAuthParams() {
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("oauth_consumer_key", consumerToken.getToken()));
        params.add(new Param("oauth_token", accessToken.getToken()));
        params.add(new Param("oauth_signature_method", SIGN_METH));
        params.add(new Param("oauth_timestamp", variant.timestamp()));
        params.add(new Param("oauth_nonce", variant.nonce()));
        params.add(new Param("oauth_version", "1.0"));
        return params;
    }


    private static List<Param> extractOAuthParams(HttpRequest.Builder builder) {
        List<Param> params = new ArrayList<Param>();
        if (builder.getQueryString() != null) {
            params.addAll(toParamList(builder.getQueryString()));
        }
        if (builder.getBodyParams() != null)
            for (Map.Entry<String, Object> entry : builder.getBodyParams().entrySet()) {
                if (Params.isForUpload(entry.getValue())) continue;
                params.add(new Param(entry.getKey(), entry.getValue().toString()));
            }

        return params;
    }

    private static List<Param> toParamList(Map<String, String> params) {
        List<Param> p = new ArrayList<Param>();
        for (Map.Entry<String, String> param : params.entrySet()) {
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
        return baseURL + url.substring(slashIndex);
    }

    private static String encodeParams(List<Param> httpParams, String sep, boolean quote) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        String format = quote ? "\"%s\"" : "%s";
        for (Param p : httpParams) {
            if (buf.length() != 0) {
                buf.append(sep);
            }
            buf.append(encode(p.getName(), ENC));
            buf.append("=");
            buf.append(String.format(format, encode(p.getValue(), ENC)));
        }
        return buf.toString();
    }



    private String generateSignature(HttpRequest.Builder request, List<Param> params) {
        try {
            // first, sort the list without changing the one given
            List<Param> sorted = new ArrayList<Param>(params);
            Collections.sort(sorted);

            String signMeth = String.valueOf(request.getMeth());
            String signUri = constructRequestURL(request.getBaseUri());
            String signParams = encodeParams(sorted, "&", false);

            // format the signature content
            String data = signMeth + "&" + encode(signUri, ENC) + "&" + encode(signParams, ENC);

            Mac mac = Mac.getInstance(SIGN_METH_4_J);
            mac.init(secretKeySpec);

            return new String(Base64.encodeToByte(mac.doFinal(data.getBytes(ENC))), ENC);
        } catch (Exception e) {
            throw new CRestException(e);
        }
    }

    static interface VariantProvider {
        String timestamp();

        String nonce();
    }

    static class DefaultVariantProvider implements VariantProvider {
        private final Random RDM = new SecureRandom(){{
            setSeed(System.currentTimeMillis());
        }};

        public String timestamp() {

            return String.valueOf(System.currentTimeMillis() / 1000l);
        }

        public String nonce() {
            return String.valueOf(System.currentTimeMillis() + RDM.nextLong());
        }
    }

    public Token getConsumerToken() {
        return consumerToken;
    }

    public Token getAccessToken() {
        return accessToken;
    }

    public boolean isWriteToHeaders() {
        return writeToHeaders;
    }
}

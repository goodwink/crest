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

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

public class OAuthAccessToken {
    public static void main(String[] args) throws IOException {
        //Scribe d;

        //https://api.login.yahoo.com/oauth/v2/request_auth
        OAuthService service = new ServiceBuilder()
                                  .provider(YahooApi.class)
                                  .apiKey("dj0yJmk9SlpWZFpjdk5jaXpxJmQ9WVdrOVVqSXpSM0V5TjJVbWNHbzlNVE01TnpVMk9ERTJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD1kZg--")
                                  .apiSecret("415e66ee3b00686b424ac39f823445364bd5e595")
                                  .build();

//        org.scribe.model.Token tok = service.getRequestToken();
//        System.out.println("RequestToken="+tok);
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("goto https://api.login.yahoo.com/oauth/v2/request_auth?oauth_token=" + tok.getToken());
//        System.out.println("Input verifier :");
//        org.scribe.model.Token accessToken = service.getAccessToken(tok, new Verifier(br.readLine()));
        org.scribe.model.Token accessToken = new org.scribe.model.Token(
                "A=tzcovznqhTFz0MxJfH2hDsqzlRGgMGuyDPtOBmhP7Or4Eqpw_XjzwBa7pD0BRSWzfdNrGY1MQczq.rmSHPKWx7a7xT4keoepJLLIwZFWGcgVMUAveivkY7OWqPSH3k10FYl62ufCjr2a6K5J0artxh5ZJ9AIeNH0CbH_dNFh9PEwa7n6L3PFSwfHJJAgW2WorU.2zhIEaZB8aPMHgBp0wzyCz2F5l0c8LSlQSA3eQ0Fw2CrZRwQIuWh_rAyk.3xTrBB8GCYAQHHOVg9qBh0LVbFU59SBbQj4eTuAyXifUcrH8nAfh1G1JvokHE9_Aomhat5yoRJcpMpzZM02SQVurW8fXFzrZyp4ZZSMqlsk_6nnA0LPzbDg6kKQ_JBVaN9bO7gAN53q0nxq.JsEJA6NlnZsPJkFepFQhW.r.r3niv3ugGBtndwQjvwaqjXQnY.Il_0.5Fitn42BKLmdpoujDau5fObGWgtu_0CDdWGFA4uzSQq.IT07pVZW379d.WNphyPCF7lhwCNDrCJc4MYKMVZvUUNE6M93saFur5TSHTfTPim.LzfGWSI_VT.hOC1FBbkFjrkjJITEPuv_SKXpWCcsYfeOM9AXsW.Vsn49qPztsb5hWBWs2xdybKpuEjk7qm6YS_20_z2M8OemSKsE8dGCsV4gvI9aNTSBP2k7Nz2qGTncraUYuY80s4W3KbYsgRQN69wUZuAHOfPcpkFA.1eT7JQfAjOBDk2JOUkWR0GBGDOfSKI5.9nFNiqX.pXB.zaF0b9wEqMWmYJ5UBI-",
                "2485b678d7d71b41d17a01c82005e4f211630442"
        );
//        System.out.println("AccessToken=" + accessToken);

        OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.del.icio.us/v2/posts/add?url=sdfdf&description=" + URLEncoder.encode("Code Gist", "utf-8"));
        service.signRequest(accessToken, request);
        System.out.println("request="+request);
        Response response = request.send();
        System.out.println(response.getBody());

    }

}

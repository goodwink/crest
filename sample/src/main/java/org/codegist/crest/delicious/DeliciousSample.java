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

package org.codegist.crest.delicious;

import org.codegist.common.log.Logger;
import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.delicious.model.Posts;
import org.codegist.crest.delicious.model.Range;
import org.codegist.crest.delicious.service.Delicious;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DeliciousSample implements Runnable {

    private static final Logger LOG = Logger.getLogger(DeliciousSample.class);
    private final String consumerKey;
    private final String consumerSecret;
    private final String accessToken;
    private final String accessTokenSecret;
    private final String sessionHandle;

    public DeliciousSample(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String sessionHandle) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.sessionHandle = sessionHandle;
    }

    public void run() {
        CRest crest = new CRestBuilder()
                .useHttpClientRestService()
                .consumesXml().handledByJaxb()
                .setListSerializerSeparator(" ")
                .setBooleanSerializer("yes", "no")
                .usePreauthentifiedOAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret)
                .setProperty(CRestProperty.OAUTH_ACCESS_TOKEN_REFRESH_URL, "https://api.login.yahoo.com/oauth/v2/get_token")
                .setProperty(CRestProperty.OAUTH_ACCESS_TOKEN_EXTRAS, new HashMap<String, String>() {{
                    put("oauth_session_handle", sessionHandle);
                }})
                .build();

        /* Build service instance */
        Delicious delicious = crest.build(Delicious.class);

        /* Use it! */
        boolean postDone = delicious.addPost("http://crest.codegist.org?rdm=" + new Date().getTime(), "that's my site");
        Posts posts = delicious.getAllPosts("opensource", new Range(1, 15), new Date(), new Date(), false);
        boolean done = delicious.renameTag("os", "opensource");

        LOG.info("Add Post=" + postDone);
        LOG.info("Posts=" + posts);
        LOG.info("Rename done=" + done);
    }

    public static void main(String[] args) throws IOException {
        new DeliciousSample(args[0], args[1], args[2], args[3], args[4]).run();
    }

}

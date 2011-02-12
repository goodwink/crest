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

package org.codegist.crest.twitter;

import org.codegist.common.log.Logger;
import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.twitter.model.Message;
import org.codegist.crest.twitter.model.Status;
import org.codegist.crest.twitter.model.User;
import org.codegist.crest.twitter.service.DirectMessageService;
import org.codegist.crest.twitter.service.StatusService;
import org.codegist.crest.twitter.service.UserService;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class TwitterSample implements Runnable {

    private static final Logger LOG = Logger.getLogger(TwitterSample.class);

    private final String consumerKey;
    private final String consumerSecret;
    private final String accessToken;
    private final String accessTokenSecret;

    public TwitterSample(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

    public void run() {
        /* Get the factory */
        CRest crest = new CRestBuilder()
                .expectsJson().handledByJackson()
                .usePreauthentifiedOAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret)
                .build();

        /* Build services instances */
        StatusService statusService = crest.build(StatusService.class);
        UserService userService = crest.build(UserService.class);
        DirectMessageService directMessageService = crest.build(DirectMessageService.class);

        /* Use them! */
        Status status = statusService.updateStatus("My status update at " + new Date().getTime());
        User[] users = userService.search("username");
        Message[] messages = directMessageService.getReceived(10, 1);

        LOG.info("status=" + status);
        LOG.info("users=" + Arrays.toString(users));
        LOG.info("messages=" + Arrays.toString(messages));
    }

    public static void main(String[] args) {
        new TwitterSample(args[0], args[1], args[2], args[3]).run();
    }
}

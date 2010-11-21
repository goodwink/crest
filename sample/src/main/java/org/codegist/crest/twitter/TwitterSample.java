package org.codegist.crest.twitter;

import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.twitter.model.Message;
import org.codegist.crest.twitter.model.Status;
import org.codegist.crest.twitter.model.User;
import org.codegist.crest.twitter.service.DirectMessageService;
import org.codegist.crest.twitter.service.StatusService;
import org.codegist.crest.twitter.service.UserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class TwitterSample {

    public static void main(String[] args) throws IOException {
        String consumerKey = args[0];
        String consumerSecret = args[1];
        String accessToken = args[2];
        String accessTokenSecret = args[3];

        /* Get the factory */
        CRest crest = new CRestBuilder()
                .expectsJson()
                .addGlobalRequestHeader("Accept-Encoding", "gzip")
                .usePreauthentifiedOAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret)
                .build();

        /* Build services instances */
        StatusService statusService = crest.build(StatusService.class);
        UserService userService = crest.build(UserService.class);
        DirectMessageService directMessageService = crest.build(DirectMessageService.class);

        /* Use them! */
        Status status = statusService.updateStatus("My status update at " + new Date());
        User[] users = userService.search("username");
        Message[] messages = directMessageService.getReceived(10, 1);

        System.out.println("status=" + status);
        System.out.println("users=" + Arrays.toString(users));
        System.out.println("messages=" + Arrays.toString(messages));
    }
}

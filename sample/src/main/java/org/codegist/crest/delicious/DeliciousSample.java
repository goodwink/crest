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

import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.delicious.interceptor.YahooOAuthInterceptor;
import org.codegist.crest.delicious.model.DeliciousModelFactory;
import org.codegist.crest.delicious.model.Range;
import org.codegist.crest.delicious.service.Delicious;

import java.io.IOException;

public class DeliciousSample {
    public static void main(String[] args) throws IOException {
        String consumerKey = args[0];
        String consumerSecret = args[1];
        String accessToken = args[2];
        String accessTokenSecret = args[3];
        String sessionHandle = args[4];

        /* Get the factory */
        CRest crest = new CRestBuilder()
                .expectsXml(DeliciousModelFactory.class)
                .setProperty(CRestProperty.OAUTH_CONSUMER_KEY, consumerKey)
                .setProperty(CRestProperty.OAUTH_CONSUMER_SECRET, consumerSecret)
                .setProperty(CRestProperty.OAUTH_ACCESS_TOKEN, accessToken)
                .setProperty(CRestProperty.OAUTH_ACCESS_TOKEN_SECRET, accessTokenSecret)
                .setProperty(CRestProperty.SERIALIZER_LIST_SEPARATOR, " ")
                .setProperty(CRestProperty.SERIALIZER_BOOLEAN_TRUE, "yes")
                .setProperty(CRestProperty.SERIALIZER_BOOLEAN_FALSE, "no")
                .setProperty(YahooOAuthInterceptor.YUI_OAUTH_SESSION_HANDLE, sessionHandle)
                .build();

        Delicious delicious = crest.build(Delicious.class);
        System.out.println("getRecents=" + delicious.getRecentsPosts());
        System.out.println("getLastUpdate=" + delicious.getLastUpdatePosts());
//        System.out.println("addPost1=" + delicious.addPost("http://www.codegist.com", "CodeGist site"));
//        System.out.println("addPost2=" + delicious.addPost("http://crest.codegist.com", "CodeGist CRest site", "extended text", new String[]{"opensource", "coding", "java", "codegist"}, new Date(), true, false));
//        System.out.println("addPost2=" + delicious.addPost("http://common.codegist.com", "CodeGist CRest site", "extended text", new String[]{"opensource", "coding", "java", "codegist"}, new Date(), true, false));
        System.out.println("getPosts=" + delicious.getPosts());
        System.out.println("getPosts2=" + delicious.getPosts(null, null, new String[]{"opensource", "coding"}, null, false));
        System.out.println("getRecentsPosts=" + delicious.getRecentsPosts());
        System.out.println("getRecentsPosts2=" + delicious.getRecentsPosts("coding", 2));
        System.out.println("getPostsPerDate=" + delicious.getPostsPerDate());
        System.out.println("getPostsPerDate2=" + delicious.getPostsPerDate("coding"));
        System.out.println("getAllPosts=" + delicious.getAllPosts());
        System.out.println("getAllPosts2=" + delicious.getAllPosts("codegist", new Range(5, 6), null, null, null));
        System.out.println("getAllPostHashes=" + delicious.getAllPostHashes());
        System.out.println("getSuggestedPosts=" + delicious.getSuggestedPosts("http://www.yahoo.com"));
//        System.out.println("getTags=" + delicious.getTags());
//        System.out.println("deleteTag=" + delicious.deleteTag("java"));
//        System.out.println("renameTag=" + delicious.renameTag("os", "opensource"));
//        System.out.println("setTagBundle=" + delicious.setTagBundle("coding", "java", "javascript", "opensource"));
//        System.out.println("getTagBundle=" + delicious.getTagBundle("coding"));
//        System.out.println("getTagBundles=" + delicious.getTagBundles());
//        System.out.println("deleteTagBundle=" + delicious.deleteTagBundle("coding2"));
    }

}

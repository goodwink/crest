/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.twitter;

import org.codegist.crest.CRestBuilder;

/**
 * @author laurent.gilles@codegist.org
 */
public class CRestBuilderFactory {

    public static final String ACC_1 = "account1";
    public static final String ACC_2 = "account2";

    public static CRestBuilder getBaseCRestBuilder(String account){
        final String consumerKey = System.getProperty("crest.sample.twitter." + account + ".consumer-key");
        final String consumerSecret = System.getProperty("crest.sample.twitter." + account + ".consumer-secret");
        final String accessToken = System.getProperty("crest.sample.twitter." + account + ".access-token");
        final String accessTokenSecret = System.getProperty("crest.sample.twitter." + account + ".access-token-secret");
        return new CRestBuilder()
                .usePreauthentifiedOAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

    public static long getAccountUserId(String account){
        return Long.valueOf(System.getProperty("crest.sample.twitter." + account + ".user-id"));
    }

    public static String getAccountScreenName(String account){
        return System.getProperty("crest.sample.twitter." + account + ".screen-name");
    }
}

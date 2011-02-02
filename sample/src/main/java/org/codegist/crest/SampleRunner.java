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

package org.codegist.crest;

import org.codegist.common.log.Logger;
import org.codegist.crest.delicious.DeliciousSample;
import org.codegist.crest.flickr.FlickrSample;
import org.codegist.crest.google.GoogleServicesSample;
import org.codegist.crest.twitter.TwitterSample;


/**
 * @author laurent.gilles@codegist.org
 */
public class SampleRunner {

    private static final Logger LOG = Logger.getLogger(SampleRunner.class);

    /**
     * expected args:
     * <p>[0] delicious.consumerKey
     * <p>[1] delicious.consumerSecret
     * <p>[2] delicious.accessToken
     * <p>[3] delicious.accessTokenSecret
     * <p>[4] delicious.sessionHandle
     * <p>[5] twitter.consumerKey
     * <p>[6] twitter.consumerSecret
     * <p>[7] twitter.accessToken
     * <p>[8] twitter.accessTokenSecret
     * <p>[9] flickr.apiKey
     * <p>[10] flickr.appSecret
     * <p>[11] flickr.authToken
     *
     * @param args args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int i = 0;
        Runnable[] samples = {
                new DeliciousSample(args[i++], args[i++], args[i++], args[i++], args[i++]),
                new TwitterSample(args[i++], args[i++], args[i++], args[i++]),
                new FlickrSample(args[i++], args[i++], args[i++]),
                new GoogleServicesSample()
        };
        for (Runnable sample : samples) {
            LOG.info("Running " + sample.getClass().getSimpleName());
            try {
                sample.run();
            } catch (Throwable e) {
                LOG.error(e);
            }
        }
    }
}

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
package org.codegist.crest.delicious;

import org.codegist.common.lang.Randoms;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.CRestProperty;
import org.codegist.crest.delicious.model.*;
import org.codegist.crest.delicious.service.Delicious;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author laurent.gilles@codegist.org
 */
@Ignore
public abstract class AbstractDeliciousIntegrationTest {

    private final Delicious delicious;

    public AbstractDeliciousIntegrationTest(Delicious delicious){
        this.delicious = delicious;
    }


    @Test
    public void testTags() throws InterruptedException {
        String[] tagValues = {Randoms.randomAlphaNumeric(5), Randoms.randomAlphaNumeric(5)};
        assertTrue(delicious.addPost(getRandomUrl(), "that' my site2", "extended!", tagValues, null, false, false));

        Thread.sleep(10000); // let it propagate...
        Tags tags = delicious.getTags();
        assertTrue(tags.getTags().length == 2);
        List<String> tagList = Arrays.asList(tagValues);
        for (Tag t : tags) {
            assertTrue(tagList.contains(t.getTag()));
        }

        assertTrue(delicious.renameTag(tagValues[0], tagValues[0] = tagValues[0] + "-renamed"));

        tags = delicious.getTags();
        assertTrue(tags.getTags().length == 2);
        for (Tag t : tags) {
            assertTrue(tagList.contains(t.getTag()));
        }
    }

    @Test
    public void testTagBundles() throws InterruptedException {
        String name1 = Randoms.randomAlphaNumeric(5);
        String[] tagValues1 = {Randoms.randomAlphaNumeric(5), Randoms.randomAlphaNumeric(5)};
        String name2 = Randoms.randomAlphaNumeric(5);
        String[] tagValues2 = {Randoms.randomAlphaNumeric(5), Randoms.randomAlphaNumeric(5)};
        assertTrue(delicious.setTagBundle(name1, tagValues1));
        assertTrue(delicious.setTagBundle(name2, tagValues2));

        Bundles bundle = delicious.getTagBundle(name1);
        assertEquals(1, bundle.getBundles().length);
        assertEquals(name1, bundle.getBundles()[0].getName());
        List<String> tagList = Arrays.asList(tagValues1);
        for (String t : bundle.getBundles()[0].getTags()) {
            assertTrue(tagList.contains(t));
        }

        Bundles bundles = delicious.getTagBundles();
        assertEquals(2, bundles.getBundles().length);
    }


    @Test
    public void testPosts() throws InterruptedException {
        long stamp = 1256734686000l;
        String url = getRandomUrl();
        String[] tags = {Randoms.randomAlphaNumeric(5), Randoms.randomAlphaNumeric(5)};

        // add test
        assertTrue(delicious.addPost(url, "that' my site"));

        // get test
        Post post = getPostByUrl(delicious.getPosts(), url);
        assertNotNull(post);
        assertEquals("that' my site", post.getDescription());

        // add test 2
        url = getRandomUrl();
        assertTrue(delicious.addPost(url, "that' my site2", "extended!", tags, new Date(stamp), false, false));
        post = getPostByUrl(delicious.getPosts(), url);
        assertNotNull(post);
        assertEquals("that' my site2", post.getDescription());
        assertEquals("extended!", post.getExtended());
        assertFalse(post.isShared());
        assertArrayEquals(tags, post.getTags());

        // add test 3 same fail
        assertFalse(delicious.addPost(url, "that' my site2", "extended!", tags, new Date(stamp), false, false));

        // add test 4 override
        assertTrue(delicious.addPost(url, "that' my site2", "extended2!", tags, new Date(stamp), true, true));
        post = getPostByUrl(delicious.getPosts(), url);
        assertNotNull(post);
        assertEquals("that' my site2", post.getDescription());
        assertEquals("extended2!", post.getExtended());
        assertTrue(post.isShared());
        assertArrayEquals(tags, post.getTags());

        // get test 2
        Posts posts = delicious.getPosts(url, new Date(stamp), tags, new String[]{post.getHash()}, false);
        assertEquals(1, posts.getPosts().length);
        assertEquals(post, posts.getPosts()[0]);

        // get post per date test 1
        Dates dates = delicious.getPostsPerDate();
        assertTrue(dates.getDates().length > 0);
        assertTrue(dates.getDates()[0].getCount() > 0);

        // get post per date test 2
        dates = delicious.getPostsPerDate(tags[0]);
        assertEquals(1, dates.getDates()[0].getCount());

        // get recents test 1
        Thread.sleep(10000); // sleep to let it propagate...
        Posts recents = delicious.getRecentsPosts();
        assertTrue(recents.getPosts().length > 0);
        assertEquals(post, recents.getPosts()[0]);

        // get recents test 2
        recents = delicious.getRecentsPosts(tags[1], 2);
        assertEquals(1, recents.getPosts().length);
        assertEquals(post, recents.getPosts()[0]);

        // last update test
        Update update = delicious.getLastUpdatePosts();
        assertTrue(post.getTime().before(update.getTime()) || post.getTime().equals(update.getTime()));

        // all posts test 1
        Posts allPosts = delicious.getAllPosts();
        assertTrue(allPosts.getPosts().length > 0);
        assertEquals(post.getHash(), allPosts.getPosts()[0].getHash());

        // add another post with +/- same tags
        String[] tags2 = new String[]{tags[0], Randoms.randomAlphaNumeric(5)};
        assertTrue(delicious.addPost(getRandomUrl(), "that' my site2", "extended!", tags2, new Date(stamp), false, false));

        // all posts test 2
        allPosts = delicious.getAllPosts(tags[0], new Range(0, 10), new Date(stamp), new Date(), false);
        assertEquals(2, allPosts.getPosts().length);
        allPosts = delicious.getAllPosts(tags[0], new Range(0, 1), new Date(stamp), new Date(), false);
        assertEquals(1, allPosts.getPosts().length);
        allPosts = delicious.getAllPosts(tags2[1], new Range(0, 10), new Date(stamp), new Date(), false);
        assertEquals(1, allPosts.getPosts().length);

        // hashes test
        Posts hashes = delicious.getAllPostHashes();
        assertTrue(hashes.getPosts().length > 0);
        assertTrue(hashes.getPosts()[0].getMeta() != null);

        // suggest test
        Suggest suggest = delicious.getSuggestedPosts(url);
        assertNotNull(suggest);

    }

    @Before @After
    public void clean() {
        // delete all test data (and test delete as well)


        // delete posts
        Posts allPosts = delicious.getAllPosts();
        if (allPosts != null && allPosts.getPosts() != null) {
            for (Post p : allPosts.getPosts()) {
                assertTrue(delicious.deletePost(p.getHref()));
            }
        }
        allPosts = delicious.getAllPosts();
        assertTrue(allPosts.getPosts() == null || allPosts.getPosts().length == 0);

        // delete tags
        Tags tags = delicious.getTags();
        if (tags != null && tags.getTags() != null) {
            for (Tag tag : tags) {
                assertTrue(delicious.deleteTag(tag.getTag()));
            }
        }
        tags = delicious.getTags();
        assertTrue(tags.getTags() == null || tags.getTags().length == 0);

        // delete bundles
        Bundles bundles = delicious.getTagBundles();
        if (bundles != null && bundles.getBundles() != null) {
            for (Bundle b : bundles) {
                delicious.deleteTagBundle(b.getName());
            }
        }
        bundles = delicious.getTagBundles();
        assertTrue(bundles.getBundles() == null || bundles.getBundles().length == 0);
    }


    private static String getRandomUrl() {
        return getRandomUrl(Randoms.randomAlphaNumeric(5));
    }

    private static String getRandomUrl(String random) {
        return "http://crest.codegist.org/?rdm=" + random;
    }

    private static Post getPostByUrl(Posts posts, String url) {
        for (Post p : posts.getPosts()) {
            if (p.getHref().equals(url)) {
                return p;
            }
        }
        return null;
    }

    protected static CRestBuilder getBaseCRestBuilder() {
        final String consumerKey = System.getProperty("crest.sample.delicious.consumer-key");
        final String consumerSecret = System.getProperty("crest.sample.delicious.consumer-secret");
        final String accessToken = System.getProperty("crest.sample.delicious.access-token");
        final String accessTokenSecret = System.getProperty("crest.sample.delicious.access-token-secret");
        final String sessionHandle = System.getProperty("crest.sample.delicious.session-handle");

        return new CRestBuilder()
                .expectsXml(DeliciousModelFactory.class)
                .setListSerializerSeparator(" ")
                .setBooleanSerializer("yes", "no")
                .usePreauthentifiedOAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret)
                .setProperty(CRestProperty.OAUTH_ACCESS_TOKEN_REFRESH_URL, "https://api.login.yahoo.com/oauth/v2/get_token")
                .setProperty(CRestProperty.OAUTH_ACCESS_TOKEN_EXTRAS, new HashMap<String, String>() {{
                    put("oauth_session_handle", sessionHandle);
                }});
    }

}

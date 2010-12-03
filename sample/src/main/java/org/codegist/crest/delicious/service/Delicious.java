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

package org.codegist.crest.delicious.service;

import org.codegist.crest.annotate.*;
import org.codegist.crest.delicious.handler.DeliciousResponseHandler;
import org.codegist.crest.delicious.model.*;
import org.codegist.crest.oauth.handler.AccessTokenExpiredRetryHandler;
import org.codegist.crest.oauth.interceptor.OAuthInterceptor;

import java.util.Date;

@EndPoint("http://api.del.icio.us/v2")
@RetryHandler(AccessTokenExpiredRetryHandler.class)
@GlobalInterceptor(OAuthInterceptor.class)
@ResponseHandler(DeliciousResponseHandler.class)
public interface Delicious {

    @Path("/posts/recent")
    Posts getRecentsPosts();

    @Path("/posts/recent?tag={0}&count={1}")
    Posts getRecentsPosts(String tag, int count);

    @Path("/posts/dates")
    Dates getPostsPerDate();

    @Path("/posts/dates?tag={0}")
    Dates getPostsPerDate(String tag);

    @Path("/posts/update")
    Update getLastUpdatePosts();

    @Path("/posts/add?url={0}&description={1}")
    boolean addPost(String url, String description);

    @Path("/posts/add?url={0}&description={1}&extended={2}&tags={3}&stamp={4}&replace={5}&shared={6}")
    boolean addPost(String url, String description, String extended, String[] tags, Date stamp, boolean replace, boolean shared);

    @Path("/posts/get")
    Posts getPosts();

    @Path("/posts/get?url={0}&dt={1}&tag={2}&hashes={3}&meta={4}")
    Posts getPosts(String url, Date date, String[] tags, String[] hashes, Boolean meta);

    @Path("/posts/all")
    Posts getAllPosts();

    @Path("/posts/all?tag={0}&fromdt={2}&todt={3}&meta={4}")
    Posts getAllPosts(String tag, Range resultRange, Date from, Date to, Boolean meta);

    @Path("/posts/all?hashes")
    Posts getAllPostHashes();

    @Path("/posts/suggest?url={0}")
    Suggest getSuggestedPosts(String url);

    @Path("/tags/get")
    Tags getTags();

    @Path("/tags/delete?tag={0}")
    boolean deleteTag(String tag);

    @Path("/tags/rename?old={0}&new={1}")
    boolean renameTag(String oldTag, String newTag);

    @Path("/tags/bundles/all")
    Bundles getTagBundles();

    @Path("/tags/bundles/all?bundle={0}")
    Bundles getTagBundle(String name);

    @Path("/tags/bundles/set?bundle={0}&tags={1}")
    boolean setTagBundle(String name, String... tags);

    @Path("/tags/bundles/delete?bundle={0}")
    boolean deleteTagBundle(String name);

}

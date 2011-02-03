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
import org.codegist.crest.security.handler.RefreshAuthentificationRetryHandler;

import java.util.Date;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://api.del.icio.us/v2")
@RetryHandler(RefreshAuthentificationRetryHandler.class)
@ResponseHandler(DeliciousResponseHandler.class)
public interface Delicious {

    @Path("/posts/delete")
    boolean deletePost(@QueryParam("url") String url);

    @Path("/posts/recent")
    Posts getRecentsPosts();

    @Path("/posts/recent")
    Posts getRecentsPosts(
            @QueryParam("tag") String tag,
            @QueryParam("count") int count);

    @Path("/posts/dates")
    Dates getPostsPerDate();

    @Path("/posts/dates")
    Dates getPostsPerDate(@QueryParam("tag") String tag);

    @Path("/posts/update")
    Update getLastUpdatePosts();

    @Path("/posts/add")
    boolean addPost(
            @QueryParam("url") String url,
            @QueryParam("description") String description);

    @Path("/posts/add")
    boolean addPost(
            @QueryParam("url") String url,
            @QueryParam("description") String description,
            @QueryParam("extended") String extended,
            @QueryParam("tags") String[] tags,
            @QueryParam("stamp") Date stamp,
            @QueryParam("replace") boolean replace,
            @QueryParam("shared") boolean shared);

    @Path("/posts/get")
    Posts getPosts();

    @Path("/posts/get")
    Posts getPosts(
            @QueryParam("url") String url,
            @QueryParam("dt") Date date,
            @QueryParam("tag") String[] tags,
            @QueryParam("hashes") String[] hashes,
            @QueryParam("meta") Boolean meta);

    @Path("/posts/all")
    Posts getAllPosts();

    @Path("/posts/all")
    Posts getAllPosts(
            @QueryParam("tag") String tag,
            @QueryParam("range") Range resultRange,
            @QueryParam("fromdt") Date from,
            @QueryParam("todt") Date to,
            @QueryParam("meta") Boolean meta);

    @QueryParam("hashes")
    @Path("/posts/all")
    Posts getAllPostHashes();

    @Path("/posts/suggest")
    Suggest getSuggestedPosts(
            @QueryParam("url") String url);

    @Path("/tags/get")
    Tags getTags();

    @Path("/tags/delete")
    boolean deleteTag(
            @QueryParam("tag") String tag);

    @Path("/tags/rename")
    boolean renameTag(
            @QueryParam("old") String oldTag,
            @QueryParam("new") String newTag);

    @Path("/tags/bundles/all")
    Bundles getTagBundles();

    @Path("/tags/bundles/all")
    Bundles getTagBundle(@QueryParam("bundle") String name);

    @Path("/tags/bundles/set")
    boolean setTagBundle(
            @QueryParam("bundle") String name,
            @QueryParam("tags") String... tags);

    @Path("/tags/bundles/delete")
    boolean deleteTagBundle(@QueryParam("bundle") String name);

}

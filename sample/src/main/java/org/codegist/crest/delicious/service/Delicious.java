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

    @Path("/posts/recent")
    Posts getRecentsPosts();

    @Path("/posts/recent")
    Posts getRecentsPosts(
            @QueryParam(name = "tag") String tag,
            @QueryParam(name = "count") int count);

    @Path("/posts/dates")
    Dates getPostsPerDate();

    @Path("/posts/dates")
    Dates getPostsPerDate(@QueryParam(name = "tag") String tag);

    @Path("/posts/update")
    Update getLastUpdatePosts();

    @Path("/posts/add")
    boolean addPost(
            @QueryParam(name = "url") String url,
            @QueryParam(name = "description") String description);

    @Path("/posts/add")
    boolean addPost(
            @QueryParam(name = "url") String url,
            @QueryParam(name = "description") String description,
            @QueryParam(name = "extended") String extended,
            @QueryParam(name = "tags") String[] tags,
            @QueryParam(name = "stamp") Date stamp,
            @QueryParam(name = "replace") boolean replace,
            @QueryParam(name = "shared") boolean shared);

    @Path("/posts/get")
    Posts getPosts();

    @Path("/posts/get")
    Posts getPosts(
            @QueryParam(name = "url") String url,
            @QueryParam(name = "dt") Date date,
            @QueryParam(name = "tag") String[] tags,
            @QueryParam(name = "hashes") String[] hashes,
            @QueryParam(name = "meta") Boolean meta);

    @Path("/posts/all")
    Posts getAllPosts();

    @Path("/posts/all")
    Posts getAllPosts(
            @QueryParam(name = "tag") String tag,
            @QueryParam(name = "range") Range resultRange,// name doesn't really matter here as it get injected. todo something about ?
            @QueryParam(name = "fromdt") Date from,
            @QueryParam(name = "todt") Date to,
            @QueryParam(name = "meta") Boolean meta);

    @QueryParam(name = "hashes")
    @Path("/posts/all")
    Posts getAllPostHashes();

    @Path("/posts/suggest")
    Suggest getSuggestedPosts(
            @QueryParam(name = "url") String url);

    @Path("/tags/get")
    Tags getTags();

    @Path("/tags/delete")
    boolean deleteTag(
            @QueryParam(name = "tag") String tag);

    @Path("/tags/rename")
    boolean renameTag(
            @QueryParam(name = "old") String oldTag,
            @QueryParam(name = "new") String newTag);

    @Path("/tags/bundles/all")
    Bundles getTagBundles();

    @Path("/tags/bundles/all")
    Bundles getTagBundle(@QueryParam(name = "bundle") String name);

    @Path("/tags/bundles/set")
    boolean setTagBundle(
            @QueryParam(name = "bundle") String name,
            @QueryParam(name = "tags") String... tags);

    @Path("/tags/bundles/delete")
    boolean deleteTagBundle(@QueryParam(name = "bundle") String name);

}

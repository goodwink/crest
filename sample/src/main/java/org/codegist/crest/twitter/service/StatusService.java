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

package org.codegist.crest.twitter.service;

import org.codegist.crest.annotate.*;
import org.codegist.crest.twitter.model.Cursor;
import org.codegist.crest.twitter.model.Status;
import org.codegist.crest.twitter.model.User;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://api.twitter.com")
@Path("/1/statuses")
@HeaderParam(value = "Accept-Encoding", defaultValue = "gzip")
public interface StatusService {

    @POST
    @Path("/update.json")
    Status updateStatus(@FormParam("status") String status);

    @POST
    @Path("/update.json")
    Status updateStatus(
            @FormParam("status") String status,
            @QueryParam("lat") float lat,
            @QueryParam("long") float longitude);

    @POST
    @Path("/destroy/{id}.json")
    Status removeStatus(@PathParam("id") long id);

    @POST
    @Path("/retweet/{id}.json")
    Status retweetStatus(@PathParam("id") long id);

    @Path("/show/{id}.json")
    Status getStatus(@PathParam("id") long id);

    @Path("/retweets/{id}.json")
    Status[] getRetweets(@PathParam("id") long id);

    @Path("/retweets/{id}.json")
    Status[] getRetweets(
            @PathParam("id") long id,
            @QueryParam("count") long count);


    @Path("/{id}/retweeted_by.json")
    User[] getRetweetedBy(@PathParam("id") long id);

    @Path("/{id}/retweeted_by.json")
    User[] getRetweetedBy(
            @PathParam("id") long id,
            @QueryParam("count") long count,
            @QueryParam("page") long page);


    @Path("/{id}/retweeted_by/ids.json")
    long[] getRetweetedByIds(@PathParam("id") long id);

    @Path("/{id}/retweeted_by/ids.json")
    long[] getRetweetedByIds(
            @PathParam("id") long id,
            @QueryParam("count") long count,
            @QueryParam("page") long page);


    @Path("/friends.json")
    User[] getFriends(
            @QueryParam("user_id") long userId,
            @QueryParam("screen_name") String screenName);

    @Path("/friends.json")
    User[] getFriends(@QueryParam("user_id") long userId);

    @Path("/friends.json")
    User[] getFriends(@QueryParam("screen_name") String screenName);

    @Path("/friends.json")
    Cursor.User getFriends(
            @QueryParam("user_id") long userId,
            @QueryParam("screen_name") String screenName,
            @QueryParam("cursor") long cursor);

    @Path("/friends.json")
    Cursor.User getFriends(
            @QueryParam("user_id") long userId,
            @QueryParam("cursor") long cursor);

    @Path("/friends.json")
    Cursor.User getFriends(
            @QueryParam("screen_name") String screenName,
            @QueryParam("cursor") long cursor);

    @Path("/followers.json")
    User[] getFollowers(
            @QueryParam("user_id") long userId,
            @QueryParam("screen_name") String screenName);

    @Path("/followers.json")
    User[] getFollowers(@QueryParam("user_id") long userId);

    @Path("/followers.json")
    User[] getFollowers(@QueryParam("screen_name") String screenName);

    @Path("/followers.json")
    Cursor.User getFollowers(
            @QueryParam("user_id") long userId,
            @QueryParam("screen_name") String screenName,
            @QueryParam("cursor") long cursor);

    @Path("/followers.json")
    Cursor.User getFollowers(
            @QueryParam("user_id") long userId,
            @QueryParam("cursor") long cursor);

    @Path("/followers.json")
    Cursor.User getFollowers(
            @QueryParam("screen_name") String screenName,
            @QueryParam("cursor") long cursor);

}

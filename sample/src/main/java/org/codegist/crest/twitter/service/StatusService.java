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

import static org.codegist.crest.HttpMethod.*;
import static org.codegist.crest.config.Destination.*;

import org.codegist.crest.twitter.model.Cursor;
import org.codegist.crest.twitter.model.Status;
import org.codegist.crest.twitter.model.User;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://api.twitter.com")
@ContextPath("/1/statuses")
public interface StatusService {

    @HttpMethod(POST) 
    @Path("/update.json") 
    @Destination(BODY)
    Status updateStatus(@Name("status") String status);

    @HttpMethod(POST) @Path("/update.json?lat={1}&long={2}")
    Status changeStatus(
            @Name("status") @Destination(BODY) String status,
            float lat, float longitude);

    @HttpMethod(POST) @Path("/destroy/{0}.json")
    Status removeStatus(long id);

    @HttpMethod(POST) @Path("/retweet/{0}.json")
    Status retweetStatus(long id);

    @Path("/show/{0}.json")
    Status getStatus(long id);

    @Path("/retweets/{0}.json")
    Status[] getRetweets(long id);

    @Path("/retweets/{0}.json?count={1}")
    Status[] getRetweets(long id, long count);


    @Path("/{0}/retweeted_by.json")
    User[] getRetweetedBy(long id);

    @Path("/{0}/retweeted_by.json?count={1}&page={2}")
    User[] getRetweetedBy(long id, long count, long page);


    @Path("/{0}/retweeted_by/ids.json")
    long[] getRetweetedByIds(long id);

    @Path("/{0}/retweeted_by/ids.json?count={1}&page={2}")
    long[] getRetweetedByIds(long id, long count, long page);


    @Path("/friends.json?user_id={0}&screen_name={1}")
    User[] getFriends(long userId, String screenName);

    @Path("/friends.json?user_id={0}")
    User[] getFriends(long userId);

    @Path("/friends.json?screen_name={0}")
    User[] getFriends(String screenName);

    @Path("/friends.json?user_id={0}&screen_name={1}&cursor={2}")
    Cursor.User getFriends(long userId, String screenName, long cursor);

    @Path("/friends.json?user_id={0}&cursor={1}")
    Cursor.User getFriends(long userId, long cursor);

    @Path("/friends.json?screen_name={0}&cursor={1}")
    Cursor.User getFriends(String screenName, long cursor);

    @Path("/followers.json?user_id={0}&screen_name={1}")
    User[] getFollowers(long userId, String screenName);

    @Path("/followers.json?user_id={0}")
    User[] getFollowers(long userId);

    @Path("/followers.json?screen_name={0}")
    User[] getFollowers(String screenName);

    @Path("/followers.json?user_id={0}&screen_name={1}&cursor={2}")
    Cursor.User getFollowers(long userId, String screenName, long cursor);

    @Path("/followers.json?user_id={0}&cursor={1}")
    Cursor.User getFollowers(long userId, long cursor);

    @Path("/followers.json?screen_name={0}&cursor={1}")
    Cursor.User getFollowers(String screenName, long cursor);

}

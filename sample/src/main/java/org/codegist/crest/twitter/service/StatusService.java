package org.codegist.crest.twitter.service;

import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.annotate.RestParam;
import org.codegist.crest.twitter.model.Cursor;
import org.codegist.crest.twitter.model.Status;
import org.codegist.crest.twitter.model.User;

@RestApi(endPoint = "http://api.twitter.com", path = "/1/statuses")
public interface StatusService {

    @RestMethod(method = "POST", path = "/update.json", paramsDestination = "BODY")
    Status updateStatus(@RestParam(name = "status") String status);

    @RestMethod(method = "POST", path = "/update.json?lat={1}&long={2}")
    Status changeStatus(
            @RestParam(name = "status", destination = "BODY") String status,
            float lat, float longitude);

    @RestMethod(method = "POST", path = "/destroy/{0}.json")
    Status removeStatus(long id);

    @RestMethod(method = "POST", path = "/retweet/{0}.json")
    Status retweetStatus(long id);

    @RestMethod(path = "/show/{0}.json")
    Status getStatus(long id);

    @RestMethod(path = "/retweets/{0}.json")
    Status[] getRetweets(long id);

    @RestMethod(path = "/retweets/{0}.json?count={1}")
    Status[] getRetweets(long id, long count);


    @RestMethod(path = "/{0}/retweeted_by.json")
    User[] getRetweetedBy(long id);

    @RestMethod(path = "/{0}/retweeted_by.json?count={1}&page={2}")
    User[] getRetweetedBy(long id, long count, long page);


    @RestMethod(path = "/{0}/retweeted_by/ids.json")
    long[] getRetweetedByIds(long id);

    @RestMethod(path = "/{0}/retweeted_by/ids.json?count={1}&page={2}")
    long[] getRetweetedByIds(long id, long count, long page);


    @RestMethod(path = "/friends.json?user_id={0}&screen_name={1}")
    User[] getFriends(long userId, String screenName);

    @RestMethod(path = "/friends.json?user_id={0}")
    User[] getFriends(long userId);

    @RestMethod(path = "/friends.json?screen_name={0}")
    User[] getFriends(String screenName);

    @RestMethod(path = "/friends.json?user_id={0}&screen_name={1}&cursor={2}")
    Cursor.User getFriends(long userId, String screenName, long cursor);

    @RestMethod(path = "/friends.json?user_id={0}&cursor={1}")
    Cursor.User getFriends(long userId, long cursor);

    @RestMethod(path = "/friends.json?screen_name={0}&cursor={1}")
    Cursor.User getFriends(String screenName, long cursor);

    @RestMethod(path = "/followers.json?user_id={0}&screen_name={1}")
    User[] getFollowers(long userId, String screenName);

    @RestMethod(path = "/followers.json?user_id={0}")
    User[] getFollowers(long userId);

    @RestMethod(path = "/followers.json?screen_name={0}")
    User[] getFollowers(String screenName);

    @RestMethod(path = "/followers.json?user_id={0}&screen_name={1}&cursor={2}")
    Cursor.User getFollowers(long userId, String screenName, long cursor);

    @RestMethod(path = "/followers.json?user_id={0}&cursor={1}")
    Cursor.User getFollowers(long userId, long cursor);

    @RestMethod(path = "/followers.json?screen_name={0}&cursor={1}")
    Cursor.User getFollowers(String screenName, long cursor);

}

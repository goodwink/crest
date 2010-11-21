package org.codegist.crest.twitter.service;

import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.twitter.model.User;

@RestApi(endPoint = "http://api.twitter.com", path = "/1/users")
public interface UserService {

    @RestMethod(path = "/search.json?q={0}")
    User[] search(String search);

    @RestMethod(path = "/search.json?q={0}&per_page={1}&page={2}")
    User[] search(String search, long count, long page);

    @RestMethod(path = "/show.json?user_id={0}")
    User get(long id);

    @RestMethod(path = "/show.json?screen_name={0}")
    User get(String screenName);

    @RestMethod(path = "/lookup.json?user_id={0}&screen_name={1}")
    User[] lookup(long id, String... screenName);

    @RestMethod(path = "/lookup.json?screen_name={0}")
    User[] lookup(String[] screenName);
}

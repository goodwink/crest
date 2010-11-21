package org.codegist.crest.twitter.service;

import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.twitter.model.Message;

@RestApi(endPoint = "http://api.twitter.com", path = "/1/direct_messages")
public interface DirectMessageService {

    @RestMethod(path = ".json?since_id={0}&max_id={1}&count={2}&page={3}")
    Message[] getReceived(long sinceId, long maxId, long count, long page);

    @RestMethod(path = ".json?count={0}&page={1}")
    Message[] getReceived(long count, long page);

    @RestMethod(path = "/sent.json?since_id={0}&max_id={1}&count={2}&page={3}")
    Message[] getSent(long sinceId, long maxId, long count, long page);

    @RestMethod(path = "/sent.json?count={0}&page={1}")
    Message[] getSent(long count, long page);

    @RestMethod(method = "POST", path = "/new.json?user_id={0}&text={1}")
    Message send(long userId, String msg);

    @RestMethod(method = "POST", path = "/new.json?user_id={0}&screen_name={1}&text={2}")
    Message send(long userId, String screenName, String msg);

    @RestMethod(method = "DELETE", path = "/destroy/{0}.json")
    Message destroy(long msgId);

}

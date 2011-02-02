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
import org.codegist.crest.twitter.model.Message;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://api.twitter.com")
@ContextPath("/1/direct_messages")
@HeaderParam(name = "Accept-Encoding", value = "gzip")
public interface DirectMessageService {

    @Path(".json")
    Message[] getReceived(
            @QueryParam(name = "since_id") long sinceId,
            @QueryParam(name = "max_id") long maxId,
            @QueryParam(name = "count") long count,
            @QueryParam(name = "page") long page);

    @Path(".json")
    Message[] getReceived(
            @QueryParam(name = "count") long count,
            @QueryParam(name = "page") long page);

    @Path("/sent.json")
    Message[] getSent(
            @QueryParam(name = "since_id") long sinceId,
            @QueryParam(name = "max_id") long maxId,
            @QueryParam(name = "count") long count,
            @QueryParam(name = "page") long page);

    @Path("/sent.json")
    Message[] getSent(
            @QueryParam(name = "count") long count,
            @QueryParam(name = "page") long page);

    @POST
    @Path("/new.json")
    Message send(
            @QueryParam(name = "user_id") long userId,
            @QueryParam(name = "text") String msg);

    @POST
    @Path("/new.json")
    Message send(
            @QueryParam(name = "user_id") long userId,
            @QueryParam(name = "screen_name") String screenName,
            @QueryParam(name = "text") String msg);

    @DELETE
    @Path("/destroy/{msgid}.json")
    Message destroy(@PathParam(name = "msgid") long msgId);

}

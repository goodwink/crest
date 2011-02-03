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
import org.codegist.crest.twitter.model.User;


/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://api.twitter.com")
@ContextPath("/1/users")
@HeaderParam(value = "Accept-Encoding", defaultValue = "gzip")
public interface UserService {

    @Path("/search.json")
    User[] search(
            @QueryParam("q") String search);

    @Path("/search.json")
    User[] search(
            @QueryParam("q") String search,
            @QueryParam("per_page") long count,
            @QueryParam("page") long page);

    @Path("/show.json")
    User get(@QueryParam("user_id") long id);

    @Path("/show.json")
    User get(@QueryParam("screen_name") String screenName);

    @Path("/lookup.json")
    User[] lookup(
            @QueryParam("user_id") long id,
            @QueryParam("screen_name") String... screenName);

    @Path("/lookup.json")
    User[] lookup(@QueryParam("screen_name") String[] screenName);
}

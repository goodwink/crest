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

import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.twitter.model.User;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
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

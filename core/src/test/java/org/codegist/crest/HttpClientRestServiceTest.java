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

package org.codegist.crest;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpClientRestServiceTest extends RestServiceContractTest {

    @Override
    public RestService getRestService() {
        HttpParams p = new BasicHttpParams();
//        ConnRouteParams.setDefaultProxy(p, new HttpHost("127.0.0.1", 8888));
        DefaultHttpClient client = new DefaultHttpClient();
        return new HttpClientRestService(client);
    }
}

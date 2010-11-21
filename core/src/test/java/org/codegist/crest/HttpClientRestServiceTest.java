package org.codegist.crest;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class HttpClientRestServiceTest extends RestServiceContractTest {

    @Override
    public RestService getRestService() {
        HttpParams p = new BasicHttpParams();
//        ConnRouteParams.setDefaultProxy(p, new HttpHost("127.0.0.1", 8888));
        DefaultHttpClient client = new DefaultHttpClient();
        return new HttpClientRestService(client);
    }
}

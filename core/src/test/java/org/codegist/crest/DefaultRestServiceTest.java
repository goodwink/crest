package org.codegist.crest;

public class DefaultRestServiceTest extends RestServiceContractTest {
    @Override
    public RestService getRestService() {
        return new DefaultRestService();
    }
}

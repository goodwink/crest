package org.codegist.crest.google.service;

import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.google.domain.Address;
import org.codegist.crest.google.domain.SearchResult;
import org.codegist.crest.google.handler.GoogleResponseHandler;

@RestApi(endPoint = "http://ajax.googleapis.com", path = "/ajax/services/search", methodsResponseHandler = GoogleResponseHandler.class)
public interface SearchService {

    @RestMethod(path = "/web?v=1.0&q={0}")
    SearchResult<Address> search(String text);

}


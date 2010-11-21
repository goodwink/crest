package org.codegist.crest;

public interface RestService {

    /**
     * Execute the given request.
     *
     * @param request Request to fire
     * @return The response.
     * @throws HttpException For any problem occuring during the http transaction, and when response status code is not HTTP OK 200
     */
    HttpResponse exec(HttpRequest request) throws HttpException;

}

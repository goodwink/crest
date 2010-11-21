package org.codegist.crest;

public class HttpException extends RuntimeException {

    private final HttpResponse response;

    public HttpException(String message, HttpResponse response) {
        super(message);
        this.response = response;
    }

    public HttpException(Throwable cause, HttpResponse response) {
        super(cause);
        this.response = response;
    }

    public HttpException(String message, HttpResponse response, Throwable cause) {
        super(message, cause);
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }
}

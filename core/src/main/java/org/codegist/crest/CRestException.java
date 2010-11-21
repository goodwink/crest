package org.codegist.crest;

public class CRestException extends RuntimeException {
    public CRestException() {
        super();
    }

    public CRestException(String message) {
        super(message);
    }

    public CRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public CRestException(Throwable cause) {
        super(cause);
    }

    static CRestException transform(Exception e) {
        if (e instanceof CRestException) {
            return (CRestException) e;
        } else {
            return new CRestException(e);
        }
    }
}

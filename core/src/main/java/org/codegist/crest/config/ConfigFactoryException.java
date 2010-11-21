package org.codegist.crest.config;

public class ConfigFactoryException extends Exception {
    public ConfigFactoryException() {
        super();
    }

    public ConfigFactoryException(String message) {
        super(message);
    }

    public ConfigFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigFactoryException(Throwable cause) {
        super(cause);
    }
}

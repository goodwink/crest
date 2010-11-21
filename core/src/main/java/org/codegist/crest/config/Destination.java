package org.codegist.crest.config;

/**
 * Param destination.
 */
public enum Destination {
    /**
     * Use it to configure a parameter to be part of the http request queryString
     */
    URL,
    /**
     * Use it to configure a parameter to be part of the http request Body
     */
    BODY
}

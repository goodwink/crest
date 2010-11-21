package org.codegist.crest.flickr.model;

public interface SimplePayload<T> extends Payload {
    T getValue();
}

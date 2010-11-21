package org.codegist.crest.twitter.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

public abstract class Cursor<T> {

    public static final long START = -1;

    @JsonProperty("next_cursor")
    private long nextCursor;
    @JsonProperty("previous_cursor")
    private long previousCursor;
    protected T payload;

    // this is a messy hack, to get a generic Cursor
    // since Twitter cursor payload property name varies with data types... 
    public static class User extends Cursor<org.codegist.crest.twitter.model.User[]> {

        @JsonProperty("users")
        public void setPayload(org.codegist.crest.twitter.model.User[] payload) {
            this.payload = payload;
        }
    }

    public long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(long nextCursor) {
        this.nextCursor = nextCursor;
    }

    public long getPreviousCursor() {
        return previousCursor;
    }

    public void setPreviousCursor(long previousCursor) {
        this.previousCursor = previousCursor;
    }

    public T getPayload() {
        return payload;
    }

    public abstract void setPayload(T payload);

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}


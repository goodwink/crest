/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.twitter.model;

import org.codegist.common.lang.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
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
        return new ToStringBuilder(this)
                .append("nextCursor",nextCursor)
                .append("previousCursor",previousCursor)
                .append("payload",payload)
                .toString();
    }
}


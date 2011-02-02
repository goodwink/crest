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

package org.codegist.crest.google.domain;

import org.codegist.common.lang.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class SearchResult<T> {

    final Cursor cursor;
    final T[] results;

    @JsonCreator
    public SearchResult(
            @JsonProperty("cursor") Cursor cursor,
            @JsonProperty("results") T[] results) {
        this.cursor = cursor;
        this.results = results;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public T[] getResults() {
        return results;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("cursor",cursor)
                .append("results",results)
                .toString();
    }

    public static class Cursor {
        final Page[] pages;
        final long estimatedResultCount;
        final long currentPageIndex;
        final String moreResultsUrl;

        @JsonCreator
        public Cursor(
                @JsonProperty("pages") Page[] pages,
                @JsonProperty("estimatedResultCount") long estimatedResultCount,
                @JsonProperty("currentPageIndex") long currentPageIndex,
                @JsonProperty("moreResultsUrl") String moreResultsUrl) {
            this.pages = pages;
            this.estimatedResultCount = estimatedResultCount;
            this.currentPageIndex = currentPageIndex;
            this.moreResultsUrl = moreResultsUrl;
        }

        public Page[] getPages() {
            return pages;
        }

        public long getEstimatedResultCount() {
            return estimatedResultCount;
        }

        public long getCurrentPageIndex() {
            return currentPageIndex;
        }

        public String getMoreResultsUrl() {
            return moreResultsUrl;
        }

        public String toString() {
            return new ToStringBuilder(this)
                .append("pages",pages)
                .append("estimatedResultCount",estimatedResultCount)
                .append("currentPageIndex",currentPageIndex)
                .append("moreResultsUrl",moreResultsUrl)
                .toString();
        }

        public static class Page {
            final long start;
            final long label;

            @JsonCreator
            public Page(
                    @JsonProperty("label") long label,
                    @JsonProperty("start") long start) {
                this.label = label;
                this.start = start;
            }

            public long getStart() {
                return start;
            }

            public long getLabel() {
                return label;
            }

            public String toString() {
                return new ToStringBuilder(this)
                .append("start",start)
                .append("label",label)
                .toString();
            }
        }
    }
}

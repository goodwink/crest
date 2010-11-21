package org.codegist.crest.google.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

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
        return ToStringBuilder.reflectionToString(this);
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
            return ToStringBuilder.reflectionToString(this);
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
                return ToStringBuilder.reflectionToString(this);
            }
        }
    }
}

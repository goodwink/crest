package org.codegist.crest.google.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Address {
    final String unescapedUrl;
    final String url;
    final String visibleUrl;
    final String cacheUrl;
    final String title;
    final String titleNoFormatting;
    final String content;

    @JsonCreator
    public Address(
            @JsonProperty("unescapedUrl") String unescapedUrl,
            @JsonProperty("url") String url,
            @JsonProperty("visibleUrl") String visibleUrl,
            @JsonProperty("cacheUrl") String cacheUrl,
            @JsonProperty("title") String title,
            @JsonProperty("titleNoFormatting") String titleNoFormatting,
            @JsonProperty("content") String content) {
        this.unescapedUrl = unescapedUrl;
        this.url = url;
        this.visibleUrl = visibleUrl;
        this.cacheUrl = cacheUrl;
        this.title = title;
        this.titleNoFormatting = titleNoFormatting;
        this.content = content;
    }

    public String getUnescapedUrl() {
        return unescapedUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getVisibleUrl() {
        return visibleUrl;
    }

    public String getCacheUrl() {
        return cacheUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleNoFormatting() {
        return titleNoFormatting;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

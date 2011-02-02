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
        return new ToStringBuilder(this)
                .append("unescapedUrl",unescapedUrl)
                .append("url",url)
                .append("visibleUrl",visibleUrl)
                .append("cacheUrl",cacheUrl)
                .append("title",title)
                .append("titleNoFormatting",titleNoFormatting)
                .append("content",content)
                .toString();
    }
}

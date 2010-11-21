package org.codegist.crest.google.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Translation {
    private final String text;

    @JsonCreator
    public Translation(@JsonProperty("translatedText") String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

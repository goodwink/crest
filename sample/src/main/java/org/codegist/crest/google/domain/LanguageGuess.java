package org.codegist.crest.google.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class LanguageGuess {
    private final String language;
    private final boolean isReliable;
    private final float confidence;

    @JsonCreator
    public LanguageGuess(
            @JsonProperty("language") String language,
            @JsonProperty("isReliable") boolean reliable,
            @JsonProperty("confidence") float confidence) {
        this.language = language;
        isReliable = reliable;
        this.confidence = confidence;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isReliable() {
        return isReliable;
    }

    public float getConfidence() {
        return confidence;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

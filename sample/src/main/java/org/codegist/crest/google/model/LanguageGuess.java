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

package org.codegist.crest.google.model;

import org.codegist.common.lang.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
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
        return new ToStringBuilder(this)
                .append("language", language)
                .append("isReliable", isReliable)
                .append("confidence", confidence)
                .toString();
    }
}

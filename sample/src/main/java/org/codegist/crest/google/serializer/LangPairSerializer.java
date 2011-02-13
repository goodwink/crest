/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.google.serializer;

import org.codegist.crest.google.model.LangPair;
import org.codegist.crest.serializer.Serializer;

/**
 * @author laurent.gilles@codegist.org
 */
public class LangPairSerializer implements Serializer<LangPair> {
    private static final String FORMAT = "%s|%s";

    public String serialize(LangPair value) {
        return String.format(FORMAT, value.getFrom(), value.getTo());
    }
}

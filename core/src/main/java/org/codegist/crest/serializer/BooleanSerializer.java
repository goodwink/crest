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

package org.codegist.crest.serializer;

import org.codegist.common.lang.Strings;
import org.codegist.crest.CRestProperty;

import java.util.Map;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class BooleanSerializer implements Serializer<Boolean> {

    public static final String DEFAULT_TRUE = "true";
    public static final String DEFAULT_FALSE = "false";
    private final String trueString;
    private final String falseString;

    public BooleanSerializer() {
        this(DEFAULT_TRUE, DEFAULT_FALSE);
    }
    public BooleanSerializer(Map<String,Object> customProperties) {
        this(
                Strings.defaultIfBlank((String) customProperties.get(CRestProperty.SERIALIZER_BOOLEAN_TRUE), DEFAULT_TRUE),
                Strings.defaultIfBlank((String) customProperties.get(CRestProperty.SERIALIZER_BOOLEAN_FALSE), DEFAULT_FALSE)
        );
    }
    public BooleanSerializer(String trueString, String falseString) {
        this.trueString = trueString;
        this.falseString = falseString;
    }

    @Override
    public String serialize(Boolean value) {
        if(value == null) return "";
        return Boolean.TRUE.equals(value) ? trueString : falseString;
    }
}

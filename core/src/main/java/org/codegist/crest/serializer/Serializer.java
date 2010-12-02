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

/**
 * Serializes are used to serialize a object to a single String.
 * <p> If implementor declares a constructor with a Map argument, it will be called with the user custom properties.
 *
 * @param <T> Optional parameter value type
 * @see org.codegist.crest.serializer.Serializers
 * @see org.codegist.crest.InterfaceContext#getProperties()
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface Serializer<T> {

    /**
     * Serialize the current arg context into a single string
     *
     * @param value argument value
     * @return serialized version of the argument
     */
    String serialize(T value);

}

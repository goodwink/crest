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

package org.codegist.crest.flickr.serializer;

import org.codegist.crest.ParamContext;
import org.codegist.crest.serializer.DefaultSerializer;
import org.codegist.crest.serializer.Serializer;

import java.util.Date;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class FlickParamSerializer implements Serializer {

    private final Serializer serializer = new DefaultSerializer(" ");

    public String serialize(ParamContext context) {
        Object o = context.getArgValue();
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return ((Boolean) o) ? "1" : "0";
        } else if (o instanceof Date) {
            return String.valueOf(((Date) o).getTime());
        } else {
            return serializer.serialize(context);
        }
    }
}

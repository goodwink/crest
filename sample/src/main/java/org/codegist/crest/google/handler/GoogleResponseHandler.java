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

package org.codegist.crest.google.handler;

import org.codegist.common.lang.Validate;
import org.codegist.common.reflect.Types;
import org.codegist.crest.CRestException;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.serializer.Deserializer;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class GoogleResponseHandler implements ResponseHandler {

    private final Deserializer deserializer;

    public GoogleResponseHandler(Map<String, Object> parameters) {
        this.deserializer = (Deserializer) parameters.get(Deserializer.class.getName());
        Validate.notNull(this.deserializer, "No deserializer set, please construct CRest using either JSON or XML expected return type.");
    }

    public final Object handle(ResponseContext context) {
        /* Marshall the response */
        Response<?> res = deserializer.deserialize(context.getResponse().asReader(), Types.newType(Response.class, context.getExpectedGenericType()));
        /* Check for google OK status */
        if (res.status == 200) {
            return res.data; /* Returns the nested payload */
        } else {
            throw new CRestException(res.details + " (status=" + res.status + ")"); /* Throw exception with google error details */
        }
    }

    static class Response<T> {
        final T data;
        final String details;
        final int status;

        @JsonCreator
        Response(
                @JsonProperty("responseData") T data,
                @JsonProperty("responseDetails") String details,
                @JsonProperty("responseStatus") int status) {
            this.data = data;
            this.details = details;
            this.status = status;
        }
    }
}

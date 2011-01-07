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

package org.codegist.crest.delicious.handler;

import org.codegist.common.lang.Strings;
import org.codegist.common.lang.Validate;
import org.codegist.common.marshal.Marshaller;
import org.codegist.crest.CRestException;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.delicious.model.Result;
import org.codegist.crest.handler.ResponseHandler;

import java.util.Map;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DeliciousResponseHandler implements ResponseHandler {

    private final Marshaller marshaller;

    public DeliciousResponseHandler(Map<String,Object> properties) {
        this.marshaller = (Marshaller) properties.get(Marshaller.class.getName());
        Validate.notNull(this.marshaller, "No marshaller set, please construct CRest using either JSON or XML expected return type.");
    }

    public Object handle(ResponseContext responseContext) throws CRestException {
        try {
            Object response = marshaller.marshall(responseContext.getResponse().asReader(), responseContext.getExpectedGenericType());
            if(response instanceof Result) {
                Result result = (Result) response;
                // Delicious Result response format is not consistent 
                boolean done =
                        "done".equalsIgnoreCase(result.getCode()) 
                        || "done".equalsIgnoreCase(result.getValue())
                        || "ok".equalsIgnoreCase(result.getValue());
                // If expected return type is boolean, then return either true/false
                if(boolean.class.equals(responseContext.getExpectedGenericType()) || Boolean.class.equals(responseContext.getExpectedGenericType())) {
                    return done;
                }else if(!done){
                    // If a response type other than boolean is expected and result is false, then throw an exception.
                    throw new CRestException(Strings.defaultIfBlank(result.getCode(), result.getValue()));
                }else{
                    // Shouldn't reach here. 
                    // Response type is an instance of Result only if an error happened or if the expected return type is either true/false
                    throw new IllegalStateException("Should not reach here");
                }
            }else{
                return response;
            }
        } finally {
            responseContext.getResponse().close();
        }
    }


    
}

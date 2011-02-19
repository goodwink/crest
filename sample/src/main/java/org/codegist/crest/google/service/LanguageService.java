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

package org.codegist.crest.google.service;

import org.codegist.crest.annotate.*;
import org.codegist.crest.google.handler.GoogleResponseHandler;
import org.codegist.crest.google.model.LangPair;
import org.codegist.crest.google.model.LanguageGuess;
import org.codegist.crest.google.model.Translation;
import org.codegist.crest.google.serializer.LangPairSerializer;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://ajax.googleapis.com")
@Path("/ajax/services/language")
@QueryParam(value = "v", defaultValue = "1.0")
@ResponseHandler(GoogleResponseHandler.class)
@Consumes("application/json")
public interface LanguageService {

    @Path("/detect")
    LanguageGuess detectLanguage(@QueryParam("q") String text);

    @Path("/translate")
    Translation translate(
            @QueryParam("q") String text,
            @QueryParam("langpair") @Serializer(LangPairSerializer.class) LangPair langPair);

}


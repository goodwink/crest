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

import org.codegist.crest.annotate.ContextPath;
import org.codegist.crest.annotate.EndPoint;
import org.codegist.crest.annotate.Path;
import org.codegist.crest.annotate.ResponseHandler;
import org.codegist.crest.google.domain.LanguageGuess;
import org.codegist.crest.google.domain.Translation;
import org.codegist.crest.google.handler.GoogleResponseHandler;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://ajax.googleapis.com")
@ContextPath("/ajax/services/language")
@ResponseHandler(GoogleResponseHandler.class)
public interface LanguageService {

    @Path("/detect?v=1.0&q={0}")
    LanguageGuess detectLanguage(String text);

    @Path("/translate?v=1.0&q={0}&langpair={1}%7C{2}")
    Translation translate(String text, String from, String to);

}


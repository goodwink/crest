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

package org.codegist.crest.google;

import org.codegist.common.log.Logger;
import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.google.model.*;
import org.codegist.crest.google.service.LanguageService;
import org.codegist.crest.google.service.SearchService;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class GoogleServicesSample implements Runnable {

    private static final Logger LOG = Logger.getLogger(GoogleServicesSample.class);

    public GoogleServicesSample() {
    }

    public void run() {
        /* Get the factory */
        CRest crest = new CRestBuilder()
                .consumesJson().handledByJackson()
                .build();

        /* Build services instances */
        SearchService searchService = crest.build(SearchService.class);
        LanguageService languageService = crest.build(LanguageService.class);

        /* Use them! */
        LanguageGuess searchLanguageGuess = languageService.detectLanguage("Guess it!");
        Translation searchTranslation = languageService.translate("Translate me if you can!", new LangPair("en", "fr"));
        SearchResult<Address> searchResult = searchService.search("this is a google search");

        LOG.info("search=" + searchResult);
        LOG.info("detectLanguage=" + searchLanguageGuess);
        LOG.info("translate=" + searchTranslation);
    }

    public static void main(String[] args) {
        new GoogleServicesSample().run();
    }


}
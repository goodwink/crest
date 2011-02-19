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

package org.codegist.crest.google;

import org.codegist.crest.CRestBuilder;
import org.codegist.crest.google.model.LangPair;
import org.codegist.crest.google.model.LanguageGuess;
import org.codegist.crest.google.model.Translation;
import org.codegist.crest.google.service.LanguageService;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author laurent.gilles@codegist.org
 */
@Ignore
public abstract class AbstractLanguageServiceIntegrationTest {

    private final LanguageService languageService;

    protected AbstractLanguageServiceIntegrationTest(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Test
    public void testDetect(){
        LanguageGuess guess = languageService.detectLanguage("salut ca va?");
        assertNotNull(guess);
        assertEquals("fr", guess.getLanguage());
    }

    @Test
    public void testTranslate(){
        Translation translation = languageService.translate("salut ca va?", new LangPair("fr", "en"));
        assertNotNull(translation);
        assertNotNull(translation.getText());
    }


    protected static CRestBuilder getBaseCRestBuilder(){
        return new CRestBuilder().consumesJson().handledByJackson();
    }


}

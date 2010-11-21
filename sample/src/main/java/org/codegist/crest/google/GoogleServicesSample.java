package org.codegist.crest.google;

import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.google.domain.Address;
import org.codegist.crest.google.domain.LanguageGuess;
import org.codegist.crest.google.domain.SearchResult;
import org.codegist.crest.google.domain.Translation;
import org.codegist.crest.google.service.LanguageService;
import org.codegist.crest.google.service.SearchService;

public class GoogleServicesSample {

    public static void main(String[] args) {
        /* Get the factory */
        CRest crest = new CRestBuilder().expectsJson().build();

        /* Build services instances */
        SearchService searchService = crest.build(SearchService.class);
        LanguageService languageService = crest.build(LanguageService.class);

        /* Use them! */
        SearchResult<Address> searchResult = searchService.search("this is a google search");
        LanguageGuess searchLanguageGuess = languageService.detectLanguage("Guess it!");
        Translation searchTranslation = languageService.translate("Translate me if you can!", "en", "fr");

        System.out.println("search=" + searchResult);
        System.out.println("detectLanguage=" + searchLanguageGuess);
        System.out.println("translate=" + searchTranslation);
    }


}
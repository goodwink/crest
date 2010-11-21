package org.codegist.crest.google.service;

import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.google.domain.LanguageGuess;
import org.codegist.crest.google.domain.Translation;
import org.codegist.crest.google.handler.GoogleResponseHandler;

@RestApi(endPoint = "http://ajax.googleapis.com", path = "/ajax/services/language", methodsResponseHandler = GoogleResponseHandler.class)
public interface LanguageService {

    @RestMethod(path = "/detect?v=1.0&q={0}")
    LanguageGuess detectLanguage(String text);

    @RestMethod(path = "/translate?v=1.0&q={0}&langpair={1}%7C{2}")
    Translation translate(String text, String from, String to);

}


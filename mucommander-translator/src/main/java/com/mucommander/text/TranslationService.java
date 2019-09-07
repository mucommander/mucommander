package com.mucommander.text;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public interface TranslationService {
    ResourceBundle getLanguagesBundle();
    ResourceBundle getDictionaryBundle();
    List<Locale> getAvailableLanguages();
}

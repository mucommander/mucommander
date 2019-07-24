/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.text;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class takes care of all text localization issues by loading all text entries from a dictionary file on startup
 * and translating them into the current language on demand.
 *
 * <p>All public methods are static to make it easy to call them throughout the application.</p>
 *
 * <p>See dictionary file for more information about th dictionary file format.</p>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class Translator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);

    /** List of all available languages in the dictionary file */
    private static List<Locale> availableLanguages;

    private static ResourceBundle dictionaryBundle;
    private static ResourceBundle languagesBundle;

    /**
     * Prevents instance creation.
     */
    private Translator() {
    }

    public static void init(ResourceBundle dictionaryBundle, ResourceBundle languagesBundle, List<Locale> availableLanguages) {
        Translator.dictionaryBundle = dictionaryBundle;
        Translator.languagesBundle = languagesBundle;
        Translator.availableLanguages = availableLanguages;
    }

    public static void test() {
        if (dictionaryBundle == null || languagesBundle == null || availableLanguages == null) {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns an array of available languages, expressed as language codes ("EN", "FR", "pt_BR"...).
     * The returned array is sorted by language codes in case insensitive order.
     *
     * @return an array of language codes.
     */
    public static List<Locale> getAvailableLanguages() {
        return availableLanguages;
    }

    /**
     * Returns <code>true</code> if the given entry's key has a value in the current language.
     * If the <code>useDefaultLanguage</code> parameter is <code>true</code>, entries that have no value in the
     * {@link #getLanguage() current language} but one in the {@link #DEFAULT_LANGUAGE} will be considered as having
     * a value (<code>true</code> will be returned).
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param useDefaultLanguage if <code>true</code>, entries that have no value in the {@link #getLanguage() current
     * language} but one in the {@link #DEFAULT_LANGUAGE} will be considered as having a value
     * @return <code>true</code> if the given key has a corresponding value in the current language.
     */
    public static boolean hasValue(String key, boolean useDefaultLanguage) {
        return dictionaryBundle.containsKey(key);
    }

    /**
     * Returns the localized text String for the given key expressed in the current language, or in the default language
     * if there is no value for the current language. Entry parameters (%1, %2, ...), if any, are replaced by the
     * specified values.
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param paramValues array of parameters which will be used as values for variables.
     * @return the localized text String for the given key expressed in the current language
     */
    public static String get(String key, String... paramValues) {
        if (dictionaryBundle.containsKey(key))
            return MessageFormat.format(dictionaryBundle.getString(key), (Object[]) paramValues);

        if (languagesBundle.containsKey(key))
            return languagesBundle.getString(key);

        return key;
    }
}

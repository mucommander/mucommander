/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;


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
    private static List<Locale> availableLanguages = new ArrayList<Locale>();

    /** Current language */
    private static Locale language;

    private static ResourceBundle bundle;
    
    /**
     * Prevents instance creation.
     */
    private Translator() {
    }

    static {
    	registerLocale(Locale.forLanguageTag("ar-SA"));
    	registerLocale(Locale.forLanguageTag("be-BY"));
    	registerLocale(Locale.forLanguageTag("ca-ES"));
    	registerLocale(Locale.forLanguageTag("cs-CZ"));
    	registerLocale(Locale.forLanguageTag("da-DA"));
    	registerLocale(Locale.forLanguageTag("de-DE"));
    	registerLocale(Locale.forLanguageTag("en-GB"));
    	registerLocale(Locale.forLanguageTag("en-US"));
    	registerLocale(Locale.forLanguageTag("es-ES"));
    	registerLocale(Locale.forLanguageTag("fr-FR"));
    	registerLocale(Locale.forLanguageTag("hu-HU"));
    	registerLocale(Locale.forLanguageTag("it-IT"));
    	registerLocale(Locale.forLanguageTag("ja-JP"));
    	registerLocale(Locale.forLanguageTag("ko-KR"));
    	registerLocale(Locale.forLanguageTag("no-NO"));
    	registerLocale(Locale.forLanguageTag("nl-NL"));
    	registerLocale(Locale.forLanguageTag("pl-PL"));
    	registerLocale(Locale.forLanguageTag("pt-BR"));
    	registerLocale(Locale.forLanguageTag("ro-RO"));
    	registerLocale(Locale.forLanguageTag("ru-RU"));
    	registerLocale(Locale.forLanguageTag("sk-SK"));
    	registerLocale(Locale.forLanguageTag("sl-SL"));
    	registerLocale(Locale.forLanguageTag("sv-SV"));
    	registerLocale(Locale.forLanguageTag("tr-TR"));
    	registerLocale(Locale.forLanguageTag("uk-UA"));
    	registerLocale(Locale.forLanguageTag("zh-CN"));
    	registerLocale(Locale.forLanguageTag("zh-TW"));
    }

    public static void registerLocale(Locale locale) {
    	availableLanguages.add(locale);
    }

    private static Locale getLocale() {
    	String localeNameFromConf = MuConfigurations.getPreferences().getVariable(MuPreference.LANGUAGE);
    	if (localeNameFromConf == null) {
    		// language is not set in preferences, use system's language
            // Try to match language with the system's language, only if the system's language
            // has values in dictionary, otherwise use default language (English).
            Locale defaultLocale = Locale.getDefault();
            LOGGER.info("Language not set in preferences, trying to match system's language ("+defaultLocale+")");
            return defaultLocale;
    	}

    	LOGGER.info("Using language set in preferences: "+localeNameFromConf);
    	switch (localeNameFromConf) {
    	// for backward compatibility
    	case "EN": return Locale.forLanguageTag("en-US");
    	case "en_GB": return Locale.forLanguageTag("en_GB");
    	case "FR": return Locale.forLanguageTag("fr-FR");
    	case "DE": return Locale.forLanguageTag("de-DE");
    	case "ES": return Locale.forLanguageTag("es-ES");
    	case "CS": return Locale.forLanguageTag("cs-CZ");
    	case "zh_CN": return Locale.forLanguageTag("zh-CN");
    	case "zh_TW": return Locale.forLanguageTag("zh-TW");
    	case "PL": return Locale.forLanguageTag("pl-PL");
    	case "HU": return Locale.forLanguageTag("hu-HU");
    	case "RU": return Locale.forLanguageTag("ru-RU");
    	case "SL": return Locale.forLanguageTag("sl-SL");
    	case "RO": return Locale.forLanguageTag("ro-RO");
    	case "IT": return Locale.forLanguageTag("it-IT");
    	case "KO": return Locale.forLanguageTag("ko-KR");
    	case "pt_BR": return Locale.forLanguageTag("pt-BR");
    	case "NL": return Locale.forLanguageTag("nl-NL");
    	case "SK": return Locale.forLanguageTag("sk-SK");
    	case "JA": return Locale.forLanguageTag("ja-JP");
    	case "SV": return Locale.forLanguageTag("sv-SV");
    	case "DA": return Locale.forLanguageTag("da-DA");
    	case "UA": return Locale.forLanguageTag("uk-UA");
    	case "AR": return Locale.forLanguageTag("ar-SA");
    	case "BE": return Locale.forLanguageTag("be-BY");
    	case "NB": return Locale.forLanguageTag("no-NO");
    	case "TR": return Locale.forLanguageTag("tr-TR");
    	case "CA": return Locale.forLanguageTag("ca-ES");
    	default: return Locale.forLanguageTag(localeNameFromConf);
    	}
    }

    public static void init() throws IOException {
    	Locale locale = getLocale();

        // Determines if language is one of the languages declared as available
        if(availableLanguages.contains(locale)) {
            // Language is available
        	bundle= ResourceBundle.getBundle("dictionary", locale);
            LOGGER.debug("Language "+locale+" is available.");
        }
        else {
            // Language is not available, fall back to default language
        	bundle= ResourceBundle.getBundle("dictionary");
            LOGGER.debug("Language "+locale+" is not available, falling back to English");
        }

        // Set preferred language in configuration file
        MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, locale.toLanguageTag());

        LOGGER.debug("Current language has been set to "+Translator.language);
    }

    /**
     * Returns the current language as a language code ("EN", "FR", "pt_BR", ...).
     *
     * @return lang a language code
     */
    public static String getLanguage() {
        return language.getLanguage();
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
    	return bundle.containsKey(key);
    }

    /**
     * Returns the localized text String for the given key expressd in the current language, or in the default language
     * if there is no value for the current language. Entry parameters (%1, %2, ...), if any, are replaced by the
     * specified values.
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param paramValues array of parameters which will be used as values for variables.
     * @return the localized text String for the given key expressd in the current language
     */
    public static String get(String key, String... paramValues) {
    	return bundle.containsKey(key) ? bundle.getString(key) : key;
    }
}

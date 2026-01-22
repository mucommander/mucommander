/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.translator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.util.LocaleUtils;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;

/**
 * JPMS service provider for translation services.
 * Initializes the translation system for JPMS-based application.
 */
public class TranslationServiceProvider implements TranslationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationServiceProvider.class);

    private static final List<String> LANGUAGE_TAGS = Arrays.asList(
            "ar","be","ca","cs","da","de","en","en-GB","es","fr", "it", "hu","ja","ko","nb","nl","pl","pt-BR","ro","ru","sk","sl","sv","tr","uk","zh-CN","zh-TW");

    private final ResourceBundle languagesBundle;
    private final ResourceBundle dictionaryBundle;
    private final List<Locale> availableLanguages;

    public TranslationServiceProvider() {
        // Initialize the translation system
        this.availableLanguages = LANGUAGE_TAGS.stream()
                .map(Locale::forLanguageTag)
                .collect(Collectors.toList());

        Locale locale = match(loadLocale(), availableLanguages);
        String languageTag = locale.toLanguageTag();
        LOGGER.debug("Current language has been set to " + languageTag);

        // Set preferred language in configuration file
        MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, languageTag);

        this.dictionaryBundle = getDictionaryBundle(locale);
        this.languagesBundle = getLanguageBundle(locale);

        // Initialize Translator with the service
        Translator.init(dictionaryBundle, languagesBundle, availableLanguages);

        LOGGER.info("Translation service initialized for locale: {}", languageTag);
    }

    @Override
    public ResourceBundle getLanguagesBundle() {
        return languagesBundle;
    }

    @Override
    public ResourceBundle getDictionaryBundle() {
        return dictionaryBundle;
    }

    @Override
    public List<Locale> getAvailableLanguages() {
        return availableLanguages;
    }

    private static Locale loadLocale() {
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
        return LocaleUtils.forLanguageTag(localeNameFromConf);
    }

    private static ResourceBundle getDictionaryBundle(Locale locale) {
        // In Java 9+ / JPMS, ResourceBundle.Control is not supported in modules
        // Properties files are UTF-8 by default, so we don't need the custom control
        // Use the class's ClassLoader directly to find resources within the module
        ClassLoader classLoader = TranslationServiceProvider.class.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dictionary", locale, classLoader);
        return new ResolveVariableResourceBundle(resourceBundle);
    }

    private static ResourceBundle getLanguageBundle(Locale locale) {
        // In Java 9+ / JPMS, properties files are UTF-8 by default
        // Use the class's ClassLoader directly to find resources within the module
        ClassLoader classLoader = TranslationServiceProvider.class.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return ResourceBundle.getBundle("languages", locale, classLoader);
    }

    private static Locale match(Locale loadedLocale, List<Locale> availableLanguages) {
        for (Locale locale : availableLanguages)
            if (locale.getLanguage().equals(loadedLocale.getLanguage())
                    && Objects.equals(locale.getCountry(), loadedLocale.getCountry())) {
                LOGGER.info("Found exact match (language+country) for locale {}", locale);
                return locale;
            }

        for (Locale locale : availableLanguages)
            if (locale.getLanguage().equals(loadedLocale.getLanguage())) {
                LOGGER.info("Found close match (language) for locale {}", loadedLocale);
                return locale;
            }

        LOGGER.info("Locale {} is not available, falling back to English", loadedLocale);
        return Locale.ENGLISH;
    }

    /**
     * Decorator allowing to resolve the values composed of variables.
     */
    private static class ResolveVariableResourceBundle extends ResourceBundle {

        /**
         * Pattern corresponding to a variable.
         */
        private static final Pattern VARIABLE = Pattern.compile("\\$\\[([^]]+)\\]");

        /**
         * The underlying resource bundle.
         */
        private final ResourceBundle resourceBundle;

        /**
         * The cache containing the resolved values in case the original value contains at least
         * one variable.
         */
        private final Map<String, String> cache;

        /**
         * Constructs a {@code ResolveVariableResourceBundle} with the specified underlying
         * {@link ResourceBundle}.
         * @param resourceBundle The underlying {@link ResourceBundle}.
         */
        ResolveVariableResourceBundle(final ResourceBundle resourceBundle) {
            this.resourceBundle = resourceBundle;
            this.cache = ResolveVariableResourceBundle.resolve(resourceBundle);
        }

        @Override
        protected Object handleGetObject(final String key) {
            final Object result = cache.get(key);
            if (result == null) {
                return resourceBundle.getObject(key);
            }
            return result;
        }

        @Override
        public Enumeration<String> getKeys() {
            return resourceBundle.getKeys();
        }

        /**
         * Resolves all the values composed of variables.
         * @param resourceBundle The {@code ResourceBundle} from which we extract the values to resolve.
         * @return A {@code Map} containing all the values that have been resolved
         */
        private static Map<String, String> resolve(final ResourceBundle resourceBundle) {
            final Map<String, String> result = new HashMap<String, String>();
            for (final Enumeration<String> enumeration = resourceBundle.getKeys(); enumeration.hasMoreElements(); ) {
                final String key = enumeration.nextElement();
                ResolveVariableResourceBundle.resolve(key, resourceBundle, result);
            }
            return Collections.unmodifiableMap(result);
        }

        /**
         * Resolves the value of the specified key if needed and stores the result in the specified map.
         * @param key The key to resolve.
         * @param resource The resource bundle from which we extract the value to resolve.
         * @param map The map in which we store the result.
         * @return The resolved value of the specified key.
         */
        private static Object resolve(final String key, final ResourceBundle resource, final Map<String, String> map) {
            Object result = resource.getObject(key);
            if (result instanceof String) {
                final String value = (String) result;
                final Matcher matcher = VARIABLE.matcher(value);
                int startIndex = 0;
                final StringBuilder buffer = new StringBuilder(64);
                while (matcher.find(startIndex)) {
                    buffer.append(value, startIndex, matcher.start());
                    try {
                        buffer.append(ResolveVariableResourceBundle.resolve(matcher.group(1), resource, map));
                    } catch (MissingResourceException e) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("The key '{}' is missing", key);
                        }
                        buffer.append(value, matcher.start(), matcher.end());
                    }
                    startIndex = matcher.end();
                }
                if (buffer.length() > 0) {
                    buffer.append(value.substring(startIndex));
                    result = buffer.toString();
                    map.put(key, (String) result);
                }
            }
            return result;
        }
    }
}

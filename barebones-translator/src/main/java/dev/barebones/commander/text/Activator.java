/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */
package dev.barebones.commander.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.barebones.commander.commons.util.LocaleUtils;
import dev.barebones.commander.conf.MuConfigurations;
import dev.barebones.commander.conf.MuPreference;

public final class Activator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final List<String> LANGUAGE_TAGS = Arrays.asList(
            "ar","be","ca","cs","da","de","en","en-GB","es","fr", "it", "hu","ja","ko","nb","nl","pl","pt-BR","ro","ru","sk","sl","sv","tr","uk","zh-CN","zh-TW");

    private static final Utf8ResourceBundleControl UTF8_CONTROL = new Utf8ResourceBundleControl();

    private Activator() {
    }

    public static void register() {
        List<Locale> availableLanguages = LANGUAGE_TAGS.stream().map(Locale::forLanguageTag).collect(Collectors.toList());
        Locale locale = match(loadLocale(), availableLanguages);
        String languageTag = locale.toLanguageTag();
        LOGGER.debug("Current language has been set to " + languageTag);
        MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, languageTag);

        ResourceBundle dictionaryBundle = getDictionaryBundle(locale);
        ResourceBundle languagesBundle = getLanguageBundle(locale);

        TranslationTracker.register(new TranslationService() {
            @Override public ResourceBundle getLanguagesBundle() { return languagesBundle; }
            @Override public ResourceBundle getDictionaryBundle() { return dictionaryBundle; }
            @Override public List<Locale> getAvailableLanguages() { return availableLanguages; }
        });
    }

    static Locale loadLocale() {
        String localeNameFromConf = MuConfigurations.getPreferences().getVariable(MuPreference.LANGUAGE);
        if (localeNameFromConf == null) {
            Locale defaultLocale = Locale.getDefault();
            LOGGER.info("Language not set in preferences, trying to match system's language (" + defaultLocale + ")");
            return defaultLocale;
        }
        LOGGER.info("Using language set in preferences: " + localeNameFromConf);
        return LocaleUtils.forLanguageTag(localeNameFromConf);
    }

    static ResourceBundle getDictionaryBundle(Locale locale) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("dictionary", locale, UTF8_CONTROL);
        return new ResolveVariableResourceBundle(resourceBundle);
    }

    static ResourceBundle getLanguageBundle(Locale locale) {
        return ResourceBundle.getBundle("languages", UTF8_CONTROL);
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

    private static final class Utf8ResourceBundleControl extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            URL resourceURL = loader.getResource(resourceName);
            if (resourceURL != null) {
                try {
                    return new PropertyResourceBundle(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    LOGGER.debug("Language " + locale + " failed to load, non english characters might be broken", e);
                }
            }
            return super.newBundle(baseName, locale, format, loader, reload);
        }
    }

    private static class ResolveVariableResourceBundle extends ResourceBundle {
        private static final Pattern VARIABLE = Pattern.compile("\\$\\[([^]]+)\\]");
        private final ResourceBundle resourceBundle;
        private final Map<String, String> cache;

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

        private static Map<String, String> resolve(final ResourceBundle resourceBundle) {
            final Map<String, String> result = new HashMap<>();
            for (final Enumeration<String> enumeration = resourceBundle.getKeys(); enumeration.hasMoreElements(); ) {
                final String key = enumeration.nextElement();
                ResolveVariableResourceBundle.resolve(key, resourceBundle, result);
            }
            return Collections.unmodifiableMap(result);
        }

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

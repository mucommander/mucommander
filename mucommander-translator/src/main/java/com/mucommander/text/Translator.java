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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
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
	private static List<Locale> availableLanguages = new ArrayList<>();

	private static ResourceBundle dictionaryBundle;
	private static ResourceBundle languagesBundle;

	private static List<String> languageTags = Arrays.asList(
	        "ar","be","ca","cs","da","de","en","en-GB","es","fr","hu","ja","ko","nb","nl","pl","pt-BR","ro","ru","sk","sl","sv","tr","uk","zh-CN","zh-TW");

	/**
	 * Prevents instance creation.
	 */
	private Translator() {
	}

	static {
		languageTags.forEach(tag -> availableLanguages.add(Locale.forLanguageTag(tag)));
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
		return Locale.forLanguageTag(localeNameFromConf.replace('_', '-'));
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
					LOGGER.debug("Language "+locale+" failed to load, non english characters might be broken",e);
				}
			}

			return super.newBundle(baseName, locale, format, loader, reload);
		}
	}

	private static Locale match(Locale loadedLocale) {
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

	public static void init() {
		// used in order to force static initialization
	}

	static {
		Locale locale = match(loadLocale());
		final Utf8ResourceBundleControl utf8ResourceBundleControl = new Utf8ResourceBundleControl();
		ResourceBundle resourceBundle= ResourceBundle.getBundle("dictionary", locale, utf8ResourceBundleControl);
		dictionaryBundle = new Translator.ResolveVariableResourceBundle(resourceBundle);

		String languageTag = locale.toLanguageTag();
		// Set preferred language in configuration file
		MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, languageTag);

		LOGGER.debug("Current language has been set to "+languageTag);

		languagesBundle = ResourceBundle.getBundle("languages", utf8ResourceBundleControl);
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

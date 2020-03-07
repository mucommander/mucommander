package com.mucommander.text;

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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;

public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static List<String> languageTags = Arrays.asList(
            "ar","be","ca","cs","da","de","en","en-GB","es","fr", "it", "hu","ja","ko","nb","nl","pl","pt-BR","ro","ru","sk","sl","sv","tr","uk","zh-CN","zh-TW");

    private static Utf8ResourceBundleControl utf8ResourceBundleControl = new Utf8ResourceBundleControl();
    private ServiceRegistration<TranslationService> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        List<Locale> availableLanguages = languageTags.stream().map(Locale::forLanguageTag).collect(Collectors.toList());
        Locale locale = match(loadLocale(), availableLanguages);
        String languageTag = locale.toLanguageTag();
        LOGGER.debug("Current language has been set to "+languageTag);
        // Set preferred language in configuration file
        MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, languageTag);

        ResourceBundle dictionaryBundle = getDictionaryBundle(locale);
        ResourceBundle languagesBundle = getLanguageBundle(locale);

        TranslationService translationService = new TranslationService() {
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
        };
        serviceRegistration = context.registerService(TranslationService.class, translationService, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
    }

    static Locale loadLocale() {
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

    static ResourceBundle getDictionaryBundle(Locale locale) {
        ResourceBundle resourceBundle= ResourceBundle.getBundle("dictionary", locale, utf8ResourceBundleControl);
        return new Activator.ResolveVariableResourceBundle(resourceBundle);
    }

    static ResourceBundle getLanguageBundle(Locale locale) {
        return ResourceBundle.getBundle("languages", utf8ResourceBundleControl);
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
                    LOGGER.debug("Language "+locale+" failed to load, non english characters might be broken",e);
                }
            }

            return super.newBundle(baseName, locale, format, loader, reload);
        }
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

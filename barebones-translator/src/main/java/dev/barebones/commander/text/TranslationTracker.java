/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package dev.barebones.commander.text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static holder for the {@link TranslationService}. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a plain holder.
 */
public final class TranslationTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationTracker.class);

    private static volatile TranslationService service;

    private TranslationTracker() {
    }

    public static void register(TranslationService translationService) {
        service = translationService;
        Translator.init(
                translationService.getDictionaryBundle(),
                translationService.getLanguagesBundle(),
                translationService.getAvailableLanguages());
        LOGGER.info("TranslationService is registered: " + translationService);
    }

    public static void unregister(TranslationService translationService) {
        if (service == translationService) {
            service = null;
        }
        LOGGER.info("TranslationService is unregistered: " + translationService);
    }

    public static TranslationService getTranslationService() {
        return service;
    }
}

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
package dev.barebones.commander.conf;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Activator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private Activator() {
    }

    public static void register(Map<String, String> properties) throws java.io.IOException {
        LOGGER.debug("starting");
        PlatformManager.setPreferencesFolder(properties.get("mucommander.preferences"));
        MuConfigurations.loadPreferences();
    }
}

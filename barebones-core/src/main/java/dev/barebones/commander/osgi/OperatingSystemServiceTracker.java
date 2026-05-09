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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dev.barebones.commander.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.barebones.commander.core.desktop.DesktopManager;

/**
 * Static registry for {@link OperatingSystemService}. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a plain registry.
 */
public final class OperatingSystemServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemServiceTracker.class);

    private OperatingSystemServiceTracker() {
    }

    public static void register(OperatingSystemService service) {
        service.getDesktopAdapters().forEach(DesktopManager::registerAdapter);
        LOGGER.info("OperatingSystemService is registered: " + service);
    }

    public static void unregister(OperatingSystemService service) {
        LOGGER.info("OperatingSystemService is unregistered: " + service);
    }
}

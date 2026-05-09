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
package dev.barebones.commander.commons.file.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.barebones.commander.commons.file.FileFactory;

/**
 * Static registration helper for archive-format providers. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a thin wrapper around
 * {@link FileFactory#registerArchiveFormat}.
 */
public final class FileFormatServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFormatServiceTracker.class);

    private FileFormatServiceTracker() {
    }

    public static void register(FileFormatService service) {
        FileFactory.registerArchiveFormat(service.getProvider());
        LOGGER.info("FileFormatService is registered: " + service);
    }

    public static void unregister(FileFormatService service) {
        FileFactory.unregisterArchiveFormat(service.getProvider());
        LOGGER.info("FileFormatService is unregistered: " + service);
    }
}

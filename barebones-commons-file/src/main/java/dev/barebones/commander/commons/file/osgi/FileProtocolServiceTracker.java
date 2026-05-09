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
import dev.barebones.commander.commons.file.FileURL;

/**
 * Static registration helper for protocol providers. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a thin wrapper around
 * {@link FileFactory#registerProtocol} and {@link FileURL#registerHandler}
 * that lets each protocol module register a {@link FileProtocolService}
 * directly from its bootstrap entry point.
 */
public final class FileProtocolServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileProtocolServiceTracker.class);

    private FileProtocolServiceTracker() {
    }

    public static void register(FileProtocolService service) {
        FileFactory.registerProtocol(service.getSchema(), service.getProtocolProvider());
        FileURL.registerHandler(service.getSchema(), service.getSchemeHandler());
        LOGGER.info("FileProtocolService is registered: " + service);
    }

    public static void unregister(FileProtocolService service) {
        FileFactory.unregisterProtocol(service.getSchema());
        FileURL.unregisterHandler(service.getSchema());
        LOGGER.info("FileProtocolService is unregistered: " + service);
    }
}

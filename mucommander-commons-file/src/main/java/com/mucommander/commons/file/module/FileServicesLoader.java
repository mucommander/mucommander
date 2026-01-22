/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.module;

import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.archive.ArchiveFormatProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Initializes the file system by discovering and registering protocol and format providers
 * using Java's ServiceLoader mechanism.
 *
 * @author Arik Hadas
 */
public class FileServicesLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileServicesLoader.class);

    private static boolean initialized = false;

    /**
     * Initializes all file protocols and formats by loading services via ServiceLoader.
     * This method is idempotent - calling it multiple times has no effect.
     */
    public static synchronized void load() {
        if (initialized) {
            LOGGER.debug("FileSystemInitializer already initialized, skipping");
            return;
        }

        LOGGER.info("Initializing file system protocols and formats...");

        // Load and register protocol services
        ServiceLoader<FileProtocolService> protocolLoader = ServiceLoader.load(FileProtocolService.class);
        for (FileProtocolService service : protocolLoader) {
            try {
                FileFactory.registerProtocol(service.getSchema(), service.getProtocolProvider());
                FileURL.registerHandler(service.getSchema(), service.getSchemeHandler());
                LOGGER.info("Registered protocol: {}", service.getSchema());
            } catch (Exception e) {
                LOGGER.error("Failed to register protocol service: " + service, e);
            }
        }

        // Load and register format services
        ServiceLoader<FileFormatService> formatLoader = ServiceLoader.load(FileFormatService.class);
        for (FileFormatService service : formatLoader) {
            try {
                ArchiveFormatProvider provider = service.getProvider();
                if (provider != null) {
                    FileFactory.registerArchiveFormat(provider);
                    LOGGER.info("Registered archive format: {} (extensions: {})",
                               provider.getClass().getSimpleName(),
                               provider.getExtensions());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to register format service: " + service, e);
            }
        }

        initialized = true;
        LOGGER.info("File system initialization complete");
    }

    /**
     * Returns whether the file system has been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Resets the initialization state (primarily for testing).
     */
    static synchronized void reset() {
        initialized = false;
    }
}

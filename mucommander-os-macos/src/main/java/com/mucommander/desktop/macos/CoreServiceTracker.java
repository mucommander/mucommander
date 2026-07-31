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

package com.mucommander.desktop.macos;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.os.api.CoreService;

/**
 * Service locator for CoreService using JPMS ServiceLoader.
 * <p>
 * Replaces OSGi ServiceTracker with ServiceLoader for JPMS migration.
 *
 * @author Arik Hadas
 */
public class CoreServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServiceTracker.class);

    private static CoreService service;

    /**
     * Initialize the CoreService using ServiceLoader.
     */
    public static void initialize() {
        ServiceLoader<CoreService> loader = ServiceLoader.load(CoreService.class);
        service = loader.findFirst().orElse(null);
        if (service != null) {
            LOGGER.info("CoreService is registered: " + service);
        } else {
            LOGGER.warn("No CoreService implementation found");
        }
    }

    public static CoreService getCoreService() {
        if (service == null) {
            initialize();
        }
        return service;
    }
}

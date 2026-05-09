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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static registry for {@link BrowsableItemsMenuService}. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a plain registry.
 */
public final class BrowsableItemsMenuServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsableItemsMenuServiceTracker.class);
    private static final List<BrowsableItemsMenuService> SERVICES = new ArrayList<>();

    private BrowsableItemsMenuServiceTracker() {
    }

    public static void register(BrowsableItemsMenuService service) {
        SERVICES.add(service);
        LOGGER.info("BrowsableItemsMenuService is registered: " + service);
    }

    public static void unregister(BrowsableItemsMenuService service) {
        SERVICES.remove(service);
        LOGGER.info("BrowsableItemsMenuService is unregistered: " + service);
    }

    public static List<BrowsableItemsMenuService> getMenuServices() {
        return new ArrayList<>(SERVICES);
    }
}

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
package com.mucommander.osgi;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracker of {@link BrowsableItemsMenuService}
 * @author Arik Hadas
 */
public class BrowsableItemsMenuServiceTracker extends ServiceTracker<BrowsableItemsMenuService, BrowsableItemsMenuService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsableItemsMenuService.class);
    private static List<BrowsableItemsMenuService> SERVICES = new ArrayList<>();

    public BrowsableItemsMenuServiceTracker(BundleContext context) {
        super(context, BrowsableItemsMenuService.class, null);
    }

    @Override
    public BrowsableItemsMenuService addingService(ServiceReference<BrowsableItemsMenuService> reference) {
        BrowsableItemsMenuService service = super.addingService(reference);
        SERVICES.add(service);
        LOGGER.info("BrowseableItemsMenuService is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<BrowsableItemsMenuService> reference, BrowsableItemsMenuService service) {
        super.removedService(reference, service);
        SERVICES.remove(service);
        LOGGER.info("BrowseableItemsMenuService is unregistered: " + service);
    }

    public static List<BrowsableItemsMenuService> getMenuServices() {
        return new ArrayList<>(SERVICES);
    }
}

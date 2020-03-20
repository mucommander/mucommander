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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.os.api.CoreService;

public class CoreServiceTracker extends ServiceTracker<CoreService, CoreService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServiceTracker.class);

    private static CoreService service;

    public CoreServiceTracker(BundleContext context) {
        super(context, CoreService.class, null);
    }

    @Override
    public CoreService addingService(ServiceReference<CoreService> reference) {
        CoreService service = super.addingService(reference);
        CoreServiceTracker.service = service;
        LOGGER.info("CoreService is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<CoreService> reference, CoreService service) {
        super.removedService(reference, service);
        LOGGER.info("CoreService is unregistered: " + service);
    }

    public static CoreService getCoreService() {
        return service;
    }
}

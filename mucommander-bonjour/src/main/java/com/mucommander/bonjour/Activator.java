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
package com.mucommander.bonjour;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mucommander.osgi.BrowsableItemsMenuService;

public class Activator implements BundleActivator {

    private ServiceRegistration<BrowsableItemsMenuService> menuRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        menuRegistration = context.registerService(BrowsableItemsMenuService.class, BonjourMenu::new, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        menuRegistration.unregister();
    }

}

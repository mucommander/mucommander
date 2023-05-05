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

package com.mucommander.commons.file.protocol.adb;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.DefaultSchemeHandler;
import com.mucommander.commons.file.DefaultSchemeParser;
import com.mucommander.commons.file.SchemeHandler;
import com.mucommander.commons.file.osgi.FileProtocolService;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.osgi.BrowsableItemsMenuService;

public class Activator implements BundleActivator {

    private ServiceRegistration<FileProtocolService> serviceRegistration;
    private ServiceRegistration<BrowsableItemsMenuService> menuRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        FileProtocolService service = new FileProtocolService() {
            @Override
            public String getSchema() {
                return "adb";
            }

            @Override
            public ProtocolProvider getProtocolProvider() {
                return new AdbProtocolProvider();
            }

            @Override
            public SchemeHandler getSchemeHandler() {
                return new DefaultSchemeHandler(new DefaultSchemeParser(), 21, "/", AuthenticationType.NO_AUTHENTICATION, new Credentials("anonymous", "anonymous_coward@mucommander.com"));
            }
        };
        serviceRegistration = context.registerService(FileProtocolService.class, service, null);
        menuRegistration = context.registerService(BrowsableItemsMenuService.class, AndroidMenu::new, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
        menuRegistration.unregister();
    }
}
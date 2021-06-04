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

package com.mucommander.desktop.windows;

import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mucommander.desktop.DesktopAdapter;
import com.mucommander.osgi.OperatingSystemService;

public class Activator implements BundleActivator  {

    private ServiceRegistration<OperatingSystemService> osRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        OperatingSystemService service = new OperatingSystemService() {
            @Override
            public List<DesktopAdapter> getDesktopAdapters() {
                return Collections.singletonList(new WindowsDesktopAdapter());
            }
        };
        osRegistration = context.registerService(OperatingSystemService.class, service, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        osRegistration.unregister();
    }
}

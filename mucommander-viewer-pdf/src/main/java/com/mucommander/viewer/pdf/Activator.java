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
package com.mucommander.viewer.pdf;

import com.mucommander.viewer.FileViewerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator for viewer of PDF files.
 *
 * @author Arik Hadas
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<FileViewerService> viewerRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        viewerRegistration = context.registerService(FileViewerService.class, new PdfFileViewer(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        viewerRegistration.unregister();
    }

}

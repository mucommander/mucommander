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
package com.mucommander.commons.file.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.FileFactory;

/**
 * @author Arik Hadas
 */
public class FileFormatServiceTracker extends ServiceTracker<FileFormatService, FileFormatService>{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileFormatServiceTracker.class);

    public FileFormatServiceTracker(BundleContext context) {
        super(context, FileFormatService.class,null);
    }

    @Override
    public FileFormatService addingService(ServiceReference<FileFormatService> reference) {
        FileFormatService service = super.addingService(reference);
        FileFactory.registerArchiveFormat(service.getProvider());
        LOGGER.info("FileFormatService is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<FileFormatService> reference, FileFormatService service) {
        FileFactory.unregisterArchiveFormat(service.getProvider());
        super.removedService(reference, service);
        LOGGER.info("FileFormatService is unregistered: " + service);
    }
}

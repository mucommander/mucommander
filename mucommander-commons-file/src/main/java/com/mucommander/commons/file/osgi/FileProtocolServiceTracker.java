/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;

/**
 * @author Arik Hadas
 */
public class FileProtocolServiceTracker extends ServiceTracker<FileProtocolService, FileProtocolService> {

    public FileProtocolServiceTracker(BundleContext context) {
        super(context, FileProtocolService.class,null);
    }

    @Override
    public FileProtocolService addingService(ServiceReference<FileProtocolService> reference) {
        FileProtocolService service = super.addingService(reference);
        FileFactory.registerProtocol(service.getSchema(), service.getProtocolProvider());
        FileURL.registerHandler(service.getSchema(), service.getSchemeHandler());
        return service;
    }

    @Override
    public void removedService(ServiceReference<FileProtocolService> reference, FileProtocolService service) {
        FileFactory.unregisterProtocol(service.getSchema());
        FileURL.unregisterHandler(service.getSchema());
        super.removedService(reference, service);
    }
}

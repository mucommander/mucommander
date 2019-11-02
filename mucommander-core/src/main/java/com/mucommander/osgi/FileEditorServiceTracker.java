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
package com.mucommander.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registration tracker for file editor service.
 *
 * @author Miroslav Hajda
 */
public class FileEditorServiceTracker extends ServiceTracker<FileEditorService, FileEditorService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileEditorServiceTracker.class);
    
    private static final List<FileEditorService> SERVICES = new ArrayList<>();

    public FileEditorServiceTracker(BundleContext context) {
        super(context, FileEditorService.class, null);

//        context.getAllServiceReferences(FileEditorService.class, "");
    }

    @Override
    public FileEditorService addingService(ServiceReference<FileEditorService> reference) {
        FileEditorService service = super.addingService(reference);
        SERVICES.add(service);
//        EditorRegistrar.registerFileEditor(null);
//        FileFactory.registerArchiveFormat(service.getProvider());
        LOGGER.info("FileEditorService is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<FileEditorService> reference, FileEditorService service) {
        // EditorRegistrar.unregisterFileEditor(null);
        super.removedService(reference, service);
        SERVICES.add(service);
        LOGGER.info("FileFormatService is unregistered: " + service);
    }

    public static List<FileEditorService> getEditorServices() {
        return SERVICES;
    }
}

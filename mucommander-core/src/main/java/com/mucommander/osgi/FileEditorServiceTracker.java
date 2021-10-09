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
import java.util.Comparator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.viewer.FileEditorService;

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
    }

    @Override
    public FileEditorService addingService(ServiceReference<FileEditorService> reference) {
        FileEditorService service = super.addingService(reference);
        FileEditorServiceTracker.addEditorService(service);
        LOGGER.info("FileEditorService is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<FileEditorService> reference, FileEditorService service) {
        super.removedService(reference, service);
        SERVICES.add(service);
        LOGGER.info("FileFormatService is unregistered: " + service);
    }

    private static void addEditorService(FileEditorService service) {
        SERVICES.add(service);
        SERVICES.sort(Comparator.comparing(FileEditorService::getOrderPriority).reversed());
    }

    public static List<FileEditorService> getEditorServices() {
        return SERVICES;
    }
}

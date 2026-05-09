/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package dev.barebones.commander.osgi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.barebones.commander.viewer.FileViewerService;

/**
 * Static registry for {@link FileViewerService}. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a plain registry whose
 * {@link #register(FileViewerService)} is called directly from each
 * viewer module's bootstrap entry point.
 */
public final class FileViewerServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileViewerServiceTracker.class);
    private static final List<FileViewerService> SERVICES = new ArrayList<>();

    private FileViewerServiceTracker() {
    }

    public static void register(FileViewerService service) {
        SERVICES.add(service);
        SERVICES.sort(Comparator.comparing(FileViewerService::getOrderPriority).reversed());
        LOGGER.info("FileViewerService is registered: " + service);
    }

    public static void unregister(FileViewerService service) {
        SERVICES.remove(service);
        LOGGER.info("FileViewerService is unregistered: " + service);
    }

    public static List<FileViewerService> getViewerServices() {
        return SERVICES;
    }
}

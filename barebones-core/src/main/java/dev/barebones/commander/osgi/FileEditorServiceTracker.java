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

import dev.barebones.commander.viewer.FileEditorService;

/**
 * Static registry for {@link FileEditorService}. Was an OSGi
 * {@code ServiceTracker} pre-Phase-2; is now a plain registry.
 */
public final class FileEditorServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileEditorServiceTracker.class);
    private static final List<FileEditorService> SERVICES = new ArrayList<>();

    private FileEditorServiceTracker() {
    }

    public static void register(FileEditorService service) {
        SERVICES.add(service);
        SERVICES.sort(Comparator.comparing(FileEditorService::getOrderPriority).reversed());
        LOGGER.info("FileEditorService is registered: " + service);
    }

    public static void unregister(FileEditorService service) {
        SERVICES.remove(service);
        LOGGER.info("FileEditorService is unregistered: " + service);
    }

    public static List<FileEditorService> getEditorServices() {
        return SERVICES;
    }
}

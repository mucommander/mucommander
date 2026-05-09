/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */
package dev.barebones.commander.viewer.text;

import dev.barebones.commander.osgi.FileEditorServiceTracker;
import dev.barebones.commander.osgi.FileViewerServiceTracker;
import dev.barebones.commander.snapshot.MuSnapshot;

public final class Activator {

    private Activator() {
    }

    public static void register() {
        MuSnapshot.registerHandler(new TextViewerSnapshot());

        TextFileViewerService service = new TextFileViewerService();
        FileViewerServiceTracker.register(service);
        FileEditorServiceTracker.register(service);
    }
}

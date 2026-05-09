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
package dev.barebones.commander.commons.file.osgi;

/**
 * Pre-Phase-2 this opened the protocol / format service trackers as the
 * commons-file OSGi bundle started. Now both trackers are static
 * registries (see {@link FileProtocolServiceTracker} and
 * {@link FileFormatServiceTracker}), so this class is just a no-op left
 * in place for symmetry with the other modules.
 */
public final class Activator {

    private Activator() {
    }

    public static void register() {
        // No-op. Static registries replace the dynamic trackers.
    }
}

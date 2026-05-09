/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import dev.barebones.commander.mount.ui.MountPanelProvider;
import dev.barebones.commander.protocol.ui.ProtocolPanelRegistry;

/**
 * Phase-2-style static register() entry point. Called by Bootstrap on
 * startup. Picks the OS-appropriate {@link MountCommand} and stashes a
 * ready-to-use {@link MountExecutor} on {@link MountService}, then
 * registers the {@link MountPanelProvider} so the Connect-to-server
 * dialog grows a "Mount" tab.
 */
public final class Activator {

    private Activator() {
    }

    public static void register() {
        String os = System.getProperty("os.name", "").toLowerCase();
        MountCommand command;
        if (os.contains("mac") || os.contains("darwin")) {
            command = new MacOSMountCommand();
        } else if (os.contains("linux")) {
            command = new LinuxMountCommand();
        } else {
            // Windows / OpenBSD / etc — Phase 10 targets Linux + macOS.
            // Leave MountService.executor() returning null; the
            // MountPanel still registers but its getServerURL throws
            // a clear error if the user opens the tab on an
            // unsupported OS.
            return;
        }
        MountService.install(new MountExecutor(command));
        ProtocolPanelRegistry.register(new MountPanelProvider());
    }
}

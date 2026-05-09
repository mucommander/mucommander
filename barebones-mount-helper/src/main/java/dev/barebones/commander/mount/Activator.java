/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

/**
 * Phase-2-style static register() entry point. Called by Bootstrap on
 * startup. Picks the OS-appropriate {@link MountCommand} and stashes a
 * ready-to-use {@link MountExecutor} on {@link MountService}, so the
 * UI layer doesn't repeat the OS-detection switch.
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
            // Leave MountService.executor() returning null; the dialog
            // layer should hide the "Mount remote share" menu item.
            return;
        }
        MountService.install(new MountExecutor(command));
    }
}

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
package dev.barebones.commander.desktop.linux;

import java.util.Arrays;
import java.util.List;

import dev.barebones.commander.desktop.DesktopAdapter;
import dev.barebones.commander.desktop.linux.gnome.ConfiguredGnomeDesktopAdapter;
import dev.barebones.commander.desktop.linux.gnome.GuessedGnomeDesktopAdapter;
import dev.barebones.commander.desktop.linux.kde.ConfiguredKde3DesktopAdapter;
import dev.barebones.commander.desktop.linux.kde.ConfiguredKde4DesktopAdapter;
import dev.barebones.commander.desktop.linux.kde.ConfiguredKde5DesktopAdapter;
import dev.barebones.commander.desktop.linux.kde.GuessedKde3DesktopAdapter;
import dev.barebones.commander.desktop.linux.kde.GuessedKde4DesktopAdapter;
import dev.barebones.commander.desktop.linux.kde.GuessedKde5DesktopAdapter;
import dev.barebones.commander.desktop.linux.xfce.ConfiguredXfceDesktopAdapter;
import dev.barebones.commander.desktop.linux.xfce.GuessedXfceDesktopAdapter;
import dev.barebones.commander.osgi.OperatingSystemService;
import dev.barebones.commander.osgi.OperatingSystemServiceTracker;

public final class Activator {

    private Activator() {
    }

    public static void register() {
        OperatingSystemServiceTracker.register(new OperatingSystemService() {
            @Override
            public List<DesktopAdapter> getDesktopAdapters() {
                // Unix desktops:
                // - Gnome before KDE (more popular).
                // - 'configured' before 'guessed' (guesses are less reliable and more expensive).
                return Arrays.asList(
                        new GuessedXfceDesktopAdapter(),
                        new GuessedKde3DesktopAdapter(),
                        new GuessedKde4DesktopAdapter(),
                        new GuessedKde5DesktopAdapter(),
                        new GuessedGnomeDesktopAdapter(),
                        new ConfiguredXfceDesktopAdapter(),
                        new ConfiguredKde3DesktopAdapter(),
                        new ConfiguredKde4DesktopAdapter(),
                        new ConfiguredKde5DesktopAdapter(),
                        new ConfiguredGnomeDesktopAdapter());
            }
        });
    }
}

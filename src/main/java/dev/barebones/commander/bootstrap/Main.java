/*
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
package dev.barebones.commander.bootstrap;

import com.beust.jcommander.JCommander;
import dev.barebones.commander.main.Configuration;
import dev.barebones.commander.main.UserPreferencesDir;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Plain-Java entry point for barebones-commander.
 *
 * Parses CLI arguments via JCommander (reusing the existing
 * {@link Configuration} class), assembles a property map, and hands off
 * to {@link Bootstrap#start(Map)} which calls every module's Activator
 * register() in dependency order. The core Activator's register() is
 * the last call and shows the Swing UI.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        JCommander jCommander = new JCommander(configuration);
        jCommander.parse(args);

        if (configuration.help) {
            jCommander.setProgramName("barebones-commander");
            jCommander.usage();
            return;
        }

        if (configuration.version) {
            String version = Main.class.getPackage().getImplementationVersion();
            jCommander.getConsole().println(version != null ? version : "0.1.0-SNAPSHOT");
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: no graphical environment detected.");
            return;
        }

        File preferencesFolder = configuration.preferences != null
                ? new File(configuration.preferences)
                : UserPreferencesDir.getDefaultPreferencesFolder();
        configuration.preferences = preferencesFolder.getAbsolutePath();
        System.setProperty("MUCOMMANDER_USER_PREFERENCES", configuration.preferences);

        Map<String, String> properties = new HashMap<>();
        properties.putAll(new AbstractMap<String, String>() {
            @Override
            public java.util.Set<Map.Entry<String, String>> entrySet() {
                return configuration.entrySet();
            }
        });

        Bootstrap.start(properties);
    }
}

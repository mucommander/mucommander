/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.desktop.linux.gnome;

import java.io.IOException;

/**
 * Utility class for interacting with gsettings, the modern GNOME 3+ configuration system.
 * <p>
 * This is the preferred way to access GNOME configuration. For older GNOME 2 systems,
 * see {@link GConfTool} as a fallback.
 * </p>
 */
public class GSettings {
    private static final String COMMAND = "gsettings";

    private static final String GSETTINGS_MOUSE_PATH = "org.gnome.desktop.peripherals.mouse";
    private static final String GSETTINGS_DOUBLE_CLICK_CONFIG_KEY = "double-click";

    private GSettings() {
        // Utility class - prevent instantiation
    }

    /**
     * Retrieves an integer configuration value from gsettings.
     *
     * @param path the schema path (e.g., "org.gnome.desktop.peripherals.mouse")
     * @param key the configuration key to retrieve
     * @return the integer value of the configuration key
     * @throws IOException if the command execution fails
     * @throws InterruptedException if the command is interrupted
     * @throws NumberFormatException if the output cannot be parsed as an integer
     */
    public static int getIntValue(String path, String key) throws IOException, InterruptedException {
        return OSCommand.runCommand(Integer::parseInt, COMMAND, "get", path, key);
    }

    /**
     * Retrieves the multi-click (double-click) interval from GNOME configuration.
     *
     * @return the double-click interval in milliseconds
     * @throws IOException if the command execution fails
     * @throws InterruptedException if the command is interrupted
     */
    public static int getMultiClickInterval() throws IOException, InterruptedException {
        return getIntValue(GSETTINGS_MOUSE_PATH, GSETTINGS_DOUBLE_CLICK_CONFIG_KEY);
    }
}

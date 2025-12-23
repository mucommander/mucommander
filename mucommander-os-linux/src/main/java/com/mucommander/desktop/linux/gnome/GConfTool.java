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
 * Utility class for interacting with gconftool, the legacy GNOME 2 configuration system.
 * <p>
 * Note: gconftool is deprecated since GNOME 3. Prefer {@link GSettings} which uses
 * the modern dconf/gsettings system. This class is kept as a fallback for older systems.
 * </p>
 */
public class GConfTool {
    private static final String COMMAND = "gconftool";

    private static final String GCONFTOOL_DOUBLE_CLICK_CONFIG_KEY = "/desktop/gnome/peripherals/mouse/double_click";

    private GConfTool() {
        // Utility class - prevent instantiation
    }

    /**
     * Retrieves an integer configuration value from gconftool.
     *
     * @param key the configuration key to retrieve
     * @return the integer value of the configuration key
     * @throws IOException if the command execution fails
     * @throws InterruptedException if the command is interrupted
     * @throws NumberFormatException if the output cannot be parsed as an integer
     */
    public static int getIntValue(String key) throws IOException, InterruptedException {
        return OSCommand.runCommandWithIntReturn(COMMAND, "-g", key);
    }

    /**
     * Retrieves the multi-click (double-click) interval from GNOME configuration.
     *
     * @return the double-click interval in milliseconds
     * @throws IOException if the command execution fails
     * @throws InterruptedException if the command is interrupted
     */
    public static int getMultiClickInterval() throws IOException, InterruptedException {
        return getIntValue(GCONFTOOL_DOUBLE_CLICK_CONFIG_KEY);
    }
}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Utility class for executing OS commands and capturing their output.
 * <p>
 * This class provides methods to run external commands and retrieve their results,
 * used by {@link GSettings} and {@link GConfTool} for GNOME configuration access.
 * </p>
 */
class OSCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSCommand.class);

    private OSCommand() {
        // Utility class - prevent instantiation
    }

    /**
     * Executes an OS command and returns its output.
     *
     * @param command the command and its arguments to execute
     * @return the command output with newlines stripped
     * @throws IOException if an I/O error occurs during command execution
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    static String runCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(Paths.get(System.getProperty("user.dir")).toFile());

        String commandStr = String.join(" ", command);
        LOGGER.debug("Executing: {}", commandStr);

        Process process = pb.start();
        int exitCode = process.waitFor();

        String output = new String(process.getInputStream().readAllBytes()).replace("\n", "").replace("\r", "");

        if (exitCode != 0) {
            String error = new String(process.getErrorStream().readAllBytes());
            LOGGER.error("Error executing command: {}", commandStr);
            LOGGER.error("Exit code: {}", exitCode);
            LOGGER.error("Output: {}", output);
            LOGGER.error("Error: {}", error);
        }

        return output;
    }

    /**
     * Executes an OS command and returns its output as an integer.
     *
     * @param command the command and its arguments to execute
     * @return the command output parsed as an integer
     * @throws IOException if an I/O error occurs during command execution
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws NumberFormatException if the output cannot be parsed as an integer
     */
    static int runCommandWithIntReturn(String... command) throws IOException, InterruptedException {
        return Integer.parseInt(runCommand(command));
    }
}

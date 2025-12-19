package com.mucommander.desktop.linux.gnome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class OSCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSCommand.class);

    static String runCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(Paths.get(System.getProperty("user.dir")).toFile());

        // Log command execution
        String commandStr = String.join(" ", command);
        LOGGER.debug("Executing: {}", commandStr);

        Process process = pb.start();
        int exitCode = process.waitFor();

        String output = new String(process.getInputStream().readAllBytes()).replace("\n", "").replace("\r", "");
        String error = new String(process.getErrorStream().readAllBytes());

        if (exitCode != 0) {
            LOGGER.error("Error executing command : {}", commandStr);
            LOGGER.error("Exit code : {}", exitCode);
            LOGGER.error("Output : {}", output);
            LOGGER.error("Error : {}", error);
        }

        return output;
    }

    static int runCommandWithIntReturn(String... command) throws IOException, InterruptedException {
        return Integer.parseInt(runCommand(command));
    }
}

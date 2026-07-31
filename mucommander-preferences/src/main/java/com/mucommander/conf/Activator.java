package com.mucommander.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Preferences initialization for JPMS-based application.
 */
public class Activator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    /**
     * Initializes preferences system with the specified preferences folder.
     *
     * @param preferencesFolder the folder to store preferences, or null to use default
     * @throws IOException if preferences folder cannot be set
     */
    public static void initialize(String preferencesFolder) throws IOException {
        LOGGER.debug("Initializing preferences system");
        if (preferencesFolder != null) {
            PlatformManager.setPreferencesFolder(preferencesFolder);
        }
        MuConfigurations.loadPreferences();
        LOGGER.info("Preferences system initialized");
    }
}

/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander;

import java.util.Locale;

import com.mucommander.commons.file.module.FileServicesLoader;
import com.mucommander.module.BrowsableMenuItemsLoader;
import com.mucommander.module.OperatingSystemsLoader;
import com.mucommander.module.ProtocolPanelProvidersLoader;
import com.mucommander.module.TranslationLoader;
import com.mucommander.ui.viewer.EditorSnapshot;
import com.mucommander.ui.viewer.ViewerSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.module.FileEditorsLoader;
import com.mucommander.module.FileViewersLoader;
import com.mucommander.search.SearchSnapshot;
import com.mucommander.snapshot.MuSnapshot;

/**
 * This is the initialization class for the core component of muCommander.
 * This class handles executing shutdown tasks when:
 * 1. Shutdown action is initiated by the application
 * 2. When the Java virtual machine goes down (e.g., via CNTL+C)
 * @author Arik Hadas
 */
public class Activator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    /** Registered shutdown-hook */
    private ShutdownHook shutdownHook;

    private static Activator instance;

    public static boolean portable;

    private static String appMode;
    private static String initialFolders;
    private static String silentMode;
    private static String fatalWarningsMode;
    private static String assocProperty;
    private static String bookmarkProperty;
    private static String configurationProperty;
    private static String commandbarProperty;
    private static String extensionsProperty;
    private static String commandsProperty;
    private static String keymapProperty;
    private static String toolbarProperty;
    private static String credentialsProperty;

    static {
        // Static initialization - load system properties
        appMode = System.getProperty("app_mode");
        initialFolders = System.getProperty("mucommander.folders");
        silentMode = System.getProperty("mucommander.silent");
        fatalWarningsMode = System.getProperty("mucommander.fatalWarnings");
        assocProperty = System.getProperty("mucommander.assoc");
        bookmarkProperty = System.getProperty("mucommander.bookmark");
        configurationProperty = System.getProperty("mucommander.configuration");
        commandbarProperty = System.getProperty("mucommander.commandbar");
        extensionsProperty = System.getProperty("mucommander.extensions");
        commandsProperty = System.getProperty("mucommander.commands");
        keymapProperty = System.getProperty("mucommander.keymap");
        toolbarProperty = System.getProperty("mucommander.toolbar");
        credentialsProperty = System.getProperty("mucommander.credentials");

        instance = new Activator();
    }

    public static Activator getInstance() {
        return instance;
    }

    public void start() {
        LOGGER.debug("starting");
        portable = "portable".equals(appMode);
        MuSnapshot.registerHandler(new SearchSnapshot());
        MuSnapshot.registerHandler(new ViewerSnapshot());
        MuSnapshot.registerHandler(new EditorSnapshot());

        FileServicesLoader.load();
        ProtocolPanelProvidersLoader.load();
        TranslationLoader.load();
        FileViewersLoader.load();
        FileEditorsLoader.load();
        OperatingSystemsLoader.load();
        BrowsableMenuItemsLoader.load();

        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(shutdownHook = new ShutdownHook());

        // Make sure the filename locale is set in the preferences
        var filenameLocale = MuConfigurations.getPreferences().getVariable(MuPreference.FILENAME_LOCALE);
        if (filenameLocale == null)
            MuConfigurations.getPreferences().setVariable(MuPreference.FILENAME_LOCALE, Locale.getDefault().toLanguageTag());

        Application.run(this);
    }

    public java.util.List<String> getInitialFolders() {
        if (initialFolders == null || initialFolders.length() == 0) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.asList(initialFolders.split(","));
    }

    public boolean silent() {
        return Boolean.parseBoolean(silentMode);
    }

    public boolean fatalWarnings() {
        return Boolean.parseBoolean(fatalWarningsMode);
    }

    public String assoc() {
        return assocProperty;
    }

    public String bookmark() {
        return bookmarkProperty;
    }

    public String configuration() {
        return configurationProperty;
    }

    public String commandbar() {
        return commandbarProperty;
    }

    public String extensions() {
        return extensionsProperty;
    }

    public String commands() {
        return commandsProperty;
    }

    public String keymap() {
        return keymapProperty;
    }

    public String toolbar() {
        return toolbarProperty;
    }

    public String credentials() {
        return credentialsProperty;
    }
}

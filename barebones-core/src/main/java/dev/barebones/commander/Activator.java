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
package dev.barebones.commander;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.barebones.commander.bookmark.file.BookmarkProtocolProvider;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.commons.file.SchemeHandler;
import dev.barebones.commander.commons.file.osgi.FileProtocolService;
import dev.barebones.commander.commons.file.osgi.FileProtocolServiceTracker;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;
import dev.barebones.commander.conf.MuConfigurations;
import dev.barebones.commander.conf.MuPreference;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.os.api.CoreService;
import dev.barebones.commander.search.SearchSnapshot;
import dev.barebones.commander.snapshot.MuSnapshot;
import dev.barebones.commander.ui.action.ActionManager;
import dev.barebones.commander.ui.dialog.about.AboutDialog;
import dev.barebones.commander.ui.dialog.shutdown.QuitDialog;
import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.WindowManager;
import dev.barebones.commander.ui.viewer.EditorSnapshot;
import dev.barebones.commander.ui.viewer.ViewerSnapshot;

/**
 * Pre-Phase-2 this was the OSGi bundle activator of barebones-core. After
 * Phase 2 it is a plain class instantiated once by Bootstrap with the
 * configuration property map; its constructor performs the same setup
 * (snapshot handlers, bookmark protocol, core service, shutdown hook)
 * the OSGi {@code start} method used to do, then hands off to
 * {@link Application#run(Activator)}.
 *
 * Application reads CLI args / properties through this Activator's
 * accessor methods (assoc, bookmark, ...). Those used to delegate to
 * {@code BundleContext.getProperty}; they now read from the property
 * map passed to the constructor.
 */
public class Activator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    public static boolean portable;

    private final Map<String, String> properties;
    private ShutdownHook shutdownHook;

    public Activator(Map<String, String> properties) {
        this.properties = properties;
    }

    public void register() {
        LOGGER.debug("starting");
        portable = "portable".equals(properties.get("app_mode"));
        MuSnapshot.registerHandler(new SearchSnapshot());
        MuSnapshot.registerHandler(new ViewerSnapshot());
        MuSnapshot.registerHandler(new EditorSnapshot());
        // Register the application-specific 'bookmark' protocol.
        FileProtocolServiceTracker.register(createBookmarkProtocolService());
        // Drain protocol panel providers contributed by individual protocol
        // modules (sftp/s3/nfs) and wire each one into ServerConnectDialog
        // and DrivePopupButton. Pre-Phase-2 this fan-out lived in an OSGi
        // ServiceTracker; producers now stage panels in the api-side
        // ProtocolPanelRegistry and core drains it here.
        for (dev.barebones.commander.protocol.ui.ProtocolPanelProvider provider :
                dev.barebones.commander.protocol.ui.ProtocolPanelRegistry.all()) {
            dev.barebones.commander.ui.dialog.server.ServerConnectDialog.register(provider);
            if (provider.getPanelClass() != null) {
                dev.barebones.commander.ui.main.DrivePopupButton.register(provider);
            }
        }
        // Register core functionality service for macOS' EAWTHandler.
        dev.barebones.commander.os.api.CoreServiceHolder.set(createCoreService());
        // Trap VM shutdown.
        Runtime.getRuntime().addShutdownHook(shutdownHook = new ShutdownHook());

        // Make sure the filename locale is set in the preferences.
        var filenameLocale = MuConfigurations.getPreferences().getVariable(MuPreference.FILENAME_LOCALE);
        if (filenameLocale == null) {
            MuConfigurations.getPreferences().setVariable(MuPreference.FILENAME_LOCALE, Locale.getDefault().toLanguageTag());
        }

        Application.run(this);
    }

    /** Best-effort shutdown — invoked by the application quit flow. */
    public void stopAll() {
        if (ShutdownHook.performShutdownTasks() && shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // VM is already shutting down.
            }
        }
        System.exit(0);
    }

    public List<String> getInitialFolders() {
        String folders = properties.get("mucommander.folders");
        if (folders == null || folders.length() == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(folders.split(","));
    }

    public boolean silent() { return Boolean.parseBoolean(properties.get("mucommander.silent")); }
    public boolean fatalWarnings() { return Boolean.parseBoolean(properties.get("mucommander.fatalWarnings")); }
    public String assoc() { return properties.get("mucommander.assoc"); }
    public String bookmark() { return properties.get("mucommander.bookmark"); }
    public String configuration() { return properties.get("mucommander.configuration"); }
    public String commandbar() { return properties.get("mucommander.commandbar"); }
    public String extensions() { return properties.get("mucommander.extensions"); }
    public String commands() { return properties.get("mucommander.commands"); }
    public String keymap() { return properties.get("mucommander.keymap"); }
    public String toolbar() { return properties.get("mucommander.toolbar"); }
    public String credentials() { return properties.get("mucommander.credentials"); }

    private CoreService createCoreService() {
        return new CoreService() {
            @Override
            public void showAbout() {
                MainFrame mainFrame = WindowManager.getCurrentMainFrame();
                if (mainFrame.getNoEventsMode()) return;
                new AboutDialog(mainFrame).showDialog();
            }

            @Override
            public void showPreferences() {
                MainFrame mainFrame = WindowManager.getCurrentMainFrame();
                if (mainFrame.getNoEventsMode()) return;
                ActionManager.performAction(ActionType.ShowPreferences, mainFrame);
            }

            @Override
            public boolean doQuit() {
                if (!QuitDialog.confirmQuit()) return false;
                Application.initiateShutdown();
                return true;
            }

            @Override
            public void openFile(String path) {
                Application.waitUntilLaunched();
                AbstractFile file = FileFactory.getFile(path);
                FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
                if (file == null) {
                    LOGGER.error("Ignoring open file, as File is null for path: {}.", path);
                    return;
                }
                if (file.isBrowsable()) {
                    activePanel.tryChangeCurrentFolder(file);
                } else {
                    activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
                }
            }
        };
    }

    private FileProtocolService createBookmarkProtocolService() {
        return new FileProtocolService() {
            @Override public SchemeHandler getSchemeHandler() { return new DefaultSchemeHandler(); }
            @Override public String getSchema() { return BookmarkProtocolProvider.BOOKMARK; }
            @Override public ProtocolProvider getProtocolProvider() { return new BookmarkProtocolProvider(); }
        };
    }
}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.bookmark.file.BookmarkProtocolProvider;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.DefaultSchemeHandler;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.SchemeHandler;
import com.mucommander.commons.file.osgi.FileProtocolService;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.os.api.CoreService;
import com.mucommander.osgi.FileEditorServiceTracker;
import com.mucommander.osgi.FileViewerServiceTracker;
import com.mucommander.osgi.OperatingSystemServiceTracker;
import com.mucommander.text.TranslationTracker;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.dialog.about.AboutDialog;
import com.mucommander.ui.dialog.shutdown.QuitDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.osgi.ProtocolPanelProviderTracker;

/**
 * This is the OSGi bundle-activator of the core component of muCommander.
 * Besides reacting to the {@link #start(BundleContext)} and {@link #stop(BundleContext)} operations,
 * this activator also handles executing shutdown tasks when:
 * 1. Shutdown action is initiated by the application
 * 2. When the Java virtual machine goes down (e.g., via CNTL+C)
 * @author Arik Hadas
 */
public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ProtocolPanelProviderTracker protocolPanelTracker;
    private TranslationTracker translationTracker;
    private FileViewerServiceTracker viewersTracker;
    private FileEditorServiceTracker editorsTracker;
    private OperatingSystemServiceTracker osTracker;

    private ServiceRegistration<CoreService> coreRegistration;
    private ServiceRegistration<FileProtocolService> bookmarksRegistration;

    /** Registered shutdown-hook */
    private ShutdownHook shutdownHook;

    private BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        LOGGER.debug("starting");
        this.context = context;
        // Register the application-specific 'bookmark' protocol.
        FileProtocolService bookmarksService = createBookmarkProtocolService();
        bookmarksRegistration = context.registerService(FileProtocolService.class, bookmarksService, null);
        // Listen to protocol panel services
        protocolPanelTracker = new ProtocolPanelProviderTracker(context);
        protocolPanelTracker.open();
        // Listen to translation service
        translationTracker = new TranslationTracker(context);
        translationTracker.open();
        // Listen to file viewer services
        viewersTracker = new FileViewerServiceTracker(context);
        viewersTracker.open();
        // Listen to file editor services
        editorsTracker = new FileEditorServiceTracker(context);
        editorsTracker.open();
        // Listen to operating system services
        osTracker = new OperatingSystemServiceTracker(context);
        osTracker.open();
        // Register core functionality service
        CoreService coreService = createCoreService();
        coreRegistration = context.registerService(CoreService.class, coreService, null);
        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(shutdownHook = new ShutdownHook());
        Application.run(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.debug("stopping");
        protocolPanelTracker.close();
        translationTracker.close();
        viewersTracker.close();
        editorsTracker.close();
        osTracker.close();
        coreRegistration.unregister();
        bookmarksRegistration.unregister();
        // if the activator performs the shutdown tasks, no need for the shutdown-hook
        if (ShutdownHook.performShutdownTasks())
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    /**
     * Stops the whole application
     */
    public void stopAll() throws BundleException {
        // stop the system bundle
        context.getBundle(0).stop();
    }

    public List<String> getInitialFolders() {
        String folders = context.getProperty("mucommander.folders");
        if (folders == null || folders.length() == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(folders.split(","));
    }

    public boolean silent() {
        return Boolean.parseBoolean(context.getProperty("mucommander.silent"));
    }

    public boolean fatalWarnings() {
        return Boolean.parseBoolean(context.getProperty("mucommander.fatalWarnings"));
    }

    public String assoc() {
        return context.getProperty("mucommander.assoc");
    }

    public String bookmark() {
        return context.getProperty("mucommander.bookmark");
    }

    public String configuration() {
        return context.getProperty("mucommander.configuration");
    }

    public String commandbar() {
        return context.getProperty("mucommander.commandbar");
    }

    public String extensions() {
        return context.getProperty("mucommander.extensions");
    }

    public String commands() {
        return context.getProperty("mucommander.commands");
    }

    public String keymap() {
        return context.getProperty("mucommander.keymap");
    }

    public String shellHistory() {
        return context.getProperty("mucommander.shellHistory");
    }

    public String toolbar() {
        return context.getProperty("mucommander.toolbar");
    }

    public String credentials() {
        return context.getProperty("mucommander.credentials");
    }

    private CoreService createCoreService() {
        return new CoreService() {

            @Override
            public void showAbout() {
                MainFrame mainFrame = WindowManager.getCurrentMainFrame();

                // Do nothing (return) when in 'no events mode'
                if(mainFrame.getNoEventsMode())
                    return;

                new AboutDialog(mainFrame).showDialog();
            }

            @Override
            public void showPreferences() {
                MainFrame mainFrame = WindowManager.getCurrentMainFrame();

                // Do nothing (return) when in 'no events mode'
                if(mainFrame.getNoEventsMode())
                    return;

                ActionManager.performAction(com.mucommander.ui.action.impl.ShowPreferencesAction.Descriptor.ACTION_ID, mainFrame);
            }

            @Override
            public boolean doQuit() {
                // Ask the user for confirmation and abort if user refused to quit.
                if(!QuitDialog.confirmQuit())
                    return false;

                // We got a green -> quit!
                Application.initiateShutdown();

                return true;
            }

            @Override
            public void openFile(String path) {
                // Wait until the application has been launched. This step is required to properly handle the case where the
                // application is launched with a file to open, for instance when drag-n-dropping a file to the Dock icon
                // when muCommander is not started yet. In this case, this method is called while Launcher is still busy
                // launching the application (no mainframe exists yet).
                Application.waitUntilLaunched();

                AbstractFile file = FileFactory.getFile(path);
                FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
                if (file.isBrowsable())
                    activePanel.tryChangeCurrentFolder(file);
                else
                    activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
            }
        };
    }

    private FileProtocolService createBookmarkProtocolService() {
        return new FileProtocolService() {

            @Override
            public SchemeHandler getSchemeHandler() {
                return new DefaultSchemeHandler();
            }

            @Override
            public String getSchema() {
                return BookmarkProtocolProvider.BOOKMARK;
            }

            @Override
            public ProtocolProvider getProtocolProvider() {
                return new BookmarkProtocolProvider();
            }
        };
    }
}

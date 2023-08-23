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

package com.mucommander;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.icon.impl.SwingFileIconProvider;
import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.conf.SystemIconsPolicy;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.startup.CheckVersionDialog;
import com.mucommander.ui.dialog.startup.InitialSetupDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.main.SplashScreen;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.main.frame.CommandLineMainFrameBuilder;
import com.mucommander.ui.main.frame.DefaultMainFramesBuilder;
import com.mucommander.ui.main.toolbar.ToolBarIO;
import com.mucommander.utils.MuLogging;

import javax.swing.SwingUtilities;

/**
 * The graphical application.
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    // - Class fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** true while the application is launching, false after it has finished launching */
    private static boolean isLaunching = true;
    /** Launch lock. */
    private static final Object LAUNCH_LOCK = new Object();
    /** OSGi BundleActivator */
    private static Activator activator;

    // - Initialization ---------------------------------------------------------
    // --------------------------------------------------------------------------
    public Application() {
    }

    /**
     * This method can be called to wait until the application has been launched. The caller thread will be blocked
     * until the application has been launched. This method will return immediately if the application has already been
     * launched when it is called.
     */
    public static void waitUntilLaunched() {
        LOGGER.debug("called, thread=" + Thread.currentThread());
        synchronized (LAUNCH_LOCK) {
            while (isLaunching) {
                try {
                    LOGGER.debug("waiting");
                    LAUNCH_LOCK.wait();
                } catch (InterruptedException e) {
                    // will loop
                    LOGGER.trace("Interrupted exception", e);
                }
            }
        }
    }

    /**
     * Prints the specified error message to stderr.
     * 
     * @param msg
     *            error message to print to stderr.
     * @param quit
     *            whether to quit after printing the error message.
     * @param exception
     *            exception that triggered the error (for verbose output).
     */
    private void printError(String msg, Exception exception, boolean quit) {
        printError(createErrorMessage(msg, exception, quit).toString(), quit);
    }

    /**
     * Creates an error message.
     */
    private StringBuilder createErrorMessage(String msg, Exception exception, boolean quit) {
        StringBuilder error;

        error = new StringBuilder();
        if (quit) {
            error.append("Warning: ");
        }
        error.append(msg);
        if (!activator.silent() && (exception != null)) {
            error.append(": ");
            error.append(exception.getMessage());
        }

        return error;
    }

    /**
     * Prints an error message.
     */
    private static void printError(String msg, boolean quit) {
        System.err.println(msg);
        if (quit) {
            System.err.println("See mucommander --help for more information.");
            System.exit(1);
        }
    }

    /**
     * Prints a configuration file specific error message.
     */
    private void printFileError(String msg, Exception exception, boolean quit) {
        StringBuilder error;

        error = createErrorMessage(msg, exception, quit);
        if (!quit) {
            error.append(". Using default values.");
        }

        printError(error.toString(), quit);
    }

    /**
     * Prints the specified startup message.
     */
    private void printStartupMessage(SplashScreen splashScreen, String message) {
        if (splashScreen != null) {
            splashScreen.setLoadingMessage(message);
        }

        LOGGER.info(message);
    }

    // - Boot code --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Method used to migrate commands that used to be defined in the configuration but were moved to
     * <code>commands.xml</code>.
     * 
     * @param useName
     *            name of the <code>use custom command</code> configuration variable.
     * @param commandName
     *            name of the <code>custom command</code> configuration variable.
     */
    private static void migrateCommand(String useName, String commandName, String alias) {
        String command;

        if (MuConfigurations.getPreferences().getBooleanVariable(useName)
                && (command = MuConfigurations.getPreferences().getVariable(commandName)) != null) {
            try {
                CommandManager.registerCommand(new Command(alias, command, CommandType.SYSTEM_COMMAND));
            } catch (CommandException e) {
                // Ignore this: the command didn't work in the first place, we might as well get rid of it.
                LOGGER.trace("Command exception", e);
            }
            MuConfigurations.getPreferences().removeVariable(useName);
            MuConfigurations.getPreferences().removeVariable(commandName);
        }
    }

    private void run() {
        System.out.println(new Date() + " -- Application#run");
        SplashScreen splashScreen = null;
        try {
            // Associations handling.
            String assoc = activator.assoc();
            if (assoc != null) {
                try {
                    com.mucommander.command.CommandManager.setAssociationFile(assoc);
                } catch (Exception e) {
                    printError("Could not set association files", e, activator.fatalWarnings());
                }
            }

            // Custom commands handling.
            String commands = activator.commands();
            if (commands != null) {
                try {
                    com.mucommander.command.CommandManager.setCommandFile(commands);
                } catch (Exception e) {
                    printError("Could not set commands file", e, activator.fatalWarnings());
                }
            }

            // Bookmarks handling.
            String bookmark = activator.bookmark();
            if (bookmark != null) {
                try {
                    com.mucommander.bookmark.BookmarkManager.setBookmarksFile(bookmark);
                } catch (Exception e) {
                    printError("Could not set bookmarks file", e, activator.fatalWarnings());
                }
            }

            // Configuration handling.
            String configuration = activator.configuration();
            if (configuration != null) {
                try {
                    MuConfigurations.setPreferencesFile(configuration);
                } catch (Exception e) {
                    printError("Could not set configuration file", e, activator.fatalWarnings());
                }
            }

            // Keymap file.
            String keymap = activator.keymap();
            if (keymap != null) {
                try {
                    com.mucommander.ui.action.ActionKeymapIO.setActionsFile(keymap);
                } catch (Exception e) {
                    printError("Could not set keymap file", e, activator.fatalWarnings());
                }
            }

            // Toolbar file.
            String toolbar = activator.toolbar();
            if (toolbar != null) {
                try {
                    ToolBarIO.setDescriptionFile(toolbar);
                } catch (Exception e) {
                    printError("Could not set keymap file", e, activator.fatalWarnings());
                }
            }

            // Commandbar file.
            String commandbar = activator.commandbar();
            if (commandbar != null) {
                try {
                    CommandBarIO.setDescriptionFile(commandbar);
                } catch (Exception e) {
                    printError("Could not set commandbar description file", e, activator.fatalWarnings());
                }
            }

            // Credentials file.
            String credentials = activator.credentials();
            if (credentials != null) {
                try {
                    com.mucommander.auth.CredentialsManager.setCredentialsFile(credentials);
                } catch (Exception e) {
                    printError("Could not set credentials file", e, activator.fatalWarnings());
                }
            }

            // Extensions folder.
            String extensions = activator.extensions();
            if (extensions != null) {
                try {
                    ExtensionManager.setExtensionsFolder(extensions);
                } catch (Exception e) {
                    printError("Could not set extensions folder", e, activator.fatalWarnings());
                }
            }

            // - Configuration init ---------------------------------------
            // ------------------------------------------------------------

            // Attempts to guess whether this is the first time muCommander is booted or not.
            boolean isFirstBoot;
            try {
                isFirstBoot = !MuConfigurations.isPreferencesFileExists();
            } catch (IOException e) {
                isFirstBoot = true;
            }

            // Load snapshot data before loading configuration as until version 0.9 the snapshot properties
            // were stored as preferences so when loading such preferences they could overload snapshot properties
            try {
                MuSnapshot.loadSnapshot();
            } catch (Exception e) {
                printFileError("Could not load snapshot", e, activator.fatalWarnings());
            }

            // Configuration needs to be loaded before any sort of GUI creation is performed : under Mac OS X, if we're
            // to use the metal look, we need to know about it right about now.
            try {
                MuConfigurations.check();
            } catch (Exception e) {
                printFileError("Could not load configuration", e, activator.fatalWarnings());
            }

            // - Logging configuration ------------------------------------
            // ------------------------------------------------------------
            MuLogging.configureLogging();

            // - Set the bundle's class loader as the default one
            // ------------------------------------------------------------
            ResourceLoader.setDefaultClassLoader(this.getClass().getClassLoader());

            // - muCommander boot -----------------------------------------
            // ------------------------------------------------------------
            // Adds all extensions to the classpath.
            try {
                ExtensionManager.addExtensionsToClasspath();
            } catch (Exception e) {
                LOGGER.debug("Failed to add extensions to the classpath", e);
            }

            // This the property is supposed to have the java.net package use the proxy defined in the system settings
            // to establish HTTP connections. This property is supported only under Java 1.5 and up.
            // Note that Mac OS X already uses the system HTTP proxy, with or without this property being set.
            System.setProperty("java.net.useSystemProxies", "true");

            // Shows the splash screen, if enabled in the preferences
            boolean useSplash = MuConfigurations.getPreferences()
                    .getVariable(MuPreference.SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN);
            if (useSplash) {
                new Thread(() -> {
                    splashScreen = new SplashScreen(RuntimeConstants.VERSION, "Loading preferences...");
                }).start();
            }

            boolean showSetup;
            showSetup = MuConfigurations.getPreferences().getVariable(MuPreference.THEME_TYPE) == null;

            // Configure filesystems
            configureFilesystems();

            if (isFirstBoot) {
                try {
                    com.mucommander.ui.main.WindowManager.setDefaultLookAndFeel();
                } catch (Exception e) {
                    printError("Could not initialize look & feel", e, true);
                }
            }

            ExecutorService executor = null;
            try {
                var firstBoot = isFirstBoot;
                executor = Executors.newFixedThreadPool(8);

                executor.execute(() -> {
                    // Initializes the desktop.
                    try {
                        com.mucommander.core.desktop.DesktopManager.init(firstBoot);
                    } catch (Exception e) {
                        printError("Could not initialize desktop", e, true);
                    }
                });

                executor.execute(() -> {
                    // Loads custom commands
                    printStartupMessage(splashScreen, "Loading file associations..."); // TODO Localize those messages.....
                    try {
                        com.mucommander.command.CommandManager.loadCommands();
                    } catch (Exception e) {
                        printFileError("Could not load custom commands", e, activator.fatalWarnings());
                    }
                });

                executor.execute(() -> {
                    // Migrates the custom editor and custom viewer if necessary.
                    migrateCommand("viewer.use_custom", "viewer.custom_command", CommandManager.VIEWER_ALIAS);
                    migrateCommand("editor.use_custom", "editor.custom_command", CommandManager.EDITOR_ALIAS);
                    try {
                        CommandManager.writeCommands();
                    } catch (Exception e) {
                        System.out.println("###############################");
                        LOGGER.debug("Caught exception", e);
                        // There's really nothing we can do about this...
                    }
                });

                executor.execute(() -> {
                    try {
                        com.mucommander.command.CommandManager.loadAssociations();
                    } catch (Exception e) {
                        printFileError("Could not load custom associations", e, activator.fatalWarnings());
                    }
                });

                executor.execute(() -> {
                    // Loads bookmarks
                    printStartupMessage(splashScreen, "Loading bookmarks...");
                    try {
                        com.mucommander.bookmark.BookmarkManager.loadBookmarks();
                    } catch (Exception e) {
                        printFileError("Could not load bookmarks", e, activator.fatalWarnings());
                    }
                });

                executor.execute(() -> {
                    // Loads credentials
                    printStartupMessage(splashScreen, "Loading credentials...");
                    try {
                        com.mucommander.auth.CredentialsManager.loadCredentials();
                    } catch (Exception e) {
                        printFileError("Could not load credentials", e, activator.fatalWarnings());
                    }
                });

                executor.execute(() -> {
                    // Inits CustomDateFormat to make sure that its ConfigurationListener is added
                    // before FileTable, so CustomDateFormat gets notified of date format changes first
                    com.mucommander.text.CustomDateFormat.init();

                    // Initialize file icons
                    printStartupMessage(splashScreen, "Loading icons...");
                    // Initialize the SwingFileIconProvider from the main thread, see method Javadoc for an explanation on why
                    // we do this now
                    SwingFileIconProvider.forceInit();
                    setFileIconsScaleFactor();
                    setSystemIconsPolicy();
                });

                long pre = System.currentTimeMillis();
                executor.shutdown();
                executor.awaitTermination(20L, TimeUnit.SECONDS);
                System.out.println("------- Application#run pre main took: " + (System.currentTimeMillis() - pre));

                // The code below makes keyboard actions not working (even up/down doesn't work then)
                //executor.execute(() -> {
                    // Register actions
                    printStartupMessage(splashScreen, "Registering actions...");
                    ActionManager.registerActions();

                    // Loads the ActionKeymap file
                    printStartupMessage(splashScreen, "Loading actions shortcuts...");
                    try {
                        com.mucommander.ui.action.ActionKeymapIO.loadActionKeymap();
                    } catch (Exception e) {
                        printFileError("Could not load actions shortcuts", e, activator.fatalWarnings());
                    }

                    // Loads the ToolBar's description file
                    printStartupMessage(splashScreen, "Loading toolbar description...");
                    try {
                        ToolBarIO.loadDescriptionFile();
                    } catch (Exception e) {
                        printFileError("Could not load toolbar description", e, activator.fatalWarnings());
                    }

                    // Loads the CommandBar's description file
                    printStartupMessage(splashScreen, "Loading command bar description...");
                    try {
                        CommandBarIO.loadCommandBar();
                    } catch (Exception e) {
                        printFileError("Could not load commandbar description", e, activator.fatalWarnings());
                    }
                //});
            } finally {
                if (executor != null) {
                    executor.shutdown();
                    executor.awaitTermination(20L, TimeUnit.SECONDS);
                }
            }


            new Thread(() -> {
                LOGGER.error("muC UI about to be presented");
                // Loads the themes.
                printStartupMessage(splashScreen, "Loading theme...");
                try {
                    SwingUtilities.invokeAndWait(() -> com.mucommander.ui.theme.ThemeManager.loadCurrentTheme());
                } catch (InterruptedException | InvocationTargetException e) {
                    LOGGER.error("Error loading current theme, continuing without it", e);
                }
                // Creates the initial main frame using any initial path specified by the command line.
                printStartupMessage(splashScreen, "Initializing window...");
                List<String> folders = activator.getInitialFolders();
                if (CollectionUtils.isNotEmpty(folders)) {
                    WindowManager.createNewMainFrame(new CommandLineMainFrameBuilder(folders));
                } else {
                    WindowManager.createNewMainFrame(new DefaultMainFramesBuilder());
                }
                // Dispose splash screen.
                if (splashScreen != null) {
                    splashScreen.dispose();
                    splashScreen = null;
                }

                // Enable system notifications, only after MainFrame is created as SystemTrayNotifier needs to retrieve
                // a MainFrame instance
                if (MuConfigurations.getPreferences()
                        .getVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS,
                                MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS)) {
                printStartupMessage(splashScreen, "Enabling system notifications...");
                    if (com.mucommander.ui.notifier.NotifierProvider.isAvailable())
                        com.mucommander.ui.notifier.NotifierProvider.getNotifier().setEnabled(true);
                }

                long pre = System.currentTimeMillis();
                LOGGER.error("muC UI presented after: " + ManagementFactory.getRuntimeMXBean().getUptime() +
                        "ms (RuntimeMXBean loaded in " + (System.currentTimeMillis() - pre) + "ms)");
                // Done launching, wake up threads waiting for the application being launched.
                // Important: this must be done before disposing the splash screen, as this would otherwise create a
                // deadlock if the AWT event thread were waiting in #waitUntilLaunched .
                synchronized (LAUNCH_LOCK) {
                    isLaunching = false;
                    LAUNCH_LOCK.notifyAll();
                }
            }).start();

            // Check for newer version unless it was disabled
            if (MuConfigurations.getPreferences()
                    .getVariable(MuPreference.CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE)) {
                CompletableFuture.runAsync(() -> {
                    new CheckVersionDialog(WindowManager.getCurrentMainFrame(), false);
                }, CompletableFuture.delayedExecutor(10L, TimeUnit.SECONDS));
            }

            // If no theme is configured in the preferences, ask for an initial theme.
            if (showSetup) {
                new InitialSetupDialog(WindowManager.getCurrentMainFrame()).showDialog();
            }
        } catch (Throwable t) {
            // Startup failed, dispose the splash screen
            if (splashScreen != null) {
                splashScreen.dispose();
                splashScreen = null;
            }

            LOGGER.error("Startup failed", t);

            // Display an error dialog with a proper message and error details
            InformationDialog.showErrorDialog(null, null, Translator.get("startup_error"), null, t);

            // Quit the application
            Application.initiateShutdown();
        }
    }

    private static void setSystemIconsPolicy() {
        String conf = MuConfigurations.getPreferences()
                .getVariable(MuPreference.USE_SYSTEM_FILE_ICONS, MuPreferences.DEFAULT_USE_SYSTEM_FILE_ICONS);
        SystemIconsPolicy policy = SystemIconsPolicy.APPLICATIONS_ONLY;
        for (SystemIconsPolicy value : SystemIconsPolicy.values()) {
            if (value.toString().equals(conf)) {
                policy = value;
                break;
            }
        }
        FileIcons.setSystemIconsPolicy(policy);
    }

    private static void setFileIconsScaleFactor() {
        float conf = MuConfigurations.getPreferences()
                .getVariable(MuPreference.TABLE_ICON_SCALE, MuPreferences.DEFAULT_TABLE_ICON_SCALE);
        // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the
        // configuration file.
        FileIcons.setScaleFactor(Math.max(1.0f, conf));
    }

    private static void configureFilesystems() {
        // Configure the SMB subsystem (backed by jCIFS) to maintain compatibility with SMB servers that don't support
        // NTLM v2 authentication such as Samba 3.0.x, which still is widely used and comes pre-installed on
        // Mac OS X Leopard.
        // Since jCIFS 1.3.0, the default is to use NTLM v2 authentication and extended security.
        // SMBProtocolProvider.setLmCompatibility(MuConfigurations.getPreferences().getVariable(MuPreference.SMB_LM_COMPATIBILITY,
        // MuPreferences.DEFAULT_SMB_LM_COMPATIBILITY));
        // SMBProtocolProvider.setExtendedSecurity(MuConfigurations.getPreferences().getVariable(MuPreference.SMB_USE_EXTENDED_SECURITY,
        // MuPreferences.DEFAULT_SMB_USE_EXTENDED_SECURITY));

        // Use the FTP configuration option that controls whether to force the display of hidden files, or leave it for
        // the servers to decide whether to show them.
        // FTPProtocolProvider.setForceHiddenFilesListing(MuConfigurations.getPreferences().getVariable(MuPreference.LIST_HIDDEN_FILES,
        // MuPreferences.DEFAULT_LIST_HIDDEN_FILES));

        // Use CredentialsManager for file URL authentication
        FileFactory.setDefaultAuthenticator(CredentialsManager.getAuthenticator());
    }

    /**
     * Starts muCommander.
     */
    public static void run(Activator activator) {
        Application.activator = activator;
        new Application().run();
    }

    /**
     * Shuts down muCommander.
     */
    public static void initiateShutdown() {
        LOGGER.info("shutting down");
        try {
            activator.stopAll();
        } catch (Exception e) {
            // should never happen
            LOGGER.error("failed to shut down", e);
        }
    }
}

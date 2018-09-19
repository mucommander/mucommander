/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
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

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.bookmark.file.BookmarkProtocolProvider;
import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.icon.impl.SwingFileIconProvider;
import com.mucommander.commons.file.protocol.ftp.FTPProtocolProvider;
import com.mucommander.commons.file.protocol.smb.SMBProtocolProvider;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.startup.CheckVersionDialog;
import com.mucommander.ui.dialog.startup.InitialSetupDialog;
import com.mucommander.ui.main.SplashScreen;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.main.frame.CommandLineMainFrameBuilder;
import com.mucommander.ui.main.frame.DefaultMainFramesBuilder;
import com.mucommander.ui.main.toolbar.ToolBarIO;
import com.mucommander.utils.MuLogging;

/**
 * muCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line
 * arguments, initialize the whole software and start the main window.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class muCommander implements BundleActivator {
	private static final Logger LOGGER = LoggerFactory.getLogger(muCommander.class);
	
    // - Class fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    private SplashScreen  splashScreen;
    /** Whether or not to display the splashscreen. */
    private boolean       useSplash;
    /** true while the application is launching, false after it has finished launching */
    private static boolean isLaunching = true;
    /** Launch lock. */
    private static final Object LAUNCH_LOCK = new Object();

    /** Whether or not to display verbose error messages. */
    @Parameter(names={"-S", "--silent"}, description="Do not print verbose error messages")
    private boolean silent;
    @Parameter(names={"-v", "--version"}, description="Print the version and exit", help=true)
    private boolean version;
    @Parameter(names={"-h", "--help"}, description="Print the help text and exit", help=true)
    private boolean help;
    // Allows users to tweak how file associations are loaded / saved.
    @Parameter(names={"-a", "--assoc"}, description="Load associations from FILE.")
    private String assoc;
    // Allows users to tweak how bookmarks are loaded / saved.
    @Parameter(names={"-b", "--bookmarks"}, description="Load bookmarks from FILE.")
    private String bookmark;
    // Allows users to tweak how configuration is loaded / saved.
    @Parameter(names={"-c", "--configuration"}, description="Load configuration from FILE")
    private String configuration;
    // Allows users to tweak how command bar configuration is loaded / saved.
    @Parameter(names={"-C", "--commandbar"}, description="Load command bar from FILE.")
    private String commandbar;
    // Allows users to change the extensions folder.
    @Parameter(names={"-e", "--extensions"}, description="Load extensions from FOLDER.")
    private String extensions;
    // Allows users to tweak how custom commands are loaded / saved.
    @Parameter(names={"-f", "--commands"}, description="Load custom commands from FILE.")
    private String commands;
    @Parameter(names={"-w", "--fail-on-warnings"}, description="Quits when a warning is encountered during")
    private boolean fatalWarnings;
    // Allows users to tweak how keymaps are loaded.
    @Parameter(names={"-k", "--keymap"}, description="Load keymap from FILE")
    private String keymap;
    // Allows users to change the preferences folder.
    @Parameter(names={"-p", "--preferences"}, description="Store configuration files in FOLDER")
    private String preferences;
    // Allows users to tweak how shell history is loaded / saved.
    @Parameter(names={"-s", "--shell-history"}, description="Load shell history from FILE")
    private String shellHistory;
    // Allows users to tweak how toolbar configuration are loaded.
    @Parameter(names={"-t", "--toolbar"}, description="Load toolbar from FILE")
    private String toolbar;
    // Allows users to tweak how credentials are loaded.
    @Parameter(names={"-u", "--credentials"}, description="Load credentials from FILE")
    private String credentials;
    @Parameter(description="[folders]")
    private List<String> folders = new ArrayList<>();


    // - Initialization ---------------------------------------------------------
    // --------------------------------------------------------------------------
    public muCommander() {}


    /**
     * This method can be called to wait until the application has been launched. The caller thread will be blocked
     * until the application has been launched.
     * This method will return immediately if the application has already been launched when it is called.
     */
    public static void waitUntilLaunched() {
        LOGGER.debug("called, thread="+Thread.currentThread());
        synchronized(LAUNCH_LOCK) {
            while(isLaunching) {
                try {
                    LOGGER.debug("waiting");
                    LAUNCH_LOCK.wait();
                }
                catch(InterruptedException e) {
                    // will loop
                }
            }
        }
    }

    /**
     * Prints muCommander's version to stdout and exits.
     */
    private static void printVersion() {
        System.out.println(RuntimeConstants.APP_STRING);
        System.out.print("Copyright (C) ");
        System.out.print(RuntimeConstants.COPYRIGHT);
        System.out.println(" Arik Hadas");
        System.out.println("This is free software, distributed under the terms of the GNU General Public License.");
    }

    /**
     * Prints the specified error message to stderr.
     * @param msg       error message to print to stder.
     * @param quit      whether or not to quit after printing the error message.
     * @param exception exception that triggered the error (for verbose output).
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
        if(quit)
            error.append("Warning: ");
        error.append(msg);
        if(!silent && (exception != null)) {
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
        if(quit) {
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
        if(!quit)
            error.append(". Using default values.");

        printError(error.toString(), quit);
    }

    /**
     * Prints the specified startup message.
     */
    private void printStartupMessage(String message) {
        if(useSplash)
            splashScreen.setLoadingMessage(message);

        LOGGER.trace(message);
    }


    // - Boot code --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Method used to migrate commands that used to be defined in the configuration but were moved to <code>commands.xml</code>.
     * @param useName     name of the <code>use custom command</code> configuration variable.
     * @param commandName name of the <code>custom command</code> configuration variable.
     */
    private static void migrateCommand(String useName, String commandName, String alias) {
        String command;

        if(MuConfigurations.getPreferences().getBooleanVariable(useName) && (command = MuConfigurations.getPreferences().getVariable(commandName)) != null) {
            try {
                CommandManager.registerCommand(new Command(alias, command, CommandType.SYSTEM_COMMAND));}
            catch(CommandException e) {
                // Ignore this: the command didn't work in the first place, we might as well get rid of it.
            }
            MuConfigurations.getPreferences().removeVariable(useName);
            MuConfigurations.getPreferences().removeVariable(commandName);
        }
    }

    /**
     * Checks whether a graphics environment is available and exit with an error otherwise.
     */
    private static void checkHeadless() {
        if(GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: no graphical environment detected.");
            System.exit(1);
        }
    }


    /**
     * Main method used to startup muCommander.
     * @param args command line arguments.
     * @throws IOException if an unrecoverable error occurred during startup 
     */
    public static void main(String args[]) throws IOException {
        muCommander mu = new muCommander();
        JCommander jCommander = new JCommander(mu, args);
        if (mu.help) {
            jCommander.setProgramName(muCommander.class.getSimpleName());
            jCommander.usage();
        }
        else if (mu.version) {
            printVersion();
        }
        else
            mu.run();
    }

    private void run() {
        try {
            // Associations handling.
            if (assoc != null) {
                try {com.mucommander.command.CommandManager.setAssociationFile(assoc);}
                catch(Exception e) {printError("Could not set association files", e, fatalWarnings);}
            }

            // Custom commands handling.
            if (commands != null) {
                try {com.mucommander.command.CommandManager.setCommandFile(commands);}
                catch(Exception e) {printError("Could not set commands file", e, fatalWarnings);}
            }

            // Bookmarks handling.
            if (bookmark != null) {
                try {com.mucommander.bookmark.BookmarkManager.setBookmarksFile(bookmark);}
                catch(Exception e) {printError("Could not set bookmarks file", e, fatalWarnings);}
            }

            // Configuration handling.
            if (configuration != null) {
                try {MuConfigurations.setPreferencesFile(configuration);}
                catch(Exception e) {printError("Could not set configuration file", e, fatalWarnings);}
            }

            // Shell history.
            if (shellHistory != null) {
                try {ShellHistoryManager.setHistoryFile(shellHistory);}
                catch(Exception e) {printError("Could not set shell history file", e, fatalWarnings);}
            }

            // Keymap file.
            if (keymap != null) {
                try {com.mucommander.ui.action.ActionKeymapIO.setActionsFile(keymap);}
                catch(Exception e) {printError("Could not set keymap file", e, fatalWarnings);}
            }

            // Toolbar file.
            if (toolbar != null) {
                try {ToolBarIO.setDescriptionFile(toolbar);}
                catch(Exception e) {printError("Could not set keymap file", e, fatalWarnings);}
            }

            // Commandbar file.
            if (commandbar != null) {
                try {CommandBarIO.setDescriptionFile(commandbar);}
                catch(Exception e) {printError("Could not set commandbar description file", e, fatalWarnings);}
            }

            // Credentials file.
            if (credentials != null) {
                try {com.mucommander.auth.CredentialsManager.setCredentialsFile(credentials);}
                catch(Exception e) {printError("Could not set credentials file", e, fatalWarnings);}
            }

            // Preference folder.
            if (preferences != null) {
                try {PlatformManager.setPreferencesFolder(preferences);}
                catch(Exception e) {printError("Could not set preferences folder", e, fatalWarnings);}
            }

            // Extensions folder.
            if (extensions != null) {
                try {ExtensionManager.setExtensionsFolder(extensions);}
                catch(Exception e) {printError("Could not set extensions folder", e, fatalWarnings);}
            }

            // - Configuration init ---------------------------------------
            // ------------------------------------------------------------

            // Ensure that a graphics environment is available, exit otherwise.
            checkHeadless();

            // Attempts to guess whether this is the first time muCommander is booted or not.
            boolean isFirstBoot;
            try {isFirstBoot = !MuConfigurations.isPreferencesFileExists();}
            catch(IOException e) {isFirstBoot = true;}

            // Load snapshot data before loading configuration as until version 0.9 the snapshot properties
            // were stored as preferences so when loading such preferences they could overload snapshot properties
            try {MuConfigurations.loadSnapshot();}
            catch(Exception e) {printFileError("Could not load snapshot", e, fatalWarnings);}
            
            // Configuration needs to be loaded before any sort of GUI creation is performed : under Mac OS X, if we're
            // to use the metal look, we need to know about it right about now.
            try {MuConfigurations.loadPreferences();}
            catch(Exception e) {printFileError("Could not load configuration", e, fatalWarnings);}


            // - Logging configuration ------------------------------------
            // ------------------------------------------------------------
            MuLogging.configureLogging();


            // - MAC OS X specific init -----------------------------------
            // ------------------------------------------------------------
            // If muCommander is running under Mac OS X (how lucky!), add some glue for the main menu bar and other OS X
            // specifics.
            if(OsFamily.MAC_OS_X.isCurrent()) {
                // Use reflection to create an OSXIntegration instance so that ClassLoader
                // doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
                try {
                    Class<?> osxIntegrationClass = Class.forName("com.mucommander.ui.macosx.OSXIntegration");
                    Constructor<?> constructor   = osxIntegrationClass.getConstructor(new Class[]{});
                    constructor.newInstance();
                }
                catch(Exception e) {
                    LOGGER.debug("Exception thrown while initializing Mac OS X integration", e);
                }
            }


            // - muCommander boot -----------------------------------------
            // ------------------------------------------------------------
            // Adds all extensions to the classpath.
            try {
                ExtensionManager.addExtensionsToClasspath();
            }
            catch(Exception e) {
                LOGGER.debug("Failed to add extensions to the classpath", e);
            }

            // This the property is supposed to have the java.net package use the proxy defined in the system settings
            // to establish HTTP connections. This property is supported only under Java 1.5 and up.
            // Note that Mac OS X already uses the system HTTP proxy, with or without this property being set.
            System.setProperty("java.net.useSystemProxies", "true");

            // Shows the splash screen, if enabled in the preferences
            useSplash = MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN);
            if(useSplash) {
                splashScreen = new SplashScreen(RuntimeConstants.VERSION, "Loading preferences...");}

            boolean showSetup;
            showSetup = MuConfigurations.getPreferences().getVariable(MuPreference.THEME_TYPE) == null;

            // Traps VM shutdown
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());

            // Configure filesystems
            configureFilesystems();
            
            // Initializes the desktop.
            try {com.mucommander.desktop.DesktopManager.init(isFirstBoot);}
            catch(Exception e) {printError("Could not initialize desktop", e, true);}

            // Loads dictionary
            printStartupMessage("Loading dictionary...");
            try {com.mucommander.text.Translator.init();}
            catch(Exception e) {printError("Could not load dictionary", e, true);}

            // Loads custom commands
            printStartupMessage("Loading file associations...");
            try {com.mucommander.command.CommandManager.loadCommands();}
            catch(Exception e) {
                printFileError("Could not load custom commands", e, fatalWarnings);
            }

            // Migrates the custom editor and custom viewer if necessary.
            migrateCommand("viewer.use_custom", "viewer.custom_command", CommandManager.VIEWER_ALIAS);
            migrateCommand("editor.use_custom", "editor.custom_command", CommandManager.EDITOR_ALIAS);
            try {CommandManager.writeCommands();}
            catch(Exception e) {
                System.out.println("###############################");
                LOGGER.debug("Caught exception", e);
                // There's really nothing we can do about this...
            }

            try {com.mucommander.command.CommandManager.loadAssociations();}
            catch(Exception e) {
                printFileError("Could not load custom associations", e, fatalWarnings);
            }

            // Loads bookmarks
            printStartupMessage("Loading bookmarks...");
            try {com.mucommander.bookmark.BookmarkManager.loadBookmarks();}
            catch(Exception e) {printFileError("Could not load bookmarks", e, fatalWarnings);}

            // Loads credentials
            printStartupMessage("Loading credentials...");
            try {com.mucommander.auth.CredentialsManager.loadCredentials();}
            catch(Exception e) {printFileError("Could not load credentials", e, fatalWarnings);}

            // Loads shell history
            printStartupMessage("Loading shell history...");
            try {ShellHistoryManager.loadHistory();}
            catch(Exception e) {printFileError("Could not load shell history", e, fatalWarnings);}

            // Inits CustomDateFormat to make sure that its ConfigurationListener is added
            // before FileTable, so CustomDateFormat gets notified of date format changes first
            com.mucommander.text.CustomDateFormat.init();

            // Initialize file icons
            printStartupMessage("Loading icons...");
            // Initialize the SwingFileIconProvider from the main thread, see method Javadoc for an explanation on why we do this now
            SwingFileIconProvider.forceInit();
            // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
            com.mucommander.ui.icon.FileIcons.setScaleFactor(Math.max(1.0f, MuConfigurations.getPreferences().getVariable(MuPreference.TABLE_ICON_SCALE,
                                                                                              MuPreferences.DEFAULT_TABLE_ICON_SCALE)));
            com.mucommander.ui.icon.FileIcons.setSystemIconsPolicy(MuConfigurations.getPreferences().getVariable(MuPreference.USE_SYSTEM_FILE_ICONS, MuPreferences.DEFAULT_USE_SYSTEM_FILE_ICONS));

            // Register actions
            printStartupMessage("Registering actions...");
            ActionManager.registerActions();

            // Loads the ActionKeymap file
            printStartupMessage("Loading actions shortcuts...");
            try {com.mucommander.ui.action.ActionKeymapIO.loadActionKeymap();}
            catch(Exception e) {printFileError("Could not load actions shortcuts", e, fatalWarnings);}

            // Loads the ToolBar's description file
            printStartupMessage("Loading toolbar description...");
            try {ToolBarIO.loadDescriptionFile();}
            catch(Exception e) {printFileError("Could not load toolbar description", e, fatalWarnings);}

            // Loads the CommandBar's description file
            printStartupMessage("Loading command bar description...");
            try {CommandBarIO.loadCommandBar();}
            catch(Exception e) {printFileError("Could not load commandbar description", e, fatalWarnings);}

            // Loads the themes.
            printStartupMessage("Loading theme...");
            com.mucommander.ui.theme.ThemeManager.loadCurrentTheme();

            // Starts Bonjour services discovery (only if enabled in prefs)
            printStartupMessage("Starting Bonjour services discovery...");
            com.mucommander.bonjour.BonjourDirectory.setActive(MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY, MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));

            // Creates the initial main frame using any initial path specified by the command line.
            printStartupMessage("Initializing window...");
            WindowManager.createNewMainFrame(new CommandLineMainFrameBuilder(folders));

            // If no initial path was specified, start a default main window.
            if(WindowManager.getCurrentMainFrame() == null)
                WindowManager.createNewMainFrame(new DefaultMainFramesBuilder());

            // Done launching, wake up threads waiting for the application being launched.
            // Important: this must be done before disposing the splash screen, as this would otherwise create a deadlock
            // if the AWT event thread were waiting in #waitUntilLaunched .
            synchronized(LAUNCH_LOCK) {
                isLaunching = false;
                LAUNCH_LOCK.notifyAll();
            }

            // Enable system notifications, only after MainFrame is created as SystemTrayNotifier needs to retrieve
            // a MainFrame instance
            if(MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS, MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS)) {
                printStartupMessage("Enabling system notifications...");
                if(com.mucommander.ui.notifier.AbstractNotifier.isAvailable())
                    com.mucommander.ui.notifier.AbstractNotifier.getNotifier().setEnabled(true);
            }

            // Dispose splash screen.
            if(splashScreen!=null)
                splashScreen.dispose();

            // Check for newer version unless it was disabled
            if(MuConfigurations.getPreferences().getVariable(MuPreference.CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE))
                new CheckVersionDialog(WindowManager.getCurrentMainFrame(), false);

            // If no theme is configured in the preferences, ask for an initial theme.
            if(showSetup)
                new InitialSetupDialog(WindowManager.getCurrentMainFrame()).showDialog();
        }
        catch(Throwable t) {
            // Startup failed, dispose the splash screen
            if(splashScreen!=null)
                splashScreen.dispose();

            LOGGER.error("Startup failed", t);
            
            // Display an error dialog with a proper message and error details
            InformationDialog.showErrorDialog(null, null, Translator.get("startup_error"), null, t);

            // Quit the application
            WindowManager.quit();
        }
    }

    private static void configureFilesystems() {
        // Configure the SMB subsystem (backed by jCIFS) to maintain compatibility with SMB servers that don't support
        // NTLM v2 authentication such as Samba 3.0.x, which still is widely used and comes pre-installed on
        // Mac OS X Leopard.
        // Since jCIFS 1.3.0, the default is to use NTLM v2 authentication and extended security.
        SMBProtocolProvider.setLmCompatibility(MuConfigurations.getPreferences().getVariable(MuPreference.SMB_LM_COMPATIBILITY, MuPreferences.DEFAULT_SMB_LM_COMPATIBILITY));
        SMBProtocolProvider.setExtendedSecurity(MuConfigurations.getPreferences().getVariable(MuPreference.SMB_USE_EXTENDED_SECURITY, MuPreferences.DEFAULT_SMB_USE_EXTENDED_SECURITY));

        // Use the FTP configuration option that controls whether to force the display of hidden files, or leave it for
        // the servers to decide whether to show them.
        FTPProtocolProvider.setForceHiddenFilesListing(MuConfigurations.getPreferences().getVariable(MuPreference.LIST_HIDDEN_FILES, MuPreferences.DEFAULT_LIST_HIDDEN_FILES));        
        
        // Use CredentialsManager for file URL authentication
        FileFactory.setDefaultAuthenticator(CredentialsManager.getAuthenticator());

        // Register the application-specific 'bookmark' protocol.
        FileFactory.registerProtocol(BookmarkProtocolProvider.BOOKMARK, new com.mucommander.bookmark.file.BookmarkProtocolProvider());
    }


	@Override
	public void start(BundleContext context) throws Exception {
		muCommander mu = new muCommander();
		mu.run();
	}


	@Override
	public void stop(BundleContext context) throws Exception {
	}
}

package com.mucommander;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.CheckVersionDialog;
import com.mucommander.ui.InitialSetupDialog;
import com.mucommander.ui.SplashScreen;
import com.mucommander.ui.WindowManager;

import java.lang.reflect.Constructor;

/**
 * muCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line
 * arguments, initialise the whole software and start the main window.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Launcher {
    // - Commandline handling methods -------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Prints muCommander's command line usage and exits.
     */
    private static void printUsage() {
        System.out.println("Usage: mucommander [options] [folders]");
        System.out.println("Options:");

        // Allows users to tweak how file associations are loaded / saved.
        System.out.println(" -a FILE, --assoc FILE             Load associations from FILE.");

        // Allows users to tweak how bookmarks are loaded / saved.
        System.out.println(" -b FILE, --bookmarks FILE         Load bookmarks from FILE.");

        // Allows users to tweak how configuration is loaded / saved.
        System.out.println(" -c FILE, --configuration FILE     Load configuration from FILE");

        // Allows users to tweak how command bar configuration is loaded / saved.
        System.out.println(" -C FILE, --commandbar FILE        Load command bar from FILE");

        // Allows users to tweak how custom commands are loaded / saved.
        System.out.println(" -f FILE, --commands FILE          Load custom commands from FILE.");

        // Ignore warnings.
        System.out.println(" -i, --ignore-warnings             Do not fail on warnings (default).");

        // Allows users to tweak how keymaps are loaded.
        System.out.println(" -k FILE, --keymap FILE            Load keymap from FILE");

        // If debug is turned on, -n and -d are used to control whether debug
        // text is printed out or not.
        if(Debug.ON) {
            System.out.println(" -n, --no-debug                    Disable debug output to stdout");
            System.out.println(" -d, --debug                       Enable debug output to stdout (default)");
        }

        // Allows users to change the preferences folder.
        System.out.println(" -p FOLDER, --preferences FOLDER   Store configuration files in FOLDER.");

        // muCommander will not print verbose error messages.
        System.out.println(" -S, --silent                          Do not print verbose error messages");

        // Allows users to tweak how shell history is loaded / saved.
        System.out.println(" -s FILE, --shell-history FILE     Load shell history from FILE");

        // Allows users to tweak how toolbar configuration are loaded.
        System.out.println(" -t FILE, --toolbar FILE           Load toolbar from FILE");

        // Allows users to tweak how credentials are loaded.
        System.out.println(" -u FILE, --credentials FILE       Load credentials from FILE");

        // Text commands.
        System.out.println(" -h, --help                        Print the help text and exit");
        System.out.println(" -v, --version                     Print the version and exit");

        // muCommander will print verbose boot error messages.
        System.out.println(" -V, --verbose                     Print verbose error messages (default)");

        // Pedantic mode.
        System.out.println(" -w, --fail-on-warnings            Quits when a warning is encountered during");
        System.out.println("                                   the boot process.");
        System.exit(0);
    }

    /**
     * Prints muCommander's version to stdout and exits.
     */
    private static void printVersion() {
        System.out.println(RuntimeConstants.APP_STRING);
        System.out.print("Copyright (c) ");
        System.out.print(RuntimeConstants.COPYRIGHT);
        System.out.println(" Maxence Bernard");
        System.out.println("All rights reserved.");
        System.out.println("There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A");
        System.out.println("PARTICULAR PURPOSE.");
        System.exit(0);
    }

    /**
     * Prints the specified error message to stderr.
     * @param msg       error message to print to stder.
     * @param quit      whether or not to quit after printing the error message.
     * @param exception exception that triggered the error (for verbose output).
     * @param verbose   whether or not to display verbose error output.
     */
    private static void printError(String msg, Exception exception, boolean verbose, boolean quit) {
        printError(createErrorMessage(msg, exception, verbose, quit).toString(), quit);
    }

    /**
     * Creates an error message.
     */
    private static StringBuffer createErrorMessage(String msg, Exception exception, boolean verbose, boolean quit) {
        StringBuffer error;

        error = new StringBuffer();
        if(quit)
            error.append("Warning: ");
        error.append(msg);
        if(verbose && (exception != null))
            error.append(exception.getMessage());

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
    private static void printFileError(String msg, Exception exception, boolean verbose, boolean quit) {
        StringBuffer error;

        error = createErrorMessage(msg, exception, verbose, quit);
        if(!quit)
            error.append(". Using default values.");

        printError(error.toString(), quit);
    }


    // - Boot code --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Main method used to startup muCommander.
     */
    public static void main(String args[]) {
        SplashScreen splashScreen;   // Splashscreen instance.
        int          i;              // Index in the command line arguments.
        boolean      showSetup;      // Whether or not to show the setup dialog.
        boolean      fatalWarnings;  // Whether or not to ignore warnings when booting.
        boolean      verbose;        // Whether or not to display verbose error messages.


        // - Command line parsing -------------------------------------
        // ------------------------------------------------------------
        fatalWarnings = false;
        verbose       = true;
        for(i = 0; i < args.length; i++) {
            // Print version.
            if(args[i].equals("-v") || args[i].equals("--version"))
                printVersion();

            // Print help.
            else if(args[i].equals("-h") || args[i].equals("--help"))
                printUsage();

            // Associations handling.
            else if(args[i].equals("-a") || args[i].equals("--assoc")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.command.CommandManager.setAssociationFile(args[++i]);}
                catch(Exception e) {printError("Could not set association files", e, verbose, fatalWarnings);}
            }

            // Custom commands handling.
            else if(args[i].equals("-f") || args[i].equals("--commands")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.command.CommandManager.setCommandFile(args[++i]);}
                catch(Exception e) {printError("Could not set commands file", e, verbose, fatalWarnings);}
            }

            // Bookmarks handling.
            else if(args[i].equals("-b") || args[i].equals("--bookmarks")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.bookmark.BookmarkManager.setBookmarksFile(args[++i]);}
                catch(Exception e) {printError("Could not set bookmarks file", e, verbose, fatalWarnings);}
            }

            // Configuration handling.
            else if(args[i].equals("-c") || args[i].equals("--configuration")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {ConfigurationManager.setConfigurationFile(args[++i]);}
                catch(Exception e) {printError("Could not set configuration file", e, verbose, fatalWarnings);}
            }

            // Shell history.
            else if(args[i].equals("-s") || args[i].equals("--shell-history")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {ShellHistoryManager.setHistoryFile(args[++i]);}
                catch(Exception e) {printError("Could not set shell history file", e, verbose, fatalWarnings);}
            }

            // Keymap file.
            else if(args[i].equals("-k") || args[i].equals("--keymap")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.ui.action.ActionKeymap.setActionKeyMapFile(args[++i]);}
                catch(Exception e) {printError("Could not set keymap file", e, verbose, fatalWarnings);}
            }

            // Toolbar file.
            else if(args[i].equals("-t") || args[i].equals("--toolbar")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.ui.ToolBar.setDescriptionFile(args[++i]);}
                catch(Exception e) {printError("Could not set keymap file", e, verbose, fatalWarnings);}
            }

            // Commandbar file.
            else if(args[i].equals("-C") || args[i].equals("--commandbar")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.ui.CommandBar.setDescriptionFile(args[++i]);}
                catch(Exception e) {printError("Could not set commandbar description file", e, verbose, fatalWarnings);}
            }

            // Credentials file.
            else if(args[i].equals("-U") || args[i].equals("--credentials")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i], null, false, true);
                try {com.mucommander.auth.CredentialsManager.setCredentialsFile(args[++i]);}
                catch(Exception e) {printError("Could not set credentials file", e, verbose, fatalWarnings);}
            }

            // Debug options.
            else if(Debug.ON && (args[i].equals("-n") || args[i].equals("--no-debug")))
                Debug.setEnabled(false);
            else if(Debug.ON && (args[i].equals("-d") || args[i].equals("--debug")))
                Debug.setEnabled(true);

            // Preference folder.
            else if((args[i].equals("-p") || args[i].equals("--preferences"))) {
                if(i >= args.length - 1)
                    printError("Missing FOLDER parameter to " + args[i], null, false, true);
                try {PlatformManager.setPreferencesFolder(new java.io.File(args[++i]));}
                catch(Exception e) {printError("Could not set preferences folder", e, verbose, fatalWarnings);}
            }

            // Ignore warnings.
            else if(args[i].equals("-i") || args[i].equals("--ignore-warnings"))
                fatalWarnings = false;

            // Fail on warnings.
            else if(args[i].equals("-w") || args[i].equals("--fail-on-warnings"))
                fatalWarnings = true;

            // Silent mode.
            else if(args[i].equals("-S") || args[i].equals("--silent"))
                verbose = false;

            // Verbose mode.
            else if(args[i].equals("-V") || args[i].equals("--verbose"))
                verbose = true;

            // Illegal argument.
            else
                break;
        }



        // - MAC OS init ----------------------------------------------
        // ------------------------------------------------------------

        // If muCommander is running under Mac OS X (how lucky!), add some
        // glue for the main menu bar and other OS X specifics.
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
            // Configuration needs to be loaded before any sort of GUI creation
            // is performed - if we're to use the metal look, we need to know about
            // it right about now.
            try {ConfigurationManager.loadConfiguration();}
            catch(Exception e) {printFileError("Could not load configuration", e, verbose, fatalWarnings);}

            // Use reflection to create an OSXIntegration instance so that ClassLoader
            // doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
            try {
                Class osxIntegrationClass = Class.forName("com.mucommander.ui.macosx.OSXIntegration");
                Constructor constructor   = osxIntegrationClass.getConstructor(new Class[]{});
                constructor.newInstance(new Object[]{});
            }
            catch(Exception e) {if(Debug.ON) Debug.trace("Exception thrown while initializing Mac OS X integration");}
        }



        // - muCommander boot -----------------------------------------
        // ------------------------------------------------------------
        // Shows the splash screen.
        splashScreen = new SplashScreen(RuntimeConstants.VERSION, "Loading preferences...");

        // If we're not running under OS_X, preferences haven't been loaded yet.
        if(PlatformManager.OS_FAMILY != PlatformManager.MAC_OS_X) {
            try {ConfigurationManager.loadConfiguration();}
            catch(Exception e) {printFileError("Could not load configuration", e, verbose, fatalWarnings);}
        }

        showSetup = ConfigurationManager.getVariable(ConfigurationVariables.THEME_TYPE) == null;

        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
        // Loads dictionary
        splashScreen.setLoadingMessage("Loading dictionary...");
        try {com.mucommander.text.Translator.loadDictionaryFile();}
        catch(Exception e) {printError("Could not load dictionary", e, verbose, true);}

        // Loads the file associations
        splashScreen.setLoadingMessage("Loading file associations...");
        try {com.mucommander.command.CommandManager.loadCommands();}
        catch(Exception e) {printFileError("Could not load custom commands", e, verbose, fatalWarnings);}
        try {com.mucommander.command.CommandManager.loadAssociations();}
        catch(Exception e) {printFileError("Could not load custom associations", e, verbose, fatalWarnings);}

        // Loads bookmarks
        splashScreen.setLoadingMessage("Loading bookmarks...");
        try {com.mucommander.bookmark.BookmarkManager.loadBookmarks();}
        catch(Exception e) {printFileError("Could not load bookmarks", e, verbose, fatalWarnings);}

        // Loads credentials
        splashScreen.setLoadingMessage("Loading credentials...");
        try {com.mucommander.auth.CredentialsManager.loadCredentials();}
        catch(Exception e) {printFileError("Could not load credentials", e, verbose, fatalWarnings);}
        
        // Loads shell history
        splashScreen.setLoadingMessage("Loading shell history...");
        try {ShellHistoryManager.loadHistory();}
        catch(Exception e) {printFileError("Could not load shell history", e, verbose, fatalWarnings);}

        // Inits CustomDateFormat to make sure that its ConfigurationListener is added
        // before FileTable, so CustomDateFormat gets notified of date format changes first
        com.mucommander.text.CustomDateFormat.init();

        // Preload icons
        splashScreen.setLoadingMessage("Loading icons...");
        com.mucommander.ui.icon.FileIcons.init();

        // Loads the ActionKeymap file
        splashScreen.setLoadingMessage("Loading action keymap...");
        try {com.mucommander.ui.action.ActionKeymap.loadActionKeyMap();}
        catch(Exception e) {printFileError("Could not load action keyamp", e, verbose, fatalWarnings);}

        // Loads the ToolBar's description file
        splashScreen.setLoadingMessage("Loading toolbar description...");
        try {com.mucommander.ui.ToolBar.loadDescriptionFile();}
        catch(Exception e) {printFileError("Could not load toolbar description", e, verbose, fatalWarnings);}

        // Loads the CommandBar's description file
        splashScreen.setLoadingMessage("Loading command bar description...");
        try {com.mucommander.ui.CommandBar.loadDescriptionFile();}
        catch(Exception e) {printFileError("Could not load commandbar description", e, verbose, fatalWarnings);}

        // Loads the themes.
        splashScreen.setLoadingMessage("Loading theme...");
        com.mucommander.ui.theme.ThemeManager.loadCurrentTheme();

        // Starts Bonjour services discovery (only if enabled in prefs)
        splashScreen.setLoadingMessage("Starting Bonjour services discovery...");
        com.mucommander.bonjour.BonjourDirectory.setActive(ConfigurationManager.getVariableBoolean(ConfigurationVariables.ENABLE_BONJOUR_DISCOVERY, ConfigurationVariables.DEFAULT_ENABLE_BONJOUR_DISCOVERY));

        // Try and enable Growl support, only if OS is Mac OS X
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
            splashScreen.setLoadingMessage("Initializing Growl support...");
            com.mucommander.ui.macosx.GrowlSupport.init();
        }

        // Creates the initial main frame using any initial path specified by the command line.
        splashScreen.setLoadingMessage("Initializing window...");
        for(; i < args.length; i += 2) {
            if(i < args.length - 1)
                WindowManager.createNewMainFrame(args[i], args[i + 1]);
            else
                WindowManager.createNewMainFrame(args[i], null);
        }

        // If no initial path was specified, start a default main window.
        if(WindowManager.getCurrentMainFrame() == null)
    	    WindowManager.createNewMainFrame();

        // Dispose splash screen.
        splashScreen.dispose();

        // Check for newer version unless it was disabled
        if(ConfigurationManager.getVariableBoolean(ConfigurationVariables.CHECK_FOR_UPDATE, ConfigurationVariables.DEFAULT_CHECK_FOR_UPDATE))
            new CheckVersionDialog(WindowManager.getCurrentMainFrame(), false);

        // If no theme is configured in the preferences, ask for an initial theme.
        if(showSetup)
            new InitialSetupDialog(WindowManager.getCurrentMainFrame()).showDialog();
    }
}

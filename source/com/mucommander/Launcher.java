package com.mucommander;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.CheckVersionDialog;
import com.mucommander.ui.SplashScreen;
import com.mucommander.ui.WindowManager;

import java.lang.reflect.Constructor;

/**
 * muCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line
 * arguments, initialise the whole software and start the main window.
 * </p>
 * <p>
 * Valid command line options are:<br/>
 * - <code>-b FILE</code>, <code>--bookmarks FILE</code>: load bookmarks from <code>FILE</code>.<br/>
 * - <code>-c FILE</code>, <code>--configuration FILE</code>: load configuration
 *   from <code>FILE</code>.<br/>
 * - <code>-h</code>, <code>--help</code>: print command line argument help.<br/>
 * - <code>-v</code>, <code>--version</code>: print muCommander's version.<br/>
 * Debug versions of the application also accept two additional parameters:<br/>
 * - <code>-n</code>, <code>--no-debug</code>: silence debug output.<br/>
 * - <code>-d</code>, <code>--debug</code>: enable debug output.<br/>
 * Any argument that follows the last command line option will be interpreted as an URL which
 * muCommander will try to open in its main frame.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Launcher {
    // - Commandline handling methods -------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Prints muCommander's command line usage and exits.
     */
    private static final void printUsage() {
        System.out.println("Usage: mucommander [options] [folders]");
        System.out.println("Options:");

        // Allows users to tweak how bookmarks are loaded / saved.
        System.out.println(" -b FILE, --bookmarks FILE       Load bookmarks from FILE.");

        // Allows users to tweak how configuration is loaded / saved.
        System.out.println(" -c FILE, --configuration FILE   Load configuration from FILE");

        // If debug is turned on, -n and -d are used to control whether debug
        // text is printed out or not.
        if(Debug.ON) {
            System.out.println(" -n, --no-debug                  Disable debug output to stdout");
            System.out.println(" -d, --debug                     Enable debug output to stdout (default)");
        }
        // Allows users to tweak how shell history is loaded / saved.
        System.out.println(" -s FILE, --shell-history FILE   Load shell history from FILE");

        // Text commands.
        System.out.println(" -h, --help                      Print the help text and exit");
        System.out.println(" -v, --version                   Print the version and exit");
        System.exit(0);
    }

    /**
     * Prints muCommander's version to stdout and exits.
     */
    private static final void printVersion() {
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
     * Prints the specified error message to stderr and exits with an error code.
     * @param msg error message to print to stder.
     */
    private static final void printError(String msg) {
        System.err.println(msg);
        System.err.println("See mucommander --help for more information.");
        System.exit(1);
    }



    // - Boot code --------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Main method used to startup muCommander.
     */
    public static void main(String args[]) {
        SplashScreen splashScreen; // Splashscreen instance.
        int          i;            // Index in the command line arguments.

        // - Command line parsing -------------------------------------
        // ------------------------------------------------------------
        for(i = 0; i < args.length; i++) {
            // Print version.
            if(args[i].equals("-v") || args[i].equals("--version"))
                printVersion();

            // Print help.
            else if(args[i].equals("-h") || args[i].equals("--help"))
                printUsage();

            // Bookmarks handling.
            else if(args[i].equals("-b") || args[i].equals("--bookmarks")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i]);
                com.mucommander.bookmark.BookmarkManager.setBookmarksFile(args[++i]);
            }

            // Configuration handling.
            else if(args[i].equals("-c") || args[i].equals("--configuration")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i]);
                ConfigurationManager.setConfigurationFile(args[++i]);
            }

            // Shell history.
            else if(args[i].equals("-s") || args[i].equals("--shell-history")) {
                if(i >= args.length - 1)
                    printError("Missing FILE parameter to " + args[i]);
                ShellHistoryManager.setHistoryFile(args[++i]);
            }

            // Debug options.
            else if(Debug.ON && (args[i].equals("-n") || args[i].equals("--no-debug")))
                Debug.setEnabled(false);
            else if(Debug.ON && (args[i].equals("-d") || args[i].equals("--debug")))
                Debug.setEnabled(true);

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
            ConfigurationManager.loadConfiguration();

            // Use reflection to create an OSXIntegration instance so that ClassLoader
            // doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
            try {
                Class osxIntegrationClass = Class.forName("com.mucommander.ui.macosx.OSXIntegration");
                Constructor constructor = osxIntegrationClass.getConstructor(new Class[]{});
                constructor.newInstance(new Object[]{});
            }
            catch(Exception e) {
                if(Debug.ON) Debug.trace("Exception thrown while initializing Mac OS X integration");
            }
        }

        // - muCommander boot -----------------------------------------
        // ------------------------------------------------------------
        // Shows the splash screen.
        splashScreen = new SplashScreen(RuntimeConstants.VERSION, "Loading preferences...");

        // If we're not running under OS_X, preferences haven't been loaded yet.
        if(PlatformManager.OS_FAMILY != PlatformManager.MAC_OS_X)
            ConfigurationManager.loadConfiguration();

        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
        // Loads dictionary
        splashScreen.setLoadingMessage("Loading dictionary...");
        com.mucommander.text.Translator.loadDictionaryFile();

        // Loads bookmarks
        splashScreen.setLoadingMessage("Loading bookmarks...");
        com.mucommander.bookmark.BookmarkManager.loadBookmarks();

        // Loads shell history
        splashScreen.setLoadingMessage("Loading shell history...");
        ShellHistoryManager.loadHistory();

        // Inits CustomDateFormat to make sure that its ConfigurationListener is added
        // before FileTable, so CustomDateFormat gets notified of date format changes first
        com.mucommander.text.CustomDateFormat.init();

        // Preload icons
        splashScreen.setLoadingMessage("Loading icons...");
        com.mucommander.ui.icon.FileIcons.init();

        // Loads the ActionKeymap file
        splashScreen.setLoadingMessage("Loading action keymap...");
        com.mucommander.ui.action.ActionKeymap.loadActionKeyMap();

        // Loads the ToolBar's description file
        splashScreen.setLoadingMessage("Loading toolbar description...");
        com.mucommander.ui.ToolBar.loadDescription();

        // Loads the CommandBar's description file
        splashScreen.setLoadingMessage("Loading command bar description...");
        com.mucommander.ui.CommandBar.loadDescription();

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

        // Check for newer version unless it was disabled
        if(ConfigurationManager.getVariableBoolean("prefs.check_for_updates_on_startup", true))
            new CheckVersionDialog(WindowManager.getCurrentMainFrame(), false);


        // Silence jCIFS's output if not in debug mode
        // To quote jCIFS's documentation : "0 - No log messages are printed -- not even crticial exceptions."
        if(!Debug.ON)
            System.setProperty("jcifs.util.loglevel", "0");
		
        // Dispose splash screen
        splashScreen.dispose();
    }
}

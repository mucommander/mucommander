package com.mucommander;

import com.mucommander.ui.*;
import com.mucommander.conf.*;
import com.mucommander.*;
import com.mucommander.ui.*;
import com.mucommander.file.*;
import java.lang.reflect.*;
import java.io.*;

/**
 * muCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line
 * arguments, initialise the whole software and start the main window.
 * </p>
 * <p>
 * For a list of legal command line arguments, use <code>mucommander -h</code>.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Launcher {
    // - Misc. constants --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** muCommander app string */
    public final static String MUCOMMANDER_APP_STRING = "muCommander v" + RuntimeConstants.MUCOMMANDER_VERSION;

    /** Custom user agent for HTTP requests */
    public final static String USER_AGENT             = MUCOMMANDER_APP_STRING  + " (Java "+System.getProperty("java.vm.version")
                                                        + "; " + System.getProperty("os.name") + " " + 
                                                        System.getProperty("os.version") + " " + System.getProperty("os.arch") + ")";



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

        // Text commands.
        System.out.println(" -h, --help                      Print the help text and exit");
        System.out.println(" -v, --version                   Print the version and exit");
        System.exit(0);
    }

    /**
     * Prints muCommander's version to stdout and exits.
     */
    private static final void printVersion() {
        // NOTE: this is a sub-optimal way of doing things. Someone's bound to forget
        // to update the copyright at some point, which might result in some confusion.
        // Updating the copyright notice to include today's year might be a bit dodgy, though.
        System.out.print("muCommander ");
        System.out.println(RuntimeConstants.MUCOMMANDER_VERSION);
        System.out.print("Copyright (c) ");
        System.out.print(RuntimeConstants.MUCOMMANDER_COPYRIGHT);
        System.out.println(" Maxence Bernard");
        System.out.println("All rights reserved.");
        System.out.println("There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A");
        System.out.println("PARTICULAR PURPOSE.");
        System.exit(0);
    }

    /**
     * Prints the specified error message to stderr and exits.
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

            // Debug options.
            else if(Debug.ON && args[i].equals("-n") || args[i].equals("--no-debug"))
                Debug.setEnabled(false);
            else if(Debug.ON && args[i].equals("-d") || args[i].equals("--debug"))
                Debug.setEnabled(true);

            // Illegal argument.
            else
                break;
        }

        // - MAC OS init ----------------------------------------------
        // ------------------------------------------------------------

        // If muCommander is running under Mac OS X (how lucky!), add some
        // glue for the main menu bar and other OS X specifics.
        if(PlatformManager.getOSFamily()==PlatformManager.MAC_OS_X) {
            // Configuration needs to be loaded before any sort of GUI creation
            // is performed - if we're to use the metal look, we need to know about
            // it right about now.
            ConfigurationManager.loadConfiguration();

            // Use reflection to create a FinderIntegration class so that ClassLoader
            // doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
            try {
                Class finderIntegrationClass = Class.forName("com.mucommander.ui.macosx.FinderIntegration");
                Constructor constructor = finderIntegrationClass.getConstructor(new Class[]{});
                constructor.newInstance(new Object[]{});
            }
            catch(Exception e) {
                if(Debug.ON) Debug.trace("Launcher.init: exception thrown while initializing Mac Finder integration");
            }
        }

        // - muCommander boot -----------------------------------------
        // ------------------------------------------------------------
        // Shows the splash screen.
        splashScreen = new SplashScreen(RuntimeConstants.MUCOMMANDER_VERSION, "Loading preferences...");

        // If we're not running under OS_X, preferences haven't been loaded yet.
        if(PlatformManager.getOSFamily() != PlatformManager.MAC_OS_X)
            ConfigurationManager.loadConfiguration();

        // Checks that preferences folder exists and if not, creates it.
        PlatformManager.checkCreatePreferencesFolder();

        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
        // Loads bookmarks
        splashScreen.setLoadingMessage("Loading bookmarks...");
        com.mucommander.bookmark.BookmarkManager.loadBookmarks();

        // Loads dictionary
        splashScreen.setLoadingMessage("Loading dictionary...");
        com.mucommander.text.Translator.init();

        // Inits CustomDateFormat to make sure that its ConfigurationListener is added
        // before FileTable, so CustomDateFormat gets notified of date format changes first
        com.mucommander.text.CustomDateFormat.init();

        // Preload icons
        splashScreen.setLoadingMessage("Loading icons...");
        com.mucommander.ui.icon.FileIcons.init();
        com.mucommander.ui.ToolBar.init();
        com.mucommander.ui.CommandBar.init();

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

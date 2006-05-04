package com.mucommander;

import com.mucommander.ui.*;
import com.mucommander.conf.*;
import com.mucommander.*;
import com.mucommander.ui.*;
import java.lang.reflect.*;

/**
 * muCommander launcher: displays a splash screen and starts up the app
 * through the main() method.
 *
 * @author Maxence Bernard
 */
public class Launcher {

    /** Version string */
    public final static String MUCOMMANDER_VERSION = "0.8 beta3";
    //	public final static String MUCOMMANDER_VERSION = "0.8 beta2 r2";

    /** Version string */
    public final static String SHORT_VERSION_STRING = "0.8 beta3";

    /** muCommander app string */
    public final static String MUCOMMANDER_APP_STRING = "muCommander v"+SHORT_VERSION_STRING;

    /** Custom user agent for HTTP requests */
    public final static String USER_AGENT = MUCOMMANDER_APP_STRING
        +" (Java "+System.getProperty("java.vm.version")
        +"; "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch")+")";


    // - Commandline handling methods -------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Prints muCommander's command line usage and exits.
     */
    private static final void printUsage() {
        System.out.println("Usage: mucommander [options]");
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
        System.out.println(MUCOMMANDER_VERSION);
        System.out.println("Copyright (c) 2002-2006 Maxence Bernard");
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

    /**
     * Main method used to startup muCommander.
     */
    public static void main(String args[]) {
        // Parses command line arguments.
        for(int i = 0; i < args.length; i++) {
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
                printError("Illegal argument: " + args[i]);
        }

        new Launcher();
    }



    // - muCommander boot -------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * No-arg private constructor.
     */
    private Launcher() {

        //////////////////////////////////////////////////////////////////////////
        // Important: all JAR resource files need to be loaded from the main    //
        //  thread (here may be a good place), because of some weirdness of     //
        //  JNLP/Webstart's ClassLoader.                                        //
        //////////////////////////////////////////////////////////////////////////

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

        // Show up the splash screen
        SplashScreen splashScreen = new SplashScreen(SHORT_VERSION_STRING, "Loading preferences...");

        // If we're not running under OS_X, preferences haven't been loaded yet.
        if(PlatformManager.getOSFamily() != PlatformManager.MAC_OS_X)
            ConfigurationManager.loadConfiguration();

        // Checks that preferences folder exists and if not, creates it.
        PlatformManager.checkCreatePreferencesFolder();

        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
        //		// Turns on dynamic layout
        //		setDynamicLayout(true);

        splashScreen.setLoadingMessage("Loading bookmarks...");

        // Loads bookmarks
        com.mucommander.bookmark.BookmarkManager.loadBookmarks();

        splashScreen.setLoadingMessage("Loading dictionary...");

        // Loads dictionary
        com.mucommander.text.Translator.init();

        // Inits CustomDateFormat to make sure that its ConfigurationListener is added
        // before FileTable, so CustomDateFormat gets notified of date format changes first
        com.mucommander.text.CustomDateFormat.init();

        splashScreen.setLoadingMessage("Loading icons...");

        // Preload icons
        com.mucommander.ui.icon.FileIcons.init();
        com.mucommander.ui.ToolBar.init();
        com.mucommander.ui.CommandBar.init();

        splashScreen.setLoadingMessage("Initializing window...");

        // Initialize WindowManager and create a new window
        WindowManager.checkInit();
		
        // Check for newer version unless it was disabled
        if(ConfigurationManager.getVariableBoolean("prefs.check_for_updates_on_startup", true))
            new CheckVersionDialog(WindowManager.getInstance().getCurrentMainFrame(), false);
		
        // Silence jCIFS's output if not in debug mode
        // To quote jCIFS's documentation : "0 - No log messages are printed -- not even crticial exceptions."
        if(!Debug.ON)
            System.setProperty("jcifs.util.loglevel", "0");
		
        // Dispose splash screen
        splashScreen.dispose();
    }

	
    //	/**
    //	 * Turns on or off dynamic layout which updates layout while resizing a frame. This
    //	 * is a 1.4 only feature and may not be supported by the underlying OS and window manager.
    //	 */
    //	public static void setDynamicLayout(boolean b) {
    //		try {
    //			java.awt.Toolkit.getDefaultToolkit().setDynamicLayout(b);
    //		}
    //		catch(NoSuchMethodError e) {
    //		}
    //	}

}

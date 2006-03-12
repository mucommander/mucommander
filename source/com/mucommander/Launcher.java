package com.mucommander;

import com.mucommander.ui.WindowManager;
import com.mucommander.ui.CheckVersionDialog;

import com.mucommander.conf.ConfigurationManager;

import com.mucommander.Debug;

import com.mucommander.ui.SplashScreen;

import java.lang.reflect.*;


/**
 * muCommander launcher: displays a splash screen and starts up the app
 * through the main() method.
 *
 * @author Maxence Bernard
 */
public class Launcher {

	/** Version string */
	public final static String MUCOMMANDER_VERSION = "0.8 beta2 r2";

	/** Version string */
	public final static String SHORT_VERSION_STRING = "0.8 beta2";

	/** muCommander app string */
	public final static String MUCOMMANDER_APP_STRING = "muCommander v"+SHORT_VERSION_STRING;

	/** Custom user agent for HTTP requests */
	public final static String USER_AGENT = MUCOMMANDER_APP_STRING
		+" (Java "+System.getProperty("java.vm.version")
		+"; "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch")+")";
	
	
	/**
	 * Main method used to startup muCommander.
	 */
	public static void main(String args[]) {
		new Launcher();
	}

	
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

		// Triggers initialization of ConfigurationManager (preferences file parsing and loading)
		ConfigurationManager.init();

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
		com.mucommander.ui.FileIcons.init();
		com.mucommander.ui.ToolBar.init();
		com.mucommander.ui.CommandBar.init();

		splashScreen.setLoadingMessage("Initializing window...");

		// Initialize WindowManager and create a new window
		WindowManager.checkInit();
		
        // Check for newer version unless it was disabled
        if(ConfigurationManager.getVariable("prefs.check_for_updates_on_startup", "true").equals("true"))
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
package com.mucommander;

import com.mucommander.ui.WindowManager;
import com.mucommander.ui.CheckVersionDialog;

//import com.mucommander.ui.macosx.FinderIntegration;
import java.lang.reflect.*;

import com.mucommander.conf.ConfigurationManager;

import com.mucommander.Debug;

import javax.swing.*;
import javax.swing.*;

import java.awt.*;

import java.net.URL;


/**
 * muCommander launcher: displays a splash screen and starts up the app
 * when triggered by its main() method.
 *
 * @author Maxence Bernard
 */
public class Launcher {

	/** Version string */
	public final static String MUCOMMANDER_VERSION = "0.7_01";

	/** muCommander app string */
	public final static String MUCOMMANDER_APP_STRING = "muCommander v"+MUCOMMANDER_VERSION;

	/** Custom user agent for HTTP requests */
	public final static String USER_AGENT = MUCOMMANDER_APP_STRING
		+" (Java "+System.getProperty("java.vm.version")
		+"; "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch")+")";
	
	/** Launcher's sole instance */
	private static Launcher launcher;

	
	/**
	 * Main method used to startup muCommander.
	 */
	public static void main(String args[]) {
		launcher = new Launcher();
	}

	
	/**
	 * No-arg private constructor.
	 */
	private Launcher() {
		// If muCommander is running under Mac OS X (how lucky!), add some
		// glue for the main menu bar.
		if(PlatformManager.getOSFamily()==PlatformManager.MAC_OS_X) {
			// Use reflection to create a FinderIntegration class so that ClassLoader
			// doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
			try {
//				FinderIntegration finderIntegration = new FinderIntegration();
				Class finderIntegrationClass = Class.forName("com.mucommander.ui.macosx.FinderIntegration");
				Constructor constructor = finderIntegrationClass.getConstructor(new Class[]{});
				constructor.newInstance(new Object[]{});
			}
			catch(Exception e) {
				if(Debug.ON)
					System.out.println("Launcher.init: exception thrown while initializing Mac Finder integration");
			}
		}

		// Show splash screen before anything else
		JWindow splashScreen = showSplashScreen();

//		// Turns on dynamic layout
//		setDynamicLayout(true);

		// Traps VM shutdown
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

		//////////////////////////////////////////////////////////////////////
		// Important: all JAR resource files need to be loaded from main    //
		//  thread (here may be a good place), because of some weirdness of //
		//  JNLP/Webstart's ClassLoader.                                    //
		//////////////////////////////////////////////////////////////////////

		// Loads dictionary
		com.mucommander.text.Translator.init();

		// Initialize WindowManager
		WindowManager.checkInit();
		
        // Check for newer version unless it was disabled
        if(ConfigurationManager.getVariable("prefs.check_for_updates_on_startup", "true").equals("true"))
            new CheckVersionDialog(WindowManager.getInstance().getCurrentMainFrame(), false);
		
		// Dispose splash screen
		splashScreen.dispose();
	}

	
	/**
	 * Returns Launcher's unique instance.
	 */
	public static Launcher getLauncher() {
		return launcher;
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

	
    /**
	 * Shows a spash screen
	 */
	private JWindow showSplashScreen() {
		JWindow splashScreen = new JWindow();

		// Resolves the URL of the image within the JAR file
		URL imageURL = getClass().getResource("/logo.png");

		ImageIcon imageIcon = new ImageIcon(imageURL);
		splashScreen.setContentPane(new JLabel(imageIcon));
		
//		splashScreen.pack();
		// Set size manually instead of using pack(), because of a bug under 1.3.1/Win32 which
		// would eat a 1-pixel row of the image
		splashScreen.setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		splashScreen.setLocation(screenSize.width/2 - splashScreen.getSize().width/2,
					     screenSize.height/2 - splashScreen.getSize().height/2);
	    splashScreen.show();
	
		return splashScreen;
	} 

}
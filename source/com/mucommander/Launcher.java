package com.mucommander;

import com.mucommander.ui.WindowManager;
import com.mucommander.ui.CheckVersionDialog;
import com.mucommander.ui.macosx.FinderIntegration;

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
	public final static String MUCOMMANDER_VERSION = "0.6.1";

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
		// Show splash screen before anything else
		JWindow splashScreen = showSplashScreen();

//		// Turns on dynamic layout
//		setDynamicLayout(true);

		// Traps VM shutdown
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // Check for newer version unless it was disabled
        String checkForUpdates = ConfigurationManager.getVariable("prefs.check_for_updates_on_startup", "true");
        if(!(checkForUpdates==null || checkForUpdates.equals("false")))
            new CheckVersionDialog(WindowManager.getInstance().getCurrentMainFrame(), false);

		// If muCommander is running under Mac OS X (how lucky!), add some
		// glue for the main menu bar.
		if(PlatformManager.getOsType()==PlatformManager.MAC_OS_X) {
			try {
				FinderIntegration finderIntegration = new FinderIntegration();
			}
			catch(Exception e) {
				if(Debug.ON)
					System.out.println("Launcher.init: exception thrown while initializing Mac Finder integration");
			}
		}
		
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

		splashScreen.getContentPane().add(new JLabel(new ImageIcon(imageURL)));
		splashScreen.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		splashScreen.setLocation(screenSize.width/2 - splashScreen.getSize().width/2,
					     screenSize.height/2 - splashScreen.getSize().height/2);
	    splashScreen.show();
	
		return splashScreen;
	} 

}
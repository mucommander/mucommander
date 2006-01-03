
package com.mucommander.ui.macosx;

import com.mucommander.Launcher;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.QuitDialog;
import com.mucommander.ui.WindowManager;
import com.mucommander.ui.about.AboutDialog;

//import com.apple.mrj.MRJAboutHandler;
//import com.apple.mrj.MRJPrefsHandler;
//import com.apple.mrj.MRJQuitHandler;
//import com.apple.mrj.MRJApplicationUtils;


public class FinderIntegration implements Runnable, com.apple.mrj.MRJAboutHandler, com.apple.mrj.MRJPrefsHandler, com.apple.mrj.MRJQuitHandler {

	private final static int ABOUT_ACTION = 0;
	private final static int PREFS_ACTION = 1;
	private final static int QUIT_ACTION = 2;
	
	private int action;
	
	public FinderIntegration() {
		
		// Turn on/off brush metal look (default is off because still buggy when scrolling and panning dialog windows) :
		//  "Allows you to display your main windows with the 'textured' Aqua window appearance.
		//   This property should be applied only to the primary application window,
		//   and should not affect supporting windows like dialogs or preference windows."
		System.setProperty("apple.awt.brushMetalLook", ConfigurationManager.getVariable("prefs.macosx.brushed_metal_look", "true"));

		// Enables/Disables screen menu bar (default is on) :
		//  "if you are using the Aqua look and feel, this property puts Swing menus in the Mac OS X menu bar."
		System.setProperty("apple.laf.useScreenMenuBar", ConfigurationManager.getVariable("prefs.macosx.screen_menu_bar", "true"));

		// Have to catch exceptions (NoClassDefFoundError and NoSuchMethodException)
		// because they seem not to be available under Mac OS X 10.1 (reported by Lanch)
		try {com.apple.mrj.MRJApplicationUtils.registerAboutHandler(this);}
		catch(NoClassDefFoundError e){}
		catch(Exception e2){}
		try {com.apple.mrj.MRJApplicationUtils.registerPrefsHandler(this);}
		catch(NoClassDefFoundError e){}
		catch(Exception e2){}
		try {com.apple.mrj.MRJApplicationUtils.registerQuitHandler(this);}
		catch(NoClassDefFoundError e){}
		catch(Exception e2){}
	}

	public void handleAbout() {
		this.action = ABOUT_ACTION;
		new Thread(this, "com.mucommander.ui.macosx.FinderIntegration's handleAbout Tread").start();
	}
	
	public void handlePrefs() {
		this.action = PREFS_ACTION;
		new Thread(this, "com.mucommander.ui.macosx.FinderIntegration's handlePrefs Tread").start();
	}
	
	public void handleQuit() {
		this.action = QUIT_ACTION;
		new Thread(this, "com.mucommander.ui.macosx.FinderIntegration's handleQuit Tread").start();
	}

	
	public void run() {
		MainFrame mainFrame = WindowManager.getInstance().getCurrentMainFrame();
		
		// Do nothing (return) when in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

		switch(action) {
			case ABOUT_ACTION:
				new AboutDialog(mainFrame).showDialog();
				break;
			case PREFS_ACTION:
				mainFrame.showPreferencesDialog();
				break;
			case QUIT_ACTION:
				// Show confirmation dialog if it hasn't been disabled
				if(ConfigurationManager.getVariable("prefs.quit_confirmation", "true").equals("true"))
					new QuitDialog(mainFrame);
				// Quit directly otherwise
				else
					WindowManager.getInstance().quit();
				break;
		}
	}
}

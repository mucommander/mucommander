
package com.mucommander.ui.macosx;

import com.mucommander.Launcher;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.about.AboutDialog;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;
import com.apple.mrj.MRJApplicationUtils;


public class FinderIntegration implements MRJAboutHandler, MRJPrefsHandler, MRJQuitHandler, Runnable {

	private final static int ABOUT_ACTION = 0;
	private final static int PREFS_ACTION = 1;
	private final static int QUIT_ACTION = 2;
	
	private int action;
	
	public FinderIntegration() {
		MRJApplicationUtils.registerAboutHandler(this);
		MRJApplicationUtils.registerPrefsHandler(this);
		MRJApplicationUtils.registerQuitHandler(this);
	}

	public void handleAbout() {
		this.action = ABOUT_ACTION;
		new Thread(this).start();
	}
	
	public void handlePrefs() {
		this.action = PREFS_ACTION;
		new Thread(this).start();
	}
	
	public void handleQuit() {
		this.action = QUIT_ACTION;
		new Thread(this).start();
	}

	
	public void run() {
		MainFrame mainFrame = Launcher.getLauncher().getCurrentMainFrame();
		
		switch(action) {
			case ABOUT_ACTION:
				new AboutDialog(mainFrame).showDialog();
				break;
				
			case PREFS_ACTION:
				mainFrame.showPreferencesDialog();
				break;
			case QUIT_ACTION:
				System.exit(0);
				break;
		}
	}
}


package com.mucommander.ui.macosx;

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;


/**
 * This class registers the About, Preferences and Quit handlers using the com.apple.mrj API available
 * under Java 1.3 (deprecated under Java 1.4 and up).
 *
 * @author Maxence Bernard
 */
class MRJHandler implements MRJAboutHandler, MRJPrefsHandler, MRJQuitHandler, Runnable {

    private int action;

    private final static int ABOUT_ACTION = 0;
    private final static int PREFS_ACTION = 1;
    private final static int QUIT_ACTION = 2;
		    
    public MRJHandler() {
        // Register the handlers. Error has to be caught for NoClassDefFoundError and NoSuchMethodError
        // because MRJ toolkit doesn't seem to be available under Mac OS X 10.1 (reported by Lanch).
        // Note: As a result, under Mac OS X 10.1, About, Preferences and Quit won't be available
        try {MRJApplicationUtils.registerAboutHandler(this);}
        catch(Error e){}
        try {MRJApplicationUtils.registerPrefsHandler(this);}
        catch(Error e){}
        try {MRJApplicationUtils.registerQuitHandler(this);}
        catch(Error e){}
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
        switch(action) {
            case ABOUT_ACTION:
                OSXIntegration.showAbout();
                break;
            case PREFS_ACTION:
                OSXIntegration.showPreferences();
                break;
            case QUIT_ACTION:
                OSXIntegration.doQuit();
                break;
        }
    }
}
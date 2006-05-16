
package com.mucommander.ui.macosx;

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;

import com.mucommander.ui.QuitDialog;


/**
 * This class registers the About, Preferences and Quit handlers using the com.apple.mrj API available
 * under Java 1.3 (deprecated under Java 1.4 and up).
 *
 * Note: the code executed in response to handle methods has to be run in a separate thread because of a bug 
 * in Mac OS X 10.1, which will otherwise cause the app to hang if a dialog is invoked. 
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
        // because MRJ toolkit seems not to be available under Mac OS X 10.1 with some early Java runtimes
        // (reported by Lanch). For those particular configurations, about/preferences/quit menu items won't
        // be available.
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

        // The javadocs for MRJQuitHandler state that to abort the quit, IllegalStateException must be thrown.
        // Although we're not cancelling quit at this point, we're giving ourselves a chance to cancel quit
        // if quit confirmation dialog is enabled and user chooses to cancel quit.
        // If we didn't, Mac OS X would just terminate the VM when this method returns.
        if(QuitDialog.confirmationRequired())
            throw new IllegalStateException();
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
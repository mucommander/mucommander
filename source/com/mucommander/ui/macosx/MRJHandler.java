
package com.mucommander.ui.macosx;

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;


class MRJHandler implements MRJAboutHandler, MRJPrefsHandler, MRJQuitHandler, Runnable {

    private int action;

    private final static int ABOUT_ACTION = 0;
    private final static int PREFS_ACTION = 1;
    private final static int QUIT_ACTION = 2;
		    
    public MRJHandler() {
        // Have to catch Errors (NoClassDefFoundError and NoSuchMethodError)
        // because they seem not to be available under Mac OS X 10.1 (reported by Lanch)
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
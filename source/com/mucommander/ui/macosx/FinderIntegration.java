
package com.mucommander.ui.macosx;

import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.QuitDialog;
import com.mucommander.ui.WindowManager;
import com.mucommander.ui.about.AboutDialog;

import com.apple.eawt.ApplicationListener;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;


/**
 * This class handles Mac OS X specifics when muCommander is started:
 * <ul>
 *  <li>Creates hooks for the 'About', 'Preferences' and 'Quit' application menu items
 *  <li>Turns on/off brush metal based on preferences (default is on)
 *  <li>Turns screen menu bar based on preferences (default is on, no GUI for that pref)
 * </ul>
 *
 * <p>The Apple API used to register the hooks depends on the version of the Java runtime:
 * <ul>
 *  <li>com.apple.eawt is used for Java 1.4 and up
 *  <li>com.apple.mrj is used for Java 1.3 (deprecated in Java 1.4)
 * </ul>
 *
 * @author Maxence Bernard
 */
public class FinderIntegration implements ApplicationListener, MRJAboutHandler, MRJPrefsHandler, MRJQuitHandler, Runnable {

    private final static int ABOUT_ACTION = 0;
    private final static int PREFS_ACTION = 1;
    private final static int QUIT_ACTION = 2;
	
    private int action;
	
    public FinderIntegration() {
        // Turn on/off brush metal look (default is off because still buggy when scrolling and panning dialog windows) :
        //  "Allows you to display your main windows with the 'textured' Aqua window appearance.
        //   This property should be applied only to the primary application window,
        //   and should not affect supporting windows like dialogs or preference windows."
        System.setProperty("apple.awt.brushMetalLook", ""+ConfigurationManager.getVariableBoolean("prefs.macosx.brushed_metal_look", true));

        // Enables/Disables screen menu bar (default is on) :
        //  "if you are using the Aqua look and feel, this property puts Swing menus in the Mac OS X menu bar."
        System.setProperty("apple.laf.useScreenMenuBar", ""+ConfigurationManager.getVariableBoolean("prefs.macosx.screen_menu_bar", true));

        if(PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_4) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("registering EAWT hooks");

            Application app = new Application();
            app.setEnabledAboutMenu(true);
            app.setEnabledPreferencesMenu(true);
            app.addApplicationListener(this);
        }
        else {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("registering MRJ hooks");
            
            // Have to catch Errors (NoClassDefFoundError and NoSuchMethodError)
            // because they seem not to be available under Mac OS X 10.1 (reported by Lanch)
            try {MRJApplicationUtils.registerAboutHandler(this);}
            catch(Error e){}
            try {MRJApplicationUtils.registerPrefsHandler(this);}
            catch(Error e){}
            try {MRJApplicationUtils.registerQuitHandler(this);}
            catch(Error e){}
        }
    }

    private void showAbout() {
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();
        
        // Do nothing (return) when in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        new AboutDialog(mainFrame).showDialog();
    }

    private void showPreferences() {
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();

        // Do nothing (return) when in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        mainFrame.showPreferencesDialog();
    }

    private void doQuit() {
        // Show confirmation dialog if it hasn't been disabled
        if(ConfigurationManager.getVariableBoolean("prefs.quit_confirmation", true))
            new QuitDialog(WindowManager.getCurrentMainFrame());
        // Quit directly otherwise
        else
            WindowManager.quit();
    }

    ////////////////////////////////////
    // EAWT hooks for Java 1.4 and up //
    ////////////////////////////////////

    public void handleAbout(ApplicationEvent event) {
        event.setHandled(true);
        showAbout();
    }

    public void handlePreferences(ApplicationEvent event) {
        event.setHandled(true);
        showPreferences();
    }

    public void handleQuit(ApplicationEvent event) {
        event.setHandled(true);
        doQuit();
    }

    public void handleOpenApplication(ApplicationEvent event) {
        // No-op
    }

    public void handleReOpenApplication(ApplicationEvent event) {
        // No-op
    }

    public void handleOpenFile(ApplicationEvent event) {
        // No-op
    }

    public void handlePrintFile(ApplicationEvent event) {
        // No-op
    }


    ////////////////////////////
    // MRJ hooks for Java 1.3 //
    ////////////////////////////

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
                showAbout();
                break;
            case PREFS_ACTION:
                showPreferences();
                break;
            case QUIT_ACTION:
                doQuit();
                break;
        }
    }
}

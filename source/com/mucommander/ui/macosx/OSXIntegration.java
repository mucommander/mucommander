
package com.mucommander.ui.macosx;

import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.QuitDialog;
import com.mucommander.ui.WindowManager;
import com.mucommander.ui.about.AboutDialog;


/**
 * This class handles Mac OS X specifics when muCommander is started:
 * <ul>
 *  <li>Turns on/off brush metal based on preferences (default is on)
 *  <li>Turns screen menu bar based on preferences (default is on, no GUI for that pref)
 *  <li>Registers handlers for the 'About', 'Preferences' and 'Quit' application menu items
 * </ul>
 *
 * <p>The Apple API used to register the handlers depends on the Java runtime version:
 * <ul>
 *  <li>com.apple.eawt is used for Java 1.4 and up
 *  <li>com.apple.mrj is used for Java 1.3 (deprecated in Java 1.4)
 * </ul>
 *
 * @author Maxence Bernard
 */
public class OSXIntegration {

    public OSXIntegration() {
        // Turn on/off brush metal look (default is off because still buggy when scrolling and panning dialog windows) :
        //  "Allows you to display your main windows with the 'textured' Aqua window appearance.
        //   This property should be applied only to the primary application window,
        //   and should not affect supporting windows like dialogs or preference windows."
        System.setProperty("apple.awt.brushMetalLook", ""+ConfigurationManager.getVariableBoolean("prefs.macosx.brushed_metal_look", true));

        // Enables/Disables screen menu bar (default is on) :
        //  "if you are using the Aqua look and feel, this property puts Swing menus in the Mac OS X menu bar."
        System.setProperty("apple.laf.useScreenMenuBar", ""+ConfigurationManager.getVariableBoolean("prefs.macosx.screen_menu_bar", true));

        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_4) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("registering EAWT hooks");
            new EAWTHandler();
        }
        else {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("registering MRJ hooks");            
            new MRJHandler();
        }
    }

    /**
     * Shows the 'About' dialog.
     */
    public static void showAbout() {
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();
        
        // Do nothing (return) when in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        new AboutDialog(mainFrame).showDialog();
    }

    /**
     * Shows the 'Preferences' dialog.
     */
    public static void showPreferences() {
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();

        // Do nothing (return) when in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        mainFrame.showPreferencesDialog();
    }

    /**
     * Quits the application after displaying a confirmation dialog if it hasn't been disabled
     * in the preferences. Return <code>true</code> if the operation has been aborted by user.
     */
    public static boolean doQuit() {
        // Ask the user for confirmation and abort if user refused to quit.
        if(!QuitDialog.confirmQuit())
            return false;

        // We got a green -> quit!
        WindowManager.quit();
                
        return true;
    }
}

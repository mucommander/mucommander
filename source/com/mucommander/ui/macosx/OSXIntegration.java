/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.ui.macosx;

import com.mucommander.conf.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.QuitDialog;
import com.mucommander.ui.WindowManager;
import com.mucommander.ui.about.AboutDialog;
import com.mucommander.ui.action.ActionManager;


/**
 * This class handles Mac OS X specifics when muCommander is started:
 * <ul>
 *  <li>Turns on/off brush metal based on preferences (default is on)
 *  <li>Turns screen menu bar based on preferences (default is on, no GUI for that pref)
 *  <li>Registers handlers for the 'About', 'Preferences' and 'Quit' application menu items
 * </ul>
 *
 * <p>The com.apple.eawt API is used to handle 'About', 'Preferences' and 'Quit' events and report back to the OS.
 *
 * @see EAWTHandler
 * @author Maxence Bernard
 */
public class OSXIntegration {

    public OSXIntegration() {
        // Turn on/off brush metal look (default is off because still buggy when scrolling and panning dialog windows) :
        //  "Allows you to display your main windows with the 'textured' Aqua window appearance.
        //   This property should be applied only to the primary application window,
        //   and should not affect supporting windows like dialogs or preference windows."
        System.setProperty("apple.awt.brushMetalLook", ""+ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_BRUSHED_METAL,
                                                                                                  ConfigurationVariables.DEFAULT_USE_BRUSHED_METAL));

        // Enables/Disables screen menu bar (default is on) :
        //  "if you are using the Aqua look and feel, this property puts Swing menus in the Mac OS X menu bar."
        System.setProperty("apple.laf.useScreenMenuBar", ""+ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_SCREEN_MENU_BAR,
                                                                                                    ConfigurationVariables.DEFAULT_USE_SCREEN_MENU_BAR));

        // Catch 'About', 'Preferences' and 'Quit' events
        new EAWTHandler();
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

        ActionManager.performAction(com.mucommander.ui.action.ShowPreferencesAction.class, mainFrame);
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

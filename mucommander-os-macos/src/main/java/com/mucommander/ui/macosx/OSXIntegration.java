/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.macosx;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;

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
        if(OsFamily.MAC_OS_X.isCurrent()) {
            // At the time of writing, the 'brushed metal' look causes the JVM to crash randomly under Leopard (10.5)
            // so we disable brushed metal on that OS version but leave it for earlier versions where it works fine.
            // See http://www.mucommander.com/forums/viewtopic.php?f=4&t=746 for more info about this issue.
            if(OsVersion.MAC_OS_X_10_4.isCurrentOrLower()) {
                // Turn on/off brush metal look (default is off because still buggy when scrolling and panning dialog windows) :
                //  "Allows you to display your main windows with the 'textured' Aqua window appearance.
                //   This property should be applied only to the primary application window,
                //   and should not affect supporting windows like dialogs or preference windows."
                System.setProperty("apple.awt.brushMetalLook",
                    ""+MuConfigurations.getPreferences().getVariable(MuPreference.USE_BRUSHED_METAL, MuPreferences.DEFAULT_USE_BRUSHED_METAL));
            }

            // Enables/Disables screen menu bar (default is on) :
            //  "if you are using the Aqua look and feel, this property puts Swing menus in the Mac OS X menu bar."
            System.setProperty("apple.laf.useScreenMenuBar", ""+MuConfigurations.getPreferences().getVariable(MuPreference.USE_SCREEN_MENU_BAR,
                                                                                                 MuPreferences.DEFAULT_USE_SCREEN_MENU_BAR));

            // Catch 'About', 'Preferences' and 'Quit' events
            new EAWTHandler();
        }
    }
}

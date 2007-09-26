/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.notifier;

import com.growl.Growl;
import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;

/**
 * SystemTrayNotifier implements a notifier that uses the System Tray to display notifications. Growl is a 3rd party
 * notification system for Mac OS X, which allows Growl-enabled applications to display small, unintrusive popup
 * notifications to inform the user of noteworthy events. More info about Growl can be found at : http://growl.info/ .
 *
 * <p><b>Important note:</b> the Growl library (and some of the code in this class) relies on the Cocoa-Java bridge
 * provided by Apple. This bridge has unfortunately been deprecated for a while but as of today still works.
 * The day it stops working, some native code invoked via JNI will have to be used instead.
 *
 * @author Maxence Bernard
 */
public class GrowlNotifier extends AbstractNotifier {

    /** Growl notifier instance */
    private static Growl growl;

    /** Is this notifier enabled ? */
    private static boolean isEnabled;

    /** Dictionary keys for the different notification types */
    private final static String NOTIFICATION_KEYS[] = {
        "progress_dialog.job_finished",
        "progress_dialog.job_error"
    };

    GrowlNotifier() {
    }


    /////////////////////////////////////
    // AbstractNotifier implementation //
    /////////////////////////////////////

    public boolean setEnabled(boolean enabled) {
        if(enabled) {
            // No need to bother if the OS is not Mac OS X
            if(PlatformManager.getOsFamily()!=PlatformManager.MAC_OS_X)
                return false;

            // If Growl notifier has already been initialized
            if(growl!=null) {
                return (isEnabled = true);
            }

            try {
                // Register the application (muCommander) and its icon. Growl doesn't seem to be able to retrieve the
                // application's icon by itself, so we have to use some Cocoa magic to get it and feed it to Growl.
                growl = new Growl("muCommander", com.apple.cocoa.application.NSApplication.sharedApplication().applicationIconImage().TIFFRepresentation());
                //        growl = new Growl("muCommander", "/Users/maxence/Projects/mucommander/res/icons/icon.icns");

                String notificationTypes[] = new String[]{
                        Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_COMPLETED]),
                        Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_ERROR])
                };

                // Declare a list of available notification types
                growl.setAllowedNotifications(notificationTypes);
                // Declare a list of notification types enabled by default
                growl.setDefaultNotifications(notificationTypes);

                // Commit everything
                growl.register();

                if(Debug.ON)
                    Debug.trace("Application registered OK");

                return (isEnabled = true);
            }
            catch(Exception e) {
                if(Debug.ON) Debug.trace("Exception thrown while initializing Growl support (Growl not running?): "+e);
            }
            catch(Error e) {
                if(Debug.ON) Debug.trace("Error while initializing Growl support (cocoa-java not available?): "+e);
            }

            growl = null;
            return (isEnabled = false);
        }
        else {
            return (isEnabled = false);
        }
    }

    public boolean isEnabled() {
        return growl!=null && isEnabled;
    }

    public boolean displayNotification(int notificationType, String title, String description) {
        if(Debug.ON) Debug.trace("notificationType="+notificationType+" title="+title+" description="+description);

        if(!isEnabled()) {
            if(Debug.ON) Debug.trace("Ignoring notification, this notifier is not enabled");

            return false;
        }

        try {
            growl.notifyGrowlOf(Translator.get(NOTIFICATION_KEYS[notificationType]), title, description);
            if(Debug.ON) Debug.trace("Notification sent OK");

            return true;
        }
        catch(Exception e) {
            if(Debug.ON) {
                Debug.trace("Exception thrown while sending notification:");
                e.printStackTrace();
            }

            return false;
        }
    }

    public String getPrettyName() {
        return "Growl";
    }
}

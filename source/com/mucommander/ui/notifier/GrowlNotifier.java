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

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.macosx.AppleScript;
//import com.growl.Growl;

/**
 * GrowlNotifier implements a notifier that uses the Growl notification system.
 *
 * <p>Growl is a third party notification system for Mac OS X, which allows Growl-enabled applications to display small,
 * unintrusive popup notifications to inform the user of noteworthy events. Growl can be found at:
 * <a href="http://growl.info">http://growl.info</a>.
 *
 * <p>This class communicates with Growl using {@link AppleScript}. More information about the AppleScript syntax can
 * be found <a href="http://growl.info/documentation/applescript-support.php"/>here</a>.
 * The Growl Java library part of the Growl SDK was previously used but it relied on the Cocoa-Java library which has
 * been deprecated by Apple since then.</p>.
 *
 * @author Maxence Bernard
 */
public class GrowlNotifier extends AbstractNotifier {

    /** Is this notifier enabled ? */
    private static boolean isEnabled;

    /** Has muCommander been registered ? */
    private static boolean isRegistered;

    /** Dictionary keys for the different notification types */
    private final static String NOTIFICATION_KEYS[] = {
        "progress_dialog.job_finished",
        "progress_dialog.job_error"
    };

    /** Name of the application to be registered with Growl, as spelled in the .app */
    private final static String APP_NAME = "muCommander";

    /** This AppleScript returns "true" if Growl is currently running, "false" if it isn't */
    private final static String IS_GROWL_RUNNING_APPLESCRIPT =
            "tell application \"System Events\"\n" +
                "\tset isRunning to (count of (every process whose name is \"GrowlHelperApp\")) > 0\n" +
            "end tell";


    GrowlNotifier() {
    }


    /**
     * Puts the given AppleScript bit inside a <code>tell application / end tell</code> block, executes the script
     * and returns <code>true</code> if it was successfully executed.
     *
     * @param appleScript the AppleScript bit to execute
     * @return true if the script was successfully executed
     */
    private static boolean tellGrowl(String appleScript) {
        return AppleScript.execute(
                "tell application \"GrowlHelperApp\"\n" +
                    "\t"+appleScript+"\n" +
                "end tell",
            null);
    }


    /////////////////////////////////////
    // AbstractNotifier implementation //
    /////////////////////////////////////

    public String getPrettyName() {
        return "Growl";
    }


    public boolean setEnabled(boolean enabled) {
        if(enabled) {
            // No need to bother if the OS is not Mac OS X
            if(PlatformManager.getOsFamily()!=PlatformManager.MAC_OS_X)
                return false;

            // Nothing else to do if the application has already been registered
            if(isRegistered)
                return (isEnabled = true);

            // Test if Growl is currently running and abort if it is not
            StringBuffer outputBuffer = new StringBuffer();
            if(!(AppleScript.execute(IS_GROWL_RUNNING_APPLESCRIPT, outputBuffer) && outputBuffer.toString().equals("true"))) {
                if(Debug.ON) Debug.trace("Growl is not running, aborting");

                return false;
            }

            // Register the application (muCommander) with Growl

            // The list of notification types muCommander uses
            String notificationTypes =
                "{"+
                    "\""+Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_COMPLETED])+"\","+
                    "\""+Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_ERROR])+"\""+
                "}";

            // Register muCommander with Growl, declare the notifications types and enable all of them by default
            isRegistered = tellGrowl(
                "register as application \""+APP_NAME+"\""+
                " all notifications "+notificationTypes+
                " default notifications "+notificationTypes+
                " icon of application \""+APP_NAME+"\"");

            if(Debug.ON) Debug.trace(isRegistered?
                "Successfully registered "+APP_NAME+" with Growl":
                "Error while registering "+APP_NAME+" with Growl");

            return isEnabled = isRegistered;
        }
        else {
            return (isEnabled = false);
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean displayNotification(int notificationType, String title, String description) {
        if(Debug.ON) Debug.trace("notificationType="+notificationType+" title="+title+" description="+description);

        if(!isEnabled()) {
            if(Debug.ON) Debug.trace("Ignoring notification, this notifier is not enabled");

            return false;
        }

        boolean success = tellGrowl(
            "notify with"+
            " name \""+Translator.get(NOTIFICATION_KEYS[notificationType])+"\""+
            " title \""+title+"\""+
            " description \""+description+"\""+
            " application name \""+APP_NAME+"\"");

        if(Debug.ON) Debug.trace(success?
            "Notification sent successfully":
            "Error while sending notification");

        return success;
    }

// The following commented methods are implemented using the Growl Java library that comes with the Growl SDK. This
// library relies on the Cocoa-Java library which has been deprecated by Apple, which is why we're not using it anymore.
// The code has been kept for the record, in case the Growl Java library is ever used again.    

//    public boolean setEnabled(boolean enabled) {
//        if(enabled) {
//            // No need to bother if the OS is not Mac OS X
//            if(PlatformManager.getOsFamily()!=PlatformManager.MAC_OS_X)
//                return false;
//
//            // If Growl notifier has already been initialized
//            if(growl!=null) {
//                return (isEnabled = true);
//            }
//
//            try {
//                // Register the application (muCommander) and its icon. Growl doesn't seem to be able to retrieve the
//                // application's icon by itself, so we have to use some Cocoa magic to get it and feed it to Growl.
//                growl = new Growl("muCommander", com.apple.cocoa.application.NSApplication.sharedApplication().applicationIconImage().TIFFRepresentation());
//
//                String notificationTypes[] = new String[]{
//                        Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_COMPLETED]),
//                        Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_ERROR])
//                };
//
//                // Declare a list of available notification types
//                growl.setAllowedNotifications(notificationTypes);
//                // Declare a list of notification types enabled by default
//                growl.setDefaultNotifications(notificationTypes);
//
//                // Commit everything
//                growl.register();
//
//                if(Debug.ON)
//                    Debug.trace("Application registered OK");
//
//                return (isEnabled = true);
//            }
//            catch(Exception e) {
//                if(Debug.ON) Debug.trace("Exception thrown while initializing Growl support (Growl not running?): "+e);
//            }
//            catch(Error e) {
//                if(Debug.ON) Debug.trace("Error while initializing Growl support (cocoa-java not available?): "+e);
//            }
//
//            growl = null;
//            return (isEnabled = false);
//        }
//        else {
//            return (isEnabled = false);
//        }
//    }
//
//    public boolean isEnabled() {
//        return growl!=null && isEnabled;
//    }
//
//    public boolean displayNotification(int notificationType, String title, String description) {
//        if(Debug.ON) Debug.trace("notificationType="+notificationType+" title="+title+" description="+description);
//
//        if(!isEnabled()) {
//            if(Debug.ON) Debug.trace("Ignoring notification, this notifier is not enabled");
//
//            return false;
//        }
//
//        try {
//            growl.notifyGrowlOf(Translator.get(NOTIFICATION_KEYS[notificationType]), title, description);
//            if(Debug.ON) Debug.trace("Notification sent OK");
//
//            return true;
//        }
//        catch(Exception e) {
//            if(Debug.ON) {
//                Debug.trace("Exception thrown while sending notification:");
//                e.printStackTrace();
//            }
//
//            return false;
//        }
//    }

}

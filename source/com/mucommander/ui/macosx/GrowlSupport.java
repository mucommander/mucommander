package com.mucommander.ui.macosx;

import com.growl.Growl;
import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;

import java.awt.*;

/**
 * This class provides support for Growl. Growl is a notifications system for Mac OS X, which allows Growl-enabled
 * applications to display small, unintrusive popup notifications to inform the user of noteworthy events.
 * More info about Growl can be found at : http://growl.info/ .
 *
 * <p>The {@link #sendNotification(java.awt.Window, int, String, String)} is used to send Growl notifications.
 * The {@link #init()} method must be called once before any notification can be sent.
 *
 * <p><b>Important note:</b> the Growl library (and some of the code in this class) relies on the Cocoa-Java bridge
 * provided by Apple. This bridge has unfortunately been deprecated for a while but as of today still works.
 * When it stops working, some native code invoked via JNI will have to be used instead. 
 *
 * @author Maxence Bernard
 */
public class GrowlSupport {

    /** Growl notifier instance */
    private static Growl notifier;

    /** 'Job completed' notification type, to notify that a job is finished */
    public final static int NOTIFICATION_TYPE_JOB_COMPLETED = 0;

    /** 'Job completed' notification type, to notify of an error in a job */
    public final static int NOTIFICATION_TYPE_JOB_ERROR = 1;

    /** Dictionary keys of notification types */
    private final static String NOTIFICATION_KEYS[] = {
        "progress_dialog.job_finished",
        "progress_dialog.job_error"
    };


    /**
     * Initializes Growl support by registering muCommander and its notification types, and returns <code>true</code>
     * if Growl notifier could successfully be initialized and muCommander properly registered.
     * Returns <code>false</code> if :
     * <ul>
     *  <li>current OS is not Mac OS X
     *  <li>Growl daemon could not be reached (Growl not installed)
     *  <li>muCommander could not be registered for any other reason
     * </ul>
     *
     * <p>This method should only be called once.
     */
    public static boolean init() {
        // No need to bother if the OS is not Mac OS X
        if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X)
            return false;

        try {
//        notifier = new Growl("muCommander", new com.apple.cocoa.foundation.NSData());
//        notifier = new Growl("muCommander", "/Users/maxence/Projects/mucommander/res/icons/icon.icns");

            // Register the application (muCommander) and its icon. Growl doesn't seem to be able to retrieve the
            // application's icon itself, so we have to use some Cocoa magic to get it and feed it to Growl.
            notifier = new Growl("muCommander", com.apple.cocoa.application.NSApplication.sharedApplication().applicationIconImage().TIFFRepresentation());

            String notificationTypes[] = new String[]{
                    Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_COMPLETED]),
                    Translator.get(NOTIFICATION_KEYS[NOTIFICATION_TYPE_JOB_ERROR])
            };

            // Declares a list of available notification types
            notifier.setAllowedNotifications(notificationTypes);
            // Declares a list of notification types enabled by default
            notifier.setDefaultNotifications(notificationTypes);

            // Commits everything
            notifier.register();

            if(Debug.ON)
                Debug.trace("Application registered OK");

            return true;
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Exception thrown while initializing Growl support (Growl not running?): "+e);
        }
        catch(Error e) {
            if(Debug.ON) Debug.trace("Error while initializing Growl support (cocoa-java not available?): "+e);
        }

        return false;
    }


    /**
     * Returns true if Growl support is available and has been properly initialized.
     * In other words, if <code>true</code> is returned, {@link #sendNotification(java.awt.Window, int, String, String)}
     * can be called to send Growl notifications to the end user.
     * This method will always return <code>false</code> until {@link #init()} has been called.
     *
     * @return if Growl support is available and properly initialized
     */
    public static boolean isGrowlAvailable() {
        return notifier!=null;
    }


    /**
     * Sends a Growl notification of a specified type, title, description and originating window.
     * The Growl notification will only be sent if the given window is not currently active / in the foreground, in order
     * not to flood the user with notifications of things that he can already see on screen.
     *
     * <p>Returns <code>true</code> if the notification was properly sent, <code>false</code> if:
     * <ul>
     * <li>Growl support is not enabled or not available
     * <li>the given source window is currently in the foreground
     * <li>the notification could not be sent to Growl because of an error
     * </ul>
     *
     * @param sourceWindow the window the notification originates from. If the window is in the foreground, the
     * notification will not be sent.
     * @param notificationType one of the available notification types, see constant fields for possible values
     * @param title the title of the notification that Growl will display
     * @param description the description of the notification that Growl will display 
     * @return true if the notification was properly sent, false if Growl support is not enabled, the source window is
     * active, or an error happened while trying to send the notification to Growl.
     */
    public static boolean sendNotification(Window sourceWindow, int notificationType, String title, String description) {
        if(Debug.ON) Debug.trace("notificationType="+notificationType+" title="+title+" description="+description);

        if(sourceWindow.isActive() || notifier==null) {
            if(Debug.ON) Debug.trace("Ignoring notification");

            return false;
        }

        try {
            notifier.notifyGrowlOf(Translator.get(NOTIFICATION_KEYS[notificationType]), title, description);
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
}

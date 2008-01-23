/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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
import com.mucommander.ui.main.WindowManager;

import javax.swing.*;
import java.awt.*;

/**
 * AbstractNotifier is a generic representation of a system notifier. It also provides factory methods to
 * retrieve the current platform's notifier instance, if there is one.
 * <p>
 * A notifier serves the purpose of displaying notifications to the screen, to inform the user of an event when
 * the application is not visible (in the background).
 * </p>
 * <p>
 * The notifier instance returnd by {@link #getNotifier()} is platform-dependent. At this time, two notifier
 * implementations are available:
 * <ul>
 *  <li>{@link GrowlNotifier}: for Mac OS X, requires Growl to be installed
 *  <li>{@link SystemTrayNotifier}: for Java 1.6 and up, using the java.awt.SystemTray API
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class AbstractNotifier implements NotificationTypes {

    /** AbstractNotifier instance, null if none is available on the current platform */
    private static AbstractNotifier notifier;

    static {
        // Finds and creates a suitable AbstractNotifier instance for the platform, if there is one
        if(PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X)
            notifier = new GrowlNotifier();
        else if(PlatformManager.JAVA_1_6.isCurrentOrHigher() && SystemTray.isSupported())
            notifier = new SystemTrayNotifier();
    }

    /**
     * Returns <code>true<code> if an AbstractNotifier instance is available. In other words, if <code>true</code> is
     * returned, {@link #getNotifier()} will return a non-null value.
     *
     * @return true if an AbstractNotifier instance is available
     */
    public static boolean isAvailable() {
        return notifier!=null;
    }

    /**
     * Returns an AbstractNotifier instance that can be used on the current platform, <code>null</code> if none
     * is available.
     * Note that the returned <code>AbstractNotifier</code> must be enabled before it can be used, which is not
     * guaranteed to succeed.
     *
     * @return an AbstractNotifier instance that can be used on the current platform, null if none is available
     */
    public static AbstractNotifier getNotifier() {
        return notifier;
    }


    /**
     * Displays a notification with the specified type, title and description and returns <code>true</code> if the
     * notification could be displayed. The notification will not be displayed if the current muCommander window
     * (or one of its child windows) is presently in the foreground, so that the user doesn't get notified for things
     * that he/she can already see on the screen.
     *
     * <p>
     * The notification will not be displayed if:
     * <ul>
     *  <li>muCommander is in the foreground
     *  <li>this notifier is not enabled
     *  <li>the notification could not be delivered because of an error
     * </ul>
     * </p>
     *
     * <p>
     * Note that this method is executed in a separate thread after all pending Swing events have been processed,
     * to ensure in the event of a window being made inactive that the notification will not be triggered. This method
     * immediately return s(i.e. does not wait for pending events) and thus is not be able to return if the notification
     * was displayed or not, unlike {@link #displayNotification(int, String, String)}.
     * </p>
     *
     * @param notificationType one of the available notification types, see {@link NotificationTypes} for possible values
     * @param title the title of the notification to display
     * @param description the description of the notification to display
     */
    public void displayBackgroundNotification(final int notificationType, final String title, final String description) {
        SwingUtilities.invokeLater(
            new Thread() {
                public void run() {
                    if(WindowManager.getCurrentMainFrame().isAncestorOfActiveWindow()) {
                        if(Debug.ON) Debug.trace("Ignoring notification, application is in foreground");
                        return;
                    }

                    if(!displayNotification(notificationType, title, description))
                        if(Debug.ON) Debug.trace("Notification failed to be displayed");
                }
            }
        );
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Enables/disables this notifier and returns <code>true</code> if the operation succeeded. A typical case
     * for returning false, is when the underlying notification system (e.g. Growl under Mac OS X) could not be reached.
     * 
     * @param enabled true to enable this notifier, false to disable it
     * @return true if the operation succeeded
     */
    public abstract boolean setEnabled(boolean enabled);

    /**
     * Returns <code>true</code> if this notifier is enabled and ready to display notifications.
     *
     * @return true if this notifier is enabled and ready to display notifications
     */
    public abstract boolean isEnabled();

    /**
     * Displays a notification with the specified type, title and description and returns <code>true</code> if the
     * notification could be displayed. Unlike {@link #displayBackgroundNotification(int, String, String)}, the
     * notification will be attempted for display even if muCommander is currently in the foreground.
     *
     * <p>
     * Returns <code>true</code> if the notification could be displayed, <code>false</code> if:
     * <ul>
     *  <li>this notifier is not enabled
     *  <li>the notification could not be delivered because of an error
     * </ul>
     * </p>
     *
     * @param notificationType one of the available notification types, see {@link NotificationTypes} for possible values
     * @param title the title of the notification to display
     * @param description the description of the notification to display
     * @return true if the notification was properly sent, false otherwise
     */
    public abstract boolean displayNotification(int notificationType, String title, String description);

    /**
     * Returns a pretty name for the underlying notification system that can be displayed to the end user.
     *
     * @return a pretty name for the underlying notification system
     */
    public abstract String getPrettyName();
}

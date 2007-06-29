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

package com.mucommander.ui.notifier;

import com.mucommander.PlatformManager;

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
  *</ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class AbstractNotifier implements NotificationTypes {

    /** AbstractNotifier instance, null if none is available on the current platform */
    private static AbstractNotifier notifier;

    static {
        // Finds and creates a suitable AbstractNotifier instance for the platform, if there is one
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X)
            notifier = new GrowlNotifier();
        else if(PlatformManager.JAVA_VERSION>=PlatformManager.JAVA_1_6 && SystemTray.isSupported())
            notifier = new SystemTrayNotifier();
    }

    /**
     * Returns <code>true<code> if an AbstractNotifier instance is available. In other words, if <code>true</code> is
     * returned, {@link #getNotifier()} will return a non-null value.
     */
    public static boolean isAvailable() {
        return notifier!=null;
    }

    /**
     * Returns an AbstractNotifier instance that can be used on the current platform, <code>null</code> if none
     * is available.
     * Note that the returned <code>AbstractNotifier</code> must be enabled before it can be used, which is not
     * guaranteed to succeed.
     */
    public static AbstractNotifier getNotifier() {
        return notifier;
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
     */
    public abstract boolean isEnabled();

    /**
     * Displays a notification with the specified type, title and description and returns <code>true</code> if the
     * notification could be displayed. The notification will not be displayed if muCommander (of of its windows)
     * is currently in the foreground, in order not to notify the user of things that he/she can already see on
     * the screen.
     *
     * <p>
     * Returns <code>true</code> if the notification could be displayed, <code>false</code> if:
     * <ul>
     *  <li>this notifier is not enabled
     *  <li>the notification could not be delivered because of an error
     *  <li>muCommander is in the foreground
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
     * Returns the pretty name of the underlying notification system.
     */
    public abstract String getPrettyName();
}

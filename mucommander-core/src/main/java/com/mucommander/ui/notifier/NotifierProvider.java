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

package com.mucommander.ui.notifier;

import java.awt.SystemTray;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.os.notifier.AbstractNotifier;
import com.mucommander.os.notifier.NotificationType;
import com.mucommander.ui.main.WindowManager;

public class NotifierProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierProvider.class);

    private static AbstractNotifier defaultNotifier;

    static {
        // Finds and creates a suitable AbstractNotifier instance for the platform, if there is one
        if (SystemTray.isSupported())
            defaultNotifier = new SystemTrayNotifier();
    }

    /**
     * Returns <code>true<code> if an AbstractNotifier instance is available. In other words, if <code>true</code> is
     * returned, {@link #getNotifier()} will return a non-null value.
     *
     * @return true if an AbstractNotifier instance is available
     */
    public static boolean isAvailable() {
        return getNotifier()!=null;
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
        AbstractNotifier notifier = DesktopManager.getNotifier();
        if (notifier != null)
            return notifier;
        return defaultNotifier;
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
     * was displayed or not, unlike {@link #displayNotification(NotificationType, String, String)}.
     * </p>
     *
     * @param notificationType one of the available notification types, see {@link NotificationType} for possible values
     * @param title the title of the notification to display
     * @param description the description of the notification to display
     */
    public static void displayBackgroundNotification(final NotificationType notificationType, final String title, final String description) {
        SwingUtilities.invokeLater(() -> {
            if(WindowManager.getCurrentMainFrame().isAncestorOfActiveWindow()) {
                LOGGER.debug("Ignoring notification, application is in foreground");
                return;
            }

            DesktopManager.requestUserAttention();  // bounce app icon if supported by OS
            if(!getNotifier().displayNotification(notificationType, title, description))
                LOGGER.debug("Notification failed to be displayed");
        });
    }
}

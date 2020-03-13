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

package com.mucommander.os.notifier;

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
 *  <li>{@link GrowlNotifier}: for macOS that is installed with Growl
 *  <li>{@link SystemTrayNotifier}: otherwise, using the java.awt.SystemTray API
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class AbstractNotifier {

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
     * notification could be displayed. Unlike {@link #displayBackgroundNotification(NotificationType, String, String)}, the
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
     * @param notificationType one of the available notification types, see {@link NotificationType} for possible values
     * @param title the title of the notification to display
     * @param description the description of the notification to display
     * @return true if the notification was properly sent, false otherwise
     */
    public abstract boolean displayNotification(NotificationType notificationType, String title, String description);

    /**
     * Returns a pretty name for the underlying notification system that can be displayed to the end user.
     *
     * @return a pretty name for the underlying notification system
     */
    public abstract String getPrettyName();
}

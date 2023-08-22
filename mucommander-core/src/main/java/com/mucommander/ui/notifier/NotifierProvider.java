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

import java.awt.Color;
import java.awt.SystemTray;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.job.FileJob;
import com.mucommander.job.JobListener;
import com.mucommander.job.JobsManager;
import com.mucommander.os.notifier.AbstractNotifier;
import com.mucommander.os.notifier.NotificationType;
import com.mucommander.ui.main.WindowManager;

public class NotifierProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierProvider.class);

    /**
     * How often the dock icon should be updated (in ms).
     */
    private static final long UPDATE_ICON_INTERVAL = 1000L;

    private static AbstractNotifier defaultNotifier;

    private static AtomicBoolean jobsRegistered = new AtomicBoolean();

    static {
        // Finds and creates a suitable AbstractNotifier instance for the platform, if there is one
        if (SystemTray.isSupported()) {
            defaultNotifier = new SystemTrayNotifier();
        }
    }

    /**
     * Returns <code>true<code> if an AbstractNotifier instance is available. In other words, if <code>true</code> is
     * returned, {@link #getNotifier()} will return a non-null value.
     *
     * @return true if an AbstractNotifier instance is available
     */
    public static boolean isAvailable() {
        return getNotifier() != null;
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
        if (notifier != null) {
            return notifier;
        }
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
     * immediately returns (i.e. does not wait for pending events) and thus is not be able to return if the notification
     * was displayed or not, unlike {@link SystemTrayNotifier#displayNotification(NotificationType, String, String)}.
     * </p>
     *
     * @param notificationType one of the available notification types, see {@link NotificationType} for possible values
     * @param title the title of the notification to display
     * @param description the description of the notification to display
     */
    public static void displayBackgroundNotification(final NotificationType notificationType, final String title, final String description) {
        SwingUtilities.invokeLater(() -> {
            if (WindowManager.getCurrentMainFrame().isAncestorOfActiveWindow()) {
                LOGGER.debug("Ignoring notification, application is in foreground");
                return;
            }

            // bounce app icon if supported by OS
            DesktopManager.requestUserAttention();

            if (!getNotifier().displayNotification(notificationType, title, description)) {
                LOGGER.debug("Notification failed to be displayed");
            }
        });
    }

    /**
     * Displays a notification above the main frame that hide after provided time. If given notification is null or
     * blank it does nothing.
     *
     * @param mainFrame
     *            the main frame
     * @param bgColor
     *            background color (no alpha supported)
     * @param fgColor
     *            foreground color
     * @param notification
     *            the notification text
     * @param timeout
     *            the time-out in ms after which the notification disappears
     */
    public static void displayMainFrameNotification(JFrame mainFrame,
            String notification,
            Color bgColor,
            Color fgColor,
            long timeout) {
        SwingUtilities.invokeLater(() -> NotificationPopup.getInstance()
                .displayNotification(
                        mainFrame,
                        notification,
                        bgColor,
                        fgColor,
                        timeout));
    }

    public static void registerJobsListeners() {
        // register only once
        if (jobsRegistered.compareAndSet(false, true)) {
            JobsManager.getInstance().addJobListener(new JobListener() {
                long lastUpdate;

                // TODO abusing #jobProgress & #jobRemoved to get events to trigger desktop/taskbar icon updates
                @Override
                public void jobProgress(FileJob source, boolean fullUpdate) {
                    updateAppIcon();
                }

                @Override
                public void jobRemoved(FileJob source) {
                    updateAppIcon();
                }

                private void updateAppIcon() {
                    List<FileJob> jobs = JobsManager.getInstance().getAllJobs();
                    if (!jobs.isEmpty()) {
                        // Update icon every 1s
                        if (lastUpdate + UPDATE_ICON_INTERVAL < System.currentTimeMillis()) {
                            lastUpdate = System.currentTimeMillis();
                            long sum = 0;
                            int jobsCount = 0;
                            for (FileJob job : jobs) {
                                sum += job.getJobProgress().getTotalPercentInt();
                                jobsCount++;
                            }
                            DesktopManager.setIconBadgeNumber(jobsCount);
                            DesktopManager.setIconProgress((int) sum / jobsCount);
                        }
                    } else {
                        DesktopManager.setIconBadgeNumber(-1); // turn off progress bar on icon
                        DesktopManager.setIconProgress(-1); // turn off badge on icon
                    }
                }
            });
        }
    }
}

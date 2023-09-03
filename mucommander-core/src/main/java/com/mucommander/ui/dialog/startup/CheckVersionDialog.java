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

package com.mucommander.ui.dialog.startup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.VersionChecker;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.job.impl.SelfUpdateJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.GoToWebsiteAction;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.main.MainFrame;

/**
 * This class takes care of retrieving the information about the latest muCommander version from a remote server and
 * displaying the result to the end user.
 *
 * @author Maxence Bernard
 */
public class CheckVersionDialog extends QuestionDialog implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckVersionDialog.class);

    /**
     * Parent MainFrame instance
     */
    private MainFrame mainFrame;

    /**
     * true if the user manually clicked on the 'Check for updates' menu item,
     * false if the update check was automatically triggered on startup
     */
    private boolean userInitiated;

    /**
     * Dialog's width has to be at least 240
     */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320, 0);

    public enum CheckVersionAction implements DialogAction {

        OK(Translator.get("ok")),
        // TODO not nice (to load the name into enum and in this way)
        GO_TO_WEBSITE(ActionProperties.getActionLabel(ActionType.GoToWebsite)),
        INSTALL_AND_RESTART("COMMENTED-OUT");

        private final String actionName;

        CheckVersionAction(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public String getActionName() {
            return actionName;
        }
    }

    /**
     * Checks for updates and notifies the user of the outcome. The check itself is performed in a separate thread
     * to prevent the app from waiting for the request's result.
     *
     * @param userInitiated true if the user manually clicked on the 'Check for updates' menu item,
     *                      false if the update check was automatically triggered on startup. If the check was automatically triggered,
     *                      the user won't be notified if there is no new version (current version is the latest).
     */
    public CheckVersionDialog(MainFrame mainFrame, boolean userInitiated) {
        super(mainFrame.getJFrame(), "", mainFrame.getJFrame());
        this.mainFrame = mainFrame;
        this.userInitiated = userInitiated;

        // Do all the hard work in a separate thread
        new Thread(this, "CheckVersionDialog").start();
    }


    /**
     * Checks for updates and notifies the user of the outcome.
     */
    public void run() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        String message;
        String title;
        VersionChecker version;
        URL downloadURL = null;
        boolean downloadOption = false;
        String jarURL = null;

        try {
            LOGGER.debug("Checking for new version...");

            version = VersionChecker.getInstance();
            // A newer version is available
            if (version.isNewVersionAvailable()) {
                LOGGER.info("A new version is available!");

                title = Translator.get("version_dialog.new_version_title");

                // Checks if the current platform can open a new browser window
                downloadURL = new URL(version.getDownloadURL());
                downloadOption = DesktopManager.isOperationSupported(DesktopManager.BROWSE, new Object[]{downloadURL});

                // If the platform is not capable of opening a new browser window,
                // display the download URL.
                if (downloadOption) {
                    message = Translator.get("version_dialog.new_version");
                } else {
                    message = Translator.get("version_dialog.new_version_url", downloadURL.toString());
                }

                jarURL = version.getJarURL();
            }
            // We're already running latest version
            else {
                LOGGER.debug("No new version.");

                // If the version check was not iniated by the user (i.e. was automatic),
                // we do not need to inform the user that he already has the latest version
                if (!userInitiated) {
                    dispose();
                    return;
                }

                title = Translator.get("version_dialog.no_new_version_title");
                message = Translator.get("version_dialog.no_new_version");
            }
        }
        // Check failed
        catch (Exception e) {
            // If the version check was not initiated by the user (i.e. was automatic),
            // we do not need to inform the user that the check failed
            if (!userInitiated) {
                dispose();
                return;
            }

            title = Translator.get("version_dialog.not_available_title");
            message = Translator.get("version_dialog.not_available");
        }

        // Set title
        setTitle(title);

        List<DialogAction> actions = new ArrayList<>();
        actions.add(CheckVersionAction.OK);

        // 'Go to website' choice (if available)
        if (downloadOption) {
            actions.add(CheckVersionAction.GO_TO_WEBSITE);
        }

//        // 'Install and restart' choice (if available)
//        if(jarURL!=null) {
//            actionsV.add(new Integer(INSTALL_AND_RESTART_ACTION));
//            labelsV.add(Translator.get("version_dialog.install_and_restart"));
//        }

        init(new InformationPane(message, null, Font.PLAIN, InformationPane.INFORMATION_ICON),
                actions,
                0);

        JCheckBox showNextTimeCheckBox = new JCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup"),
                MuConfigurations.getPreferences().getVariable(MuPreference.CHECK_FOR_UPDATE,
                        MuPreferences.DEFAULT_CHECK_FOR_UPDATE));
        addComponent(showNextTimeCheckBox);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);

        // Show dialog and get user action
        DialogAction action = getActionValue();

        if (action == CheckVersionAction.GO_TO_WEBSITE) {
            try {
                DesktopManager.executeOperation(DesktopManager.BROWSE, new Object[]{downloadURL});
            } catch (Exception e) {
                InformationDialog.showErrorDialog(this);
            }
        } else if (action == CheckVersionAction.INSTALL_AND_RESTART) {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("Installing new version"));
            SelfUpdateJob job = new SelfUpdateJob(progressDialog, mainFrame, FileFactory.getFile(jarURL));
            progressDialog.start(job);
        }

        // Remember user preference
        MuConfigurations.getPreferences().setVariable(MuPreference.CHECK_FOR_UPDATE, showNextTimeCheckBox.isSelected());
    }
}

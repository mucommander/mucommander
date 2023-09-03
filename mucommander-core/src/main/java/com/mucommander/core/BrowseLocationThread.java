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

package com.mucommander.core;

import java.awt.Cursor;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.CachedFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.auth.AuthDialog;
import com.mucommander.ui.dialog.file.DownloadDialog;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import static com.mucommander.ui.dialog.QuestionDialog.DIALOG_DISPOSED_ACTION;

/**
 * This thread takes care of changing current folder without locking the main
 * thread. The folder change can be cancelled.
 *
 * <p>A little note out of nowhere: never ever call JComponent.paintImmediately() from a thread
 * other than the Event Dispatcher Thread, as will create nasty repaint glitches that
 * then become very hard to track. Sun's Javadoc doesn't make it clear enough... just don't!</p>
 *
 * @author Maxence Bernard
 */
public class BrowseLocationThread extends ChangeFolderThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseLocationThread.class);

    public enum BrowseLocationThreadAction implements DialogAction {

        CANCEL("cancel"),
        BROWSE("browse"),
        DOWNLOAD("download");

        private final String actionName;

        BrowseLocationThreadAction(String actionKey) {
            // here or when in #getActionName
            this.actionName = Translator.get(actionKey);
        }

        @Override
        public String getActionName() {
            return actionName;
        }
    }

    private AbstractFile folder;
    private boolean findWorkableFolder;
    private boolean changeLockedTab;
    private AbstractFile fileToSelect;
    private CredentialsMapping credentialsMapping;

    private MainFrame mainFrame;
    private FolderPanel folderPanel;
    private LocationChanger locationChanger;

    private GlobalLocationHistory globalHistory = GlobalLocationHistory.Instance();

    private boolean disposed;

    public BrowseLocationThread(AbstractFile folder, boolean findWorkableFolder, boolean changeLockedTab,
            MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        this(folder.getURL(), mainFrame, folderPanel, locationManager, locationChanger);
        // Ensure that we work on a raw file instance and not a cached one
        this.folder = (folder instanceof CachedFile)?((CachedFile)folder).getProxiedFile():folder;
        this.findWorkableFolder = findWorkableFolder;
        this.changeLockedTab = changeLockedTab;
    }

    /**
     * 
     * @param folderURL
     * @param credentialsMapping the CredentialsMapping to use for accessing the folder, <code>null</code> for none
     * @param changeLockedTab
     */
    public BrowseLocationThread(FileURL folderURL, CredentialsMapping credentialsMapping, boolean changeLockedTab,
            MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        this(folderURL, mainFrame, folderPanel, locationManager, locationChanger);
        this.changeLockedTab = changeLockedTab;
        this.credentialsMapping = credentialsMapping;
    }

    private BrowseLocationThread(FileURL folderURL, MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        super(locationManager, folderURL);
        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;
        this.locationChanger = locationChanger;
        setPriority(Thread.MAX_PRIORITY);
    }

    /**
     * Sets the file to be selected after the folder has been changed, <code>null</code> for none.
     *
     * @param fileToSelect the file to be selected after the folder has been changed
     */
    public void selectThisFileAfter(AbstractFile fileToSelect) {
        this.fileToSelect = fileToSelect;
    }

    /**
     * Returns <code>true</code> if the given file should have its canonical path followed. In that case, the
     * AbstractFile instance must be resolved again.
     *
     * <p>HTTP files MUST have their canonical path followed. For all other file protocols, this is an option in
     * the preferences.</p>
     *
     * @param file the file to test
     * @return <code>true</code> if the given file should have its canonical path followed
     */
    private boolean followCanonicalPath(AbstractFile file) {
        return (MuConfigurations.getPreferences().getVariable(MuPreference.CD_FOLLOWS_SYMLINKS, MuPreferences.DEFAULT_CD_FOLLOWS_SYMLINKS)
                || file.getURL().getScheme().equals(FileProtocols.HTTP))
                && !file.getAbsolutePath(false).equals(file.getCanonicalPath(false));
    }

    @Override
    public void run() {
        LOGGER.debug("starting folder change...");
        boolean folderChangedSuccessfully = false;

        // Show some progress in the progress bar to give hope
        folderPanel.setProgressValue(10);

        boolean userCancelled = false;
        CredentialsMapping newCredentialsMapping = null;
        // True if Guest authentication was selected in the authentication dialog (guest credentials must not be
        // added to CredentialsManager)
        boolean guestCredentialsSelected = false;

        AuthenticationType authenticationType = folderURL.getAuthenticationType();
        if(credentialsMapping!=null) {
            newCredentialsMapping = credentialsMapping;
            CredentialsManager.authenticate(folderURL, newCredentialsMapping);
        }
        // If the URL doesn't contain any credentials and authentication for this file protocol is required, or
        // optional and CredentialsManager has credentials for this location, popup the authentication dialog to
        // avoid waiting for an AuthException to be thrown.
        // There is no need to get credentials from users for locations that we already have non-password based
        // credentials for in CredentialsManager, like SFTP with a private key
        else if(!folderURL.containsCredentials() && !isSftpWithPrivateKey() &&
                (  (authenticationType==AuthenticationType.AUTHENTICATION_REQUIRED)
                        || (authenticationType==AuthenticationType.AUTHENTICATION_OPTIONAL && CredentialsManager.getMatchingCredentials(folderURL).length>0))) {
            AuthDialog authDialog = popAuthDialog(folderURL, false, null);
            newCredentialsMapping = authDialog.getCredentialsMapping();
            guestCredentialsSelected = authDialog.guestCredentialsSelected();

            // User cancelled the authentication dialog, stop
            if(newCredentialsMapping ==null)
                userCancelled = true;
            // Use the provided credentials and invalidate the folder AbstractFile instance (if any) so that
            // it gets recreated with the new credentials
            else {
                CredentialsManager.authenticate(folderURL, newCredentialsMapping);
                folder = null;
            }
        }

        if(!userCancelled) {
            boolean canonicalPathFollowed = false;

            do {
                // Set cursor to hourglass/wait
                mainFrame.getJFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                // Render all actions inactive while changing folder
                mainFrame.setNoEventsMode(true);

                try {
                    // 2 cases here :
                    // - Thread was created using an AbstractFile instance
                    // - Thread was created using a FileURL, corresponding AbstractFile needs to be resolved

                    // Thread was created using a FileURL
                    if(folder==null) {
                        AbstractFile file = FileFactory.getFile(folderURL, true);

                        synchronized(KILL_LOCK) {
                            if(killed) {
                                LOGGER.debug("this thread has been killed, returning");
                                break;
                            }
                        }

                        // File resolved -> 25% complete
                        folderPanel.setProgressValue(25);

                        // Popup an error dialog and abort folder change if the file could not be resolved
                        // or doesn't exist
                        if(file==null || !file.exists()) {
                            // Restore default cursor
                            mainFrame.getJFrame().setCursor(Cursor.getDefaultCursor());

                            locationChanger.showFolderDoesNotExistDialog();
                            break;
                        }

                        if (!file.canRead()) {
                            // Restore default cursor
                            mainFrame.getJFrame().setCursor(Cursor.getDefaultCursor());

                            showFailedToReadFolderDialog();
                            break;
                        }

                        // File is a regular directory, all good
                        if(file.isDirectory()) {
                            // Just continue
                        }
                        // File is a browsable file (Zip archive for instance) but not a directory : Browse or Download ? => ask the user
                        else if(file.isBrowsable()) {
                            // If history already contains this file, do not ask the question again and assume
                            // the user wants to 'browse' the file. In particular, this prevent the 'Download or browse'
                            // dialog from popping up when going back or forward in history.
                            // The dialog is also not displayed if the file corresponds to the currently selected file,
                            // which is a weak (and not so accurate) way to know if the folder change is the result
                            // of the OpenAction (enter pressed on the file). This works well enough in practice.
                            if(!globalHistory.contains(folderURL) && !file.equals(folderPanel.getFileTable().getSelectedFile())) {
                                // Restore default cursor
                                mainFrame.getJFrame().setCursor(Cursor.getDefaultCursor());

                                // Download or browse file ?
                                QuestionDialog dialog = new QuestionDialog(mainFrame.getJFrame(),
                                        null,
                                        Translator.get("table.download_or_browse"),
                                        mainFrame.getJFrame(),
                                        Arrays.asList(BrowseLocationThreadAction.BROWSE,
                                                BrowseLocationThreadAction.DOWNLOAD,
                                                BrowseLocationThreadAction.CANCEL),
                                        0);

                                DialogAction ret = dialog.getActionValue();

                                if(ret==DIALOG_DISPOSED_ACTION || ret==BrowseLocationThreadAction.CANCEL)
                                    break;

                                // Download file
                                if(ret==BrowseLocationThreadAction.DOWNLOAD) {
                                    showDownloadDialog(file);
                                    break;
                                }
                                // Continue if BROWSE
                                // Set cursor to hourglass/wait
                                mainFrame.getJFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                            }
                            // else just continue and browse file's contents
                        }
                        // File is a regular file: show download dialog which allows to download (copy) the file
                        // to a directory specified by the user
                        else {
                            showDownloadDialog(file);
                            break;
                        }

                        this.folder = file;
                    }
                    // Thread was created using an AbstractFile instance, check file existence
                    else if(!folder.exists()) {
                        // Find a 'workable' folder if the requested folder doesn't exist anymore
                        if(findWorkableFolder) {
                            AbstractFile newFolder = locationChanger.getWorkableFolder(folder);
                            if(newFolder.equals(folder)) {
                                // If we've already tried the returned folder, give up (avoids a potentially endless loop)
                                locationChanger.showFolderDoesNotExistDialog();
                                break;
                            }

                            // Try again with the new folder
                            folder = newFolder;
                            folderURL = folder.getURL();
                            // Discard the file to select, if any
                            fileToSelect = null;

                            continue;
                        }
                        else {
                            locationChanger.showFolderDoesNotExistDialog();
                            break;
                        }
                    }
                    else if (!folder.canRead()) {
                        showFailedToReadFolderDialog();
                        break;
                    }

                    // Checks if canonical should be followed. If that is the case, the file is invalidated
                    // and resolved again. This happens only once at most, to avoid a potential infinite loop
                    // in the event that the absolute path still didn't match canonical one after the file is
                    // resolved again.
                    if(!canonicalPathFollowed && followCanonicalPath(folder)) {
                        try {
                            // Recreate the FileURL using the file's canonical path
                            FileURL newURL = FileURL.getFileURL(folder.getCanonicalPath());
                            // Keep the credentials and properties (if any)
                            newURL.setCredentials(folderURL.getCredentials());
                            newURL.importProperties(folderURL);
                            this.folderURL = newURL;
                            // Invalidate the AbstractFile instance
                            this.folder = null;
                            // There won't be any further attempts after this one
                            canonicalPathFollowed = true;

                            // Loop the resolve the file
                            continue;
                        }
                        catch(MalformedURLException e) {
                            // In the unlikely event of the canonical path being malformed, the AbstractFile
                            // and FileURL instances are left untouched
                        }
                    }

                    synchronized(KILL_LOCK) {
                        if(killed) {
                            LOGGER.debug("this thread has been killed, returning");
                            break;
                        }
                    }

                    // File tested -> 50% complete
                    folderPanel.setProgressValue(50);

                    /* TODO branch 
                        AbstractFile children[] = new AbstractFile[0];
                        if (branchView) {
                            childrenList = new ArrayList();
                            readBranch(folder);
                            children = (AbstractFile[]) childrenList.toArray(children);
                        } else {
                            children = folder.ls(chainedFileFilter);                            
                        }*/ 

                    synchronized(KILL_LOCK) {
                        if(killed) {
                            LOGGER.debug("this thread has been killed, returning");
                            break;
                        }
                        // From now on, thread cannot be killed (would comprise table integrity)
                        doNotKill = true;
                    }

                    // files listed -> 75% complete
                    folderPanel.setProgressValue(75);

                    LOGGER.trace("calling setCurrentFolder");

                    // Change the file table's current folder and select the specified file (if any)
                    locationChanger.setCurrentFolder(folder, fileToSelect, changeLockedTab, true);

                    // folder set -> 95% complete
                    folderPanel.setProgressValue(95);

                    // If new credentials were entered by the user, these can now be considered valid
                    // (folder was changed successfully), so we add them to the CredentialsManager.
                    // Do not add the credentials if guest credentials were selected by the user.
                    if(newCredentialsMapping!=null && !guestCredentialsSelected)
                        CredentialsManager.addCredentials(newCredentialsMapping);

                    // All good !
                    folderChangedSuccessfully = true;

                    break;
                }
                catch(Exception e) {
                    LOGGER.error("failed to browse location {}", folderURL);
                    LOGGER.debug("failed to browse location", e);

                    if(killed) {
                        // If #tryKill() called #interrupt(), the exception we just caught was most likely
                        // thrown as a result of the thread being interrupted.
                        //
                        // The exception can be a java.lang.InterruptedException (Thread throws those),
                        // a java.nio.channels.ClosedByInterruptException (InterruptibleChannel throws those)
                        // or any other exception thrown by some code that swallowed the original exception
                        // and threw a new one.

                        LOGGER.debug("Thread was interrupted, ignoring exception");
                        break;
                    }

                    // Restore default cursor
                    mainFrame.getJFrame().setCursor(Cursor.getDefaultCursor());

                    if(e instanceof AuthException) {
                        AuthException authException = (AuthException)e;
                        // Retry (loop) if user provided new credentials, if not stop
                        AuthDialog authDialog = popAuthDialog(authException.getURL(), true, authException.getMessage());
                        newCredentialsMapping = authDialog.getCredentialsMapping();
                        guestCredentialsSelected = authDialog.guestCredentialsSelected();

                        if(newCredentialsMapping!=null) {
                            // Invalidate the existing AbstractFile instance
                            folder = null;
                            // Use the provided credentials
                            CredentialsManager.authenticate(folderURL, newCredentialsMapping);
                            continue;
                        }
                    }
                    else {
                        // Find a 'workable' folder if the requested folder doesn't exist anymore
                        if(findWorkableFolder) {
                            AbstractFile newFolder = locationChanger.getWorkableFolder(folder);
                            if(newFolder.equals(folder)) {
                                // If we've already tried the returned folder, give up (avoids a potentially endless loop)
                                locationChanger.showFolderDoesNotExistDialog();
                                break;
                            }

                            // Try again with the new folder
                            folder = newFolder;
                            folderURL = folder.getURL();
                            // Discard the file to select, if any
                            fileToSelect = null;

                            continue;
                        }

                        showAccessErrorDialog(e);
                    }

                    // Stop looping!
                    break;
                }
            }
            while(true);
        }

        synchronized(KILL_LOCK) {
            // Clean things up
            cleanup(folderChangedSuccessfully);
        }
    }

    protected void cleanup(boolean folderChangedSuccessfully) {
        // Ensures that this method is called only once
        synchronized(KILL_LOCK) {
            if(disposed) {
                LOGGER.debug("already called, returning");
                return;
            }

            disposed = true;
        }

        LOGGER.trace("cleaning up, folderChangedSuccessfully="+folderChangedSuccessfully);

        // Clear the interrupted flag in case this thread has been killed using #interrupt().
        // Not doing this could cause some of the code called by this method to be interrupted (because this thread
        // is interrupted) and throw an exception
        interrupted();

        // Reset location field's progress bar
        folderPanel.setProgressValue(0);

        // Restore normal mouse cursor
        mainFrame.getJFrame().setCursor(Cursor.getDefaultCursor());

        locationChanger.cleanChangeFolderThread();

        // Make all actions active again
        mainFrame.setNoEventsMode(false);

        if(!folderChangedSuccessfully) {
            FileURL failedURL = folder==null?folderURL:folder.getURL();
            // Notifies listeners that location change has been cancelled by the user or has failed
            if(killed)
                locationManager.fireLocationCancelled(failedURL);
            else
                locationManager.fireLocationFailed(failedURL);
        }
    }

    private void showFailedToReadFolderDialog() {
        InformationDialog.showErrorDialog(mainFrame.getJFrame(), Translator.get("table.folder_access_error_title"), Translator.get("failed_to_read_folder"));
    }


    /**
     * Displays a popup dialog informing the user that the requested folder couldn't be opened.
     *
     * @param e the Exception that was caught while changing the folder
     */
    private void showAccessErrorDialog(Exception e) {
        InformationDialog.showErrorDialog(mainFrame.getJFrame(), Translator.get("table.folder_access_error_title"), Translator.get("table.folder_access_error"), e==null?null:e.getMessage(), e);
    }


    /**
     * Pops up an {@link AuthDialog authentication dialog} prompting the user to select or enter credentials in order to
     * be granted the access to the file or folder represented by the given {@link FileURL}.
     * The <code>AuthDialog</code> instance is returned, allowing to retrieve the credentials that were selected
     * by the user (if any).
     *
     * @param fileURL the file or folder to ask credentials for
     * @param errorMessage optional (can be null), an error message describing a prior authentication failure
     * @return the AuthDialog that contains the credentials selected by the user (if any)
     */
    private AuthDialog popAuthDialog(FileURL fileURL, boolean authFailed, String errorMessage) {
        AuthDialog authDialog = new AuthDialog(mainFrame, fileURL, authFailed, errorMessage);
        authDialog.showDialog();
        return authDialog;
    }


    /**
     * Displays a download dialog box where the user can choose where to download the given file or cancel
     * the operation.
     *
     * @param file the file to download
     */
    private void showDownloadDialog(AbstractFile file) {
        FileSet fileSet = new FileSet(locationManager.getCurrentFolder());
        fileSet.add(file);
        
        // Show confirmation/path modification dialog
        new DownloadDialog(mainFrame, fileSet).showDialog();
    }

    private boolean isSftpWithPrivateKey() {
        boolean sftpWithPrivateKey = false;
        CredentialsMapping[] matchingCredentials = CredentialsManager.getMatchingCredentials(this.folderURL);
        for (int i = 0; i < matchingCredentials.length; i++) {
            boolean isSftp = matchingCredentials[i].getRealm().getScheme().equals(FileProtocols.SFTP);
            boolean hasPrivateKey = matchingCredentials[i].getRealm().getProperty("privateKeyPath") != null;
            sftpWithPrivateKey = isSftp && hasPrivateKey;
            if (sftpWithPrivateKey) {
                break;
            }
        }
        return sftpWithPrivateKey;
    }
}

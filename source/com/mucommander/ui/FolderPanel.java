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


package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.MappedCredentials;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.file.*;
import com.mucommander.file.filter.AndFileFilter;
import com.mucommander.file.filter.DSStoreFileFilter;
import com.mucommander.file.filter.HiddenFileFilter;
import com.mucommander.file.filter.SystemFileFilter;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.auth.AuthDialog;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.ui.dnd.FileDragSourceListener;
import com.mucommander.ui.dnd.FileDropTargetListener;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FolderChangeMonitor;
import com.mucommander.ui.table.TablePopupMenu;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * Folder pane that contains the table that displays the contents of the current directory and allows navigation, the
 * drive button, and the location field.
 *
 * @author Maxence Bernard
 */
public class FolderPanel extends JPanel implements FocusListener, ConfigurationListener, ThemeListener {

    private MainFrame  mainFrame;

    private AbstractFile currentFolder;
    private ChangeFolderThread changeFolderThread;

    private FolderChangeMonitor folderChangeMonitor;

    private LocationManager locationManager = new LocationManager(this);

    /*  We're NOT using JComboBox anymore because of its strange behavior:
        it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
        so there is no way to tell if it's the final selection (ENTER) or not.
    */
    private DrivePopupButton driveButton;
    private LocationComboBox locationComboBox;
    private ProgressTextField locationField;
    private FileTable fileTable;
    private JScrollPane scrollPane;
	
    private FolderHistory folderHistory = new FolderHistory(this);
    
    private FileDragSourceListener fileDragSourceListener;

    private Color backgroundColor;
    private Color unfocusedBackgroundColor;

    /** Contains all the registered FileFilter instances (if any) used to filter out unwanted files when listing
     * folder contents */
    private AndFileFilter chainedFileFilter;

    private final static int CANCEL_ACTION = 0;
    private final static int BROWSE_ACTION = 1;
    private final static int DOWNLOAD_ACTION = 2;
	
    private final static String CANCEL_TEXT = Translator.get("cancel");
    private final static String BROWSE_TEXT = Translator.get("browse");
    private final static String DOWNLOAD_TEXT = Translator.get("download");


    public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
        super(new BorderLayout());

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(" initialFolder="+initialFolder);
        this.mainFrame = mainFrame;

        // No decoration for this panel
        setBorder(null);

        JPanel locationPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        // Create and add drive button
        this.driveButton = new DrivePopupButton(this);
        c.weightx = 0;
        c.gridx = 0;        
        locationPanel.add(driveButton, c);

        // Create location combo box and retrieve location field instance
        this.locationComboBox = new LocationComboBox(this);
        this.locationField = (ProgressTextField)locationComboBox.getTextField();

        // Give location field all the remaining space
        c.weightx = 1;
        c.gridx = 1;
        // Add some space between drive button and location combo box (none by default)
        c.insets = new Insets(0, 4, 0, 0);
        locationPanel.add(locationComboBox, c);

        add(locationPanel, BorderLayout.NORTH);

        // Create the FileTable
        fileTable = new FileTable(mainFrame, this);

        // Init chained file filters used to filter out files in the current directory.
        // AndFileFilter is used, that means files must satisfy all the filters in order to be displayed.
        chainedFileFilter = new AndFileFilter();

        // Filters out hidden files, null when 'show hidden files' option is enabled
        if(!ConfigurationManager.getVariableBoolean(ConfigurationVariables.SHOW_HIDDEN_FILES, ConfigurationVariables.DEFAULT_SHOW_HIDDEN_FILES))
            chainedFileFilter.addFileFilter(new HiddenFileFilter());

        // Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled
        if(!ConfigurationManager.getVariableBoolean(ConfigurationVariables.SHOW_DS_STORE_FILES, ConfigurationVariables.DEFAULT_SHOW_DS_STORE_FILES))
            chainedFileFilter.addFileFilter(new DSStoreFileFilter());

        /** Filters out Mac OS X system folders, null when 'show system folders' option is enabled */
        if(!ConfigurationManager.getVariableBoolean(ConfigurationVariables.SHOW_SYSTEM_FOLDERS, ConfigurationVariables.DEFAULT_SHOW_SYSTEM_FOLDERS))
            chainedFileFilter.addFileFilter(new SystemFileFilter());

        try {
            // Set initial folder to current directory
            setCurrentFolder(initialFolder, initialFolder.ls(chainedFileFilter), null);
        }
        catch(Exception e) {
            AbstractFile rootFolders[] = RootFolders.getRootFolders();
            // If that failed, try to read any other drive
            for(int i=0; i<rootFolders.length; i++) {
                try  {
                    setCurrentFolder(rootFolders[i], rootFolders[i].ls(chainedFileFilter), null);
                    break;
                }
                catch(IOException e2) {
                    if (i==rootFolders.length-1) {
                        // Now we're screwed
                        throw new RuntimeException("Unable to read any drive");
                    }
                }					
            }
        }

        // Create the FolderChangeMonitor that monitors changes in the current folder and automatically refreshes it
        folderChangeMonitor = new FolderChangeMonitor(this);

        // Put the FileTable in a scroll pane with vertical scrolling when needed and no horizontal scrolling
        scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Sets the table border.
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getCurrentColor(Theme.FILE_TABLE_BORDER_COLOR), 1));

        // Set scroll pane's background color to match the one of this panel and FileTable
        scrollPane.getViewport().setBackground(unfocusedBackgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR));
        backgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR);

        // Catch mouse events on the ScrollPane
        scrollPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // Left-click requests focus on the FileTable
                if (PlatformManager.isLeftMouseButton(e)) {
                    fileTable.requestFocus();
                }
                // Right-click brings a contextual popup menu
                else if (PlatformManager.isRightMouseButton(e)) {
                    AbstractFile currentFolder = getCurrentFolder();
                    new TablePopupMenu(FolderPanel.this.mainFrame, currentFolder, null, false, fileTable.getFileTableModel().getMarkedFiles()).show(scrollPane, e.getX(), e.getY());
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        // Listens to some configuration variables
        ConfigurationManager.addConfigurationListener(this);
        ThemeManager.addCurrentThemeListener(this);

        // Listen to focus event in order to notify MainFrame of changes of the current active panel/table
        fileTable.addFocusListener(this);
        locationField.addFocusListener(this);

        // Drag and Drop support

        // Enable drag support on the FileTable
        this.fileDragSourceListener = new FileDragSourceListener(this);
        fileDragSourceListener.enableDrag(fileTable);

        // Enable drop support to copy/move/change current folder when files are dropped on the FileTable
        FileDropTargetListener dropTargetListener = new FileDropTargetListener(this, false);
        fileTable.setDropTarget(new DropTarget(fileTable, dropTargetListener));
        scrollPane.setDropTarget(new DropTarget(scrollPane, dropTargetListener));

        // Allow the location field to change the current directory when a file/folder is dropped on it
        dropTargetListener = new FileDropTargetListener(this, true);
        locationField.setDropTarget(new DropTarget(locationField, dropTargetListener));
        driveButton.setDropTarget(new DropTarget(driveButton, dropTargetListener));
    }


    public FileDragSourceListener getFileDragSourceListener() {
        return this.fileDragSourceListener;
    }


    /**
     * Returns the MainFrame instance that contains this panel.
     */
    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    /**
     * Returns the FileTable component this panel contains.
     */
    public FileTable getFileTable() {
        return this.fileTable;
    }

    /**
     * Returns the JScrollPane that contains the FileTable component.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Returns the LocationComboBox component this panel contains.
     */
    public LocationComboBox getLocationComboBox() {
        return locationComboBox;
    }

    /**
     * Returns the DrivePopupButton component this panel contains.
     */
    public DrivePopupButton getDriveButton() {
        return driveButton; 
    }

    /**
     * Returns the visited folders history wrapped in a FolderHistory object.
\    */
    public FolderHistory getFolderHistory() {
        return this.folderHistory;
    }

    /**
     * Returns the LocationManager instance that notifies registered listeners of location changes
     * that occur in this FolderPanel. 
     */
    public LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * Allows the user to easily change the current folder and type a new one: requests focus 
     * on the location field and selects the folder string.
     */
    public void changeCurrentLocation() {
        locationField.selectAll();
        locationField.requestFocus();
    }
	

    /**
     * Returns the folder that is currently being displayed by this FolderPanel.
     */
    public synchronized AbstractFile getCurrentFolder() {
        return currentFolder;
    }


    /**
     * Displays a popup dialog informing the user that the requested folder doesn't exist or isn't available.
     */
    private void showFolderDoesNotExistDialog() {
        JOptionPane.showMessageDialog(mainFrame, Translator.get("folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Displays a popup dialog informing the user that the requested folder couldn't be opened.
     */
    private void showAccessErrorDialog(IOException e) {
        String exceptionMsg = e==null?null:e.getMessage();
        String errorMsg = Translator.get("table.folder_access_error")+(exceptionMsg==null?"":": "+exceptionMsg);

        JOptionPane.showMessageDialog(mainFrame, errorMsg, Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Pops up an {@link AuthDialog} where the user can enter credentials to grant him access to the file or folder
     * represented by the given {@link FileURL}, and returns the credentials entered or null if the dialog was cancelled.
     *
     * @param fileURL the file or folder to ask credentials for
     * @param errorMessage optional (can be null), an error message sent by the server to display to the user
     * @return the credentials the user entered/chose and validated, null if he cancelled the dialog.
     */
    private MappedCredentials getCredentialsFromUser(FileURL fileURL, String errorMessage) {
        AuthDialog authDialog = new AuthDialog(mainFrame, fileURL, errorMessage);
        authDialog.showDialog();
        return authDialog.getCredentials();
    }


    /**
     * Displays a download dialog box where the user can choose where to download the given file
     * or cancel the operation.
     */
    private void showDownloadDialog(AbstractFile file) {
        FileSet fileSet = new FileSet(currentFolder);
        fileSet.add(file);
		
        // Show confirmation/path modification dialog
        new DownloadDialog(mainFrame, fileSet);
    }
	


    /**
     * Tries to change current folder to the new specified one. 
     * The user is notified by a dialog if the folder could not be changed.
     * 
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param folder folder to be made current folder. If folder is null or doesn't exist, a dialog will popup and inform the user
     */
    public synchronized void tryChangeCurrentFolder(AbstractFile folder) {
        tryChangeCurrentFolder(folder, null);
    }

    /**
     * Tries to change current folder to the new specified one, and select the given file after the folder has been changed.
     * The user is notified by a dialog if the folder could not be changed.
     * 
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param folder folder to be made current folder. If folder is null or doesn't exist, a dialog will popup and inform the user
     * @param selectThisFileAfter file to be selected after the folder has been changed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file 
     */
    public synchronized void tryChangeCurrentFolder(AbstractFile folder, AbstractFile selectThisFileAfter) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folder="+folder+" selectThisFileAfter"+selectThisFileAfter, 3);

        // Make sure there is not an existing thread running,
        // this should not normally happen but if it does, report the error
        if(changeFolderThread!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread, -1);
            return;
        }
		
        this.changeFolderThread = new ChangeFolderThread(folder);

        if(selectThisFileAfter!=null)
            this.changeFolderThread.selectThisFileAfter(selectThisFileAfter);

        changeFolderThread.start();
    }

    /**
     * Tries to change current folder to the new specified path.
     * The user is notified by a dialog if the folder could not be changed.
     * 
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param folderPath folder's path to be made current folder. If this path does not resolve into an existing file, an error message will be displayed
     */
    public synchronized void tryChangeCurrentFolder(String folderPath) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folderPath="+folderPath, 3);

        // Make sure there is not an existing thread running,
        // this should not normally happen but if it does, report the error
        if(changeFolderThread!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread, -1);
            return;
        }

        try {
            this.changeFolderThread = new ChangeFolderThread(new FileURL(folderPath));
            changeFolderThread.start();
        }
        catch(MalformedURLException e) {
            // FileURL could not be resolved, notify the user that the folder doesn't exist
            showFolderDoesNotExistDialog();
        }
    }

    /**
     * Tries to change current folder to the new specified URL.
     * The user is notified by a dialog if the folder could not be changed.
     * 
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param folderURL folder's URL to be made current folder. If this URL does not resolve into an existing file, an error message will be displayed
     */
    public synchronized void tryChangeCurrentFolder(FileURL folderURL) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folderURL="+folderURL, 3);

        // Make sure there is not an existing thread running,
        // this should not normally happen but if it does, report the error
        if(changeFolderThread!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread, -1);
            return;
        }

        this.changeFolderThread = new ChangeFolderThread(folderURL);
        changeFolderThread.start();
    }

    /**
     * Refreshes current folder's contents and notifies the user if current folder could not be refreshed.
     *
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     */
    public synchronized void tryRefreshCurrentFolder() {
        tryChangeCurrentFolder(currentFolder, null);
    }

    /**
     * Refreshes current folder's contents and notifies the user if current folder could not be refreshed.
     *
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param selectThisFileAfter file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file 
     */
    public synchronized void tryRefreshCurrentFolder(AbstractFile selectThisFileAfter) {
        tryChangeCurrentFolder(currentFolder, selectThisFileAfter);
    }
		
    /**
     * Refreshes current folder's contents in the same thread and throws an IOException if current folder could not be refreshed.
     *
     * @throws IOException if current folder could not be refreshed.
     */
    public synchronized void refreshCurrentFolder() throws IOException {
        setCurrentFolder(currentFolder, currentFolder.ls(chainedFileFilter), null);
    }


    /**
     * Changes current folder using the given folder and children files.
     *
     * @param folder folder to be made current folder
     * @param children current folder's files (value of folder.ls())
     * @param fileToSelect file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
     */
    private synchronized void setCurrentFolder(AbstractFile folder, AbstractFile children[], AbstractFile fileToSelect) {
        fileTable.setCurrentFolder(folder, children);

        // Select given file if not null
        if(fileToSelect!=null)
            fileTable.selectFile(fileToSelect);

        this.currentFolder = folder;

        // Add folder to history
        folderHistory.addToHistory(folder);

        // Notify listeners that location has changed
        locationManager.fireLocationChanged(folder.getURL());
    }


    /**
     * Returns true if the current folder is currently being changed, false otherwise.
     */
    public boolean isFolderChanging() {
        return changeFolderThread!=null;
    }


    /**
     * Returns the thread that is currently changing the current folder, or null is the folder is not being changed.
     */
    public ChangeFolderThread getChangeFolderThread() {
        return changeFolderThread;
    }


    /**
     * Returns the FolderChangeMonitor which monitors changes in the current folder and automatically refreshes it.
     */
    public FolderChangeMonitor getFolderChangeMonitor() {
        return folderChangeMonitor;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overridden for debugging purposes.
     */
    public String toString() {
        return getClass().getName()+"@"+hashCode() +" currentFolder="+currentFolder+" hasFocus="+hasFocus();
    }


    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////
	
    public void focusGained(FocusEvent e) {
        // Notify MainFrame that we are in control now! (our table/location field is active)
        mainFrame.setActiveTable(fileTable);
        if(e.getSource() == fileTable)
            scrollPane.getViewport().setBackground(backgroundColor);
    }

    public void focusLost(FocusEvent e) {
        if(e.getSource() == fileTable)
            scrollPane.getViewport().setBackground(unfocusedBackgroundColor);
    }
	
	 
    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /** 
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();

        // Show or hide hidden files
        if (var.equals(ConfigurationVariables.SHOW_HIDDEN_FILES)) {
            if(event.getBooleanValue())
                removeFileFilter(HiddenFileFilter.class);
            else
                chainedFileFilter.addFileFilter(new HiddenFileFilter());
        }
        // Show or hide .DS_Store files (Mac OS X option)
        else if (var.equals(ConfigurationVariables.SHOW_DS_STORE_FILES)) {
            if(event.getBooleanValue())
                removeFileFilter(DSStoreFileFilter.class);
            else
                chainedFileFilter.addFileFilter(new DSStoreFileFilter());
        }
        // Show or hide system folders (Mac OS X option)
        else if (var.equals(ConfigurationVariables.SHOW_SYSTEM_FOLDERS)) {
            if(event.getBooleanValue())
                removeFileFilter(SystemFileFilter.class);
            else
                chainedFileFilter.addFileFilter(new SystemFileFilter());
        }

        // Do not try and refresh folder here as this method can be called several times in a row on the same folder
        // if several variables have changed. This would then try to refresh the folder potentially before the previous
        // refresh has finished and cause deadlocks.

        return true;
    }


    private void removeFileFilter(Class c) {
        Iterator iterator = chainedFileFilter.getFileFilterIterator();
        while(iterator.hasNext()) {
            Object o = iterator.next();
            if(o.getClass().equals(c))
                iterator.remove();
        }
    }


    ////////////////////////////////////
    // ChangeFolderThread inner class //
    ////////////////////////////////////

    /**
     * This thread takes care of changing current folder without locking the main
     * thread. The folder change can be cancelled.
     *
     * <p>A little note out of nowhere: never ever call JComponent.paintImmediately() from a thread
     * other than the Event Dispatcher Thread, as will create nasty repaint glitches that
     * then become very hard to track. Sun's Javadoc doesn't make it clear enough... just don't!
     *
     * @author Maxence Bernard
     */
    public class ChangeFolderThread extends Thread {

        private AbstractFile folder;
        private FileURL folderURL;
        private AbstractFile fileToSelect;

        private boolean userInterrupted;
        private boolean doNotKill;

        private final Object lock = new Object();


        public ChangeFolderThread(AbstractFile folder) {
            this.folder = folder;
            this.folderURL = folder.getURL();

            setPriority(Thread.MAX_PRIORITY);
        }

        public ChangeFolderThread(FileURL folderURL) {
            this.folderURL = folderURL;

            setPriority(Thread.MAX_PRIORITY);
        }

        /**
         * Returns true if the given file should have its canonical path followed. In that case, the AbstractFile
         * instance must be resolved again.
         *
         * <p>HTTP files MIST have their canonical path followed. For all other file protocols, this is an option in
         * the preferences.
         */
        private boolean followCanonicalPath(AbstractFile file) {
            if(ConfigurationManager.getVariableBoolean(ConfigurationVariables.CD_FOLLOWS_SYMLINKS, ConfigurationVariables.DEFAULT_CD_FOLLOWS_SYMLINKS)
                    || file.getURL().getProtocol().equals(FileProtocols.HTTP) && !file.getAbsolutePath(false).equals(file.getCanonicalPath(false)))
                return true;

            return false;
        }

        /**
         * Sets the file to be selected after the folder has been changed, can be null. 
         */
        public void selectThisFileAfter(AbstractFile fileToSelect) {
            this.fileToSelect = fileToSelect;
        }

        /**
         * Kills the thread using the deprecated an not recommanded Thread#stop.
         */
        public void tryKill() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
            synchronized(lock) {
                if(userInterrupted)
                    return;

                userInterrupted = true;
                if(!doNotKill) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killing thread");
                    super.stop();

                    // execute post processing as it would have been done by run()
                    finish(false);
                }
            }
        }


        public void start() {
            // Notify listeners that location is changing
            locationManager.fireLocationChanging(folder==null?folderURL:folder.getURL());

            super.start();
        }


        public void run() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting folder change...");
            boolean folderChangedSuccessfully = false;

            // Show some progress in the progress bar to give hope
            locationField.setProgressValue(10);

//            // Disable automatic refresh
//            fileTable.setAutoRefreshActive(false);

            // If folder URL doesn't contain any credentials but CredentialsManager found some matching the URL,
            // popup the authentication dialog to avoid having to wait for an AuthException to be thrown
            boolean userCancelled = false;
            if(!folderURL.containsCredentials() && CredentialsManager.getMatchingCredentials(folderURL).length>0) {
                MappedCredentials newCredentials = getCredentialsFromUser(folderURL, null);

                // User cancelled the authentication dialog, stop
                if(newCredentials==null)
                    userCancelled = true;
                // Use the provided credentials and invalidate the folder AbstractFile instance (if any) so that
                // it gets recreated with the new credentials
                else {
                    CredentialsManager.authenticate(folderURL, newCredentials);
                    folder = null;
                }
            }

            if(!userCancelled) {
                boolean canonicalPathFollowed = false;

                do {
                    // Set cursor to hourglass/wait
                    mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    // Render all actions inactive while changing folder
                    mainFrame.setNoEventsMode(true);

                    try {
                        // 2 cases here :
                        // - Thread was created using an AbstractFile instance
                        // - Thread was created using a FileURL, corresponding AbstractFile needs to be resolved

                        // Thread was created using a FileURL
                        if(folder==null) {

                            AbstractFile file = FileFactory.getFile(folderURL, true);

                            synchronized(lock) {
                                if(userInterrupted) {
                                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
                                    break;
                                }
                            }

                            // File resolved -> 25% complete
                            locationField.setProgressValue(25);

                            // Popup an error dialog and abort folder change if the file could not be resolved
                            // or doesn't exist
                            if(file==null || !file.exists()) {
                                // Restore default cursor
                                mainFrame.setCursor(Cursor.getDefaultCursor());

                                showFolderDoesNotExistDialog();
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
                                if(!folderHistory.historyContains(folderURL) && !file.equals(fileTable.getSelectedFile())) {
                                    // Restore default cursor
                                    mainFrame.setCursor(Cursor.getDefaultCursor());

                                    // Download or browse file ?
                                    QuestionDialog dialog = new QuestionDialog(mainFrame,
                                                                               null,
                                                                               Translator.get("table.download_or_browse"),
                                                                               mainFrame,
                                                                               new String[] {BROWSE_TEXT, DOWNLOAD_TEXT, CANCEL_TEXT},
                                                                               new int[] {BROWSE_ACTION, DOWNLOAD_ACTION, CANCEL_ACTION},
                                                                               0);

                                    int ret = dialog.getActionValue();
                                    if(ret==-1 || ret==CANCEL_ACTION)
                                        break;

                                    // Download file
                                    if(ret==DOWNLOAD_ACTION) {
                                        showDownloadDialog(file);
                                        break;
                                    }
                                    // Continue if BROWSE_ACTION
                                    // Set cursor to hourglass/wait
                                    mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                    //							noWaitDialog = false;
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
                            showFolderDoesNotExistDialog();
                            break;
                        }

                        // Checks if canonical should be followed. If that is the case, the file is invalidated
                        // and resolved again. This happens only once at most, to avoid a potential infinite loop
                        // in the event that the absolute path still didn't match canonical one after the file is
                        // resolved again.
                        if(!canonicalPathFollowed && followCanonicalPath(folder)) {
                            try {
                                // Recreate the FileURL using the file's canonical path
                                FileURL newURL = new FileURL(folder.getCanonicalPath());
                                // Keep the credentials and properties (if any)
                                newURL.setCredentials(folderURL.getCredentials());
                                newURL.copyProperties(folderURL);
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
                        
                        synchronized(lock) {
                            if(userInterrupted) {
                                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
                                break;
                            }
                        }

                        // File tested -> 50% complete
                        locationField.setProgressValue(50);

                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling ls()");

                        AbstractFile children[] = folder.ls(chainedFileFilter);

                        synchronized(lock) {
                            if(userInterrupted) {
                                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
                                break;
                            }
                            // From now on, thread cannot be killed (would comprise table integrity)
                            doNotKill = true;
                        }

                        // files listed -> 75% complete
                        locationField.setProgressValue(75);

                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling setCurrentFolder");

                        // Change the file table's current folder and select the specified file (if any)
                        setCurrentFolder(folder, children, fileToSelect);

                        // folder set -> 95% complete
                        locationField.setProgressValue(95);

                        // If some new credentials were entered by the user, these can now be considered valid
                        // (folder was changed successfully) -> add them to the credentials list.
                        Credentials credentials = folder.getURL().getCredentials();
                        if(credentials!=null) {
                            if(credentials instanceof MappedCredentials)
                                CredentialsManager.addCredentials((MappedCredentials)credentials);
                            else
                                CredentialsManager.addCredentials(new MappedCredentials(credentials, folderURL, false));
                        }

                        // All good !
                        folderChangedSuccessfully = true;

                        break;
                    }
                    catch(IOException e) {
                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

                        // Restore default cursor
                        mainFrame.setCursor(Cursor.getDefaultCursor());

                        if(e instanceof AuthException) {
                            AuthException authException = (AuthException)e;
                            // Retry (loop) if user provided new credentials, if not stop
                            MappedCredentials newCredentials = getCredentialsFromUser(authException.getFileURL(), authException.getMessage());
                            if(newCredentials!=null) {
                                // Invalidate AbstractFile instance
                                folder = null;
                                // Use the provided credentials
                                CredentialsManager.authenticate(folderURL, newCredentials);
                                continue;
                            }
                        }
                        else {
                            showAccessErrorDialog(e);
                        }

                        // Stop looping!
                        break;
                    }
                }
                while(true);
            }
            
            synchronized(lock) {
                // Clean things up
                finish(folderChangedSuccessfully);
            }
        }


        public void finish(boolean folderChangedSuccessfully) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cleaning up, success="+folderChangedSuccessfully);
            // Reset location field's progress bar
            locationField.setProgressValue(0);

            // Restore normal mouse cursor
            mainFrame.setCursor(Cursor.getDefaultCursor());

//            // Re-enable automatic refresh
//            fileTable.setAutoRefreshActive(true);

            changeFolderThread = null;

            // Make all actions active again
            mainFrame.setNoEventsMode(false);

            if(!folderChangedSuccessfully) {
                FileURL failedURL = folder==null?folderURL:folder.getURL();
                // Notifies listeners that location change has been cancelled by the user or has failed
                if(userInterrupted)
                    locationManager.fireLocationCancelled(failedURL);
                else
                    locationManager.fireLocationFailed(failedURL);
            }
        }


        // For debugging purposes
        public String toString() {
            return "folderURL="+folderURL+" folder="+folder;
        }
    }

    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        switch(event.getColorId()) {
        case Theme.FILE_TABLE_BORDER_COLOR:
            scrollPane.setBorder(BorderFactory.createLineBorder(event.getColor(), 1));
            break;
        case Theme.FILE_TABLE_BACKGROUND_COLOR:
            backgroundColor = event.getColor();
            if(fileTable.hasFocus())
                scrollPane.getViewport().setBackground(backgroundColor);
            break;
        case Theme.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR:
            unfocusedBackgroundColor = event.getColor();
            if(!fileTable.hasFocus())
                scrollPane.getViewport().setBackground(unfocusedBackgroundColor);
            break;
        }
    }

    /**
     * Not used.
     */
    public void fontChanged(FontChangedEvent event) {}
}

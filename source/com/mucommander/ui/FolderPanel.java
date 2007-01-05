
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.MappedCredentials;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.file.*;
import com.mucommander.file.filter.DSStoreFileFilter;
import com.mucommander.file.filter.HiddenFileFilter;
import com.mucommander.file.filter.SystemFoldersFilter;
import com.mucommander.text.Translator;
import com.mucommander.ui.auth.AuthDialog;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.ui.dnd.FileDragSourceListener;
import com.mucommander.ui.dnd.FileDropTargetListener;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.TablePopupMenu;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class FolderPanel extends JPanel implements FocusListener, ConfigurationListener, ThemeListener {

    private MainFrame  mainFrame;

    private AbstractFile currentFolder;
    private ChangeFolderThread changeFolderThread;

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

    /** Filters out hidden files, null when 'show hidden files' option is enabled */
    private HiddenFileFilter hiddenFileFilter = ConfigurationManager.getVariableBoolean(ConfigurationVariables.SHOW_HIDDEN_FILES,
                                                                                        ConfigurationVariables.DEFAULT_SHOW_HIDDEN_FILES) ?
        null:new HiddenFileFilter();

    /** Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled */
    private DSStoreFileFilter dsStoreFilenameFilter = ConfigurationManager.getVariableBoolean(ConfigurationVariables.SHOW_DS_STORE_FILES,
                                                                                              ConfigurationVariables.DEFAULT_SHOW_DS_STORE_FILES) ?
        null:new DSStoreFileFilter();

    /** Filters out Mac OS X system folders, null when 'show system folders' option is enabled */
    private SystemFoldersFilter systemFoldersFilter = ConfigurationManager.getVariableBoolean(ConfigurationVariables.SHOW_SYSTEM_FOLDERS,
                                                                                              ConfigurationVariables.DEFAULT_SHOW_SYSTEM_FOLDERS) ?
        null:new SystemFoldersFilter();

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
		
        fileTable = new FileTable(mainFrame, this);

        try {
            // Set initial folder to current directory
            setCurrentFolder(initialFolder, applyFilters(initialFolder.ls()), null);
        }
        catch(Exception e) {
            AbstractFile rootFolders[] = RootFolders.getRootFolders();
            // If that failed, try to read any other drive
            for(int i=0; i<rootFolders.length; i++) {
                try  {
                    setCurrentFolder(rootFolders[i], applyFilters(rootFolders[i].ls()), null);
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

        scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Sets the table border.
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getCurrentColor(Theme.FILE_TABLE_BORDER), 1));

        // Set scroll pane's background color to match the one of this panel and FileTable
        scrollPane.getViewport().setBackground(ThemeManager.getCurrentColor(Theme.FILE_BACKGROUND));

        // Catch mouse events to popup a contextual 'folder' menu
        scrollPane.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int modifiers = e.getModifiers();

                    // Right-click brings a contextual popup menu
                    if (PlatformManager.isRightMouseButton(e)) {
                        AbstractFile currentFolder = getCurrentFolder();
                        new TablePopupMenu(FolderPanel.this.mainFrame, currentFolder, null, false, fileTable.getFileTableModel().getMarkedFiles()).show(scrollPane, e.getX(), e.getY());
                    }
                }
            });

        add(scrollPane, BorderLayout.CENTER);

        // Listens to some configuration variables
        ConfigurationManager.addConfigurationListener(this);
        ThemeManager.addThemeListener(this);

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
     * Applies the file filters that are enabled to the specified file array and returns the files
     * that were retained by the files.
     *
     * @param files the file array to apply the filters to
     * @return the files retained by the filters
     */
    private AbstractFile[] applyFilters(AbstractFile[] files) {
        // Filter out hidden files (if enabled)
        if(hiddenFileFilter!=null)
            files = hiddenFileFilter.filter(files);
        // Filter out Mac OS X .DS_Store files (if enabled)
        // No need to apply this filter if hidden files filter is enabled, as .DS_Store files are hidden
        else if(dsStoreFilenameFilter!=null)
            files = dsStoreFilenameFilter.filter(files);

        // Filter out Mac OS X system files (if enabled)
        if(systemFoldersFilter!=null)
            files = systemFoldersFilter.filter(files);

        return files;
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

        FileURL fileURL = URLFactory.getFileURL(folderPath);

        if(fileURL==null) {
            // FileURL could not be resolved, notify the user that the folder doesn't exist
            showFolderDoesNotExistDialog();
        }
        else {
            this.changeFolderThread = new ChangeFolderThread(fileURL);
            changeFolderThread.start();
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
        setCurrentFolder(currentFolder, applyFilters(currentFolder.ls()), null);
    }


    /**
     * Changes current folder using the given folder and children files.
     *
     * @param folder folder to be made current folder
     * @param children current folder's files (value of folder.ls())
     * @param fileToSelect file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
     */
    private void setCurrentFolder(AbstractFile folder, AbstractFile children[], AbstractFile fileToSelect) {
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
     * Changes current folder to be the current folder's parent.
     * Does nothing if current folder doesn't have a parent. 
     */
    public synchronized void goToParent() {
        AbstractFile parent;
        if((parent=getCurrentFolder().getParent())!=null)
            tryChangeCurrentFolder(parent);
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
    }

    public void focusLost(FocusEvent e) {
        // No need to do anything here, the other FolderPanel instance will call setActiveTable
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
            hiddenFileFilter = event.getBooleanValue()?null:new HiddenFileFilter();
            // Refresh current folder in a separate thread
            tryRefreshCurrentFolder();
        }
        // Show or hide .DS_Store files (Mac OS X option)
        else if (var.equals(ConfigurationVariables.SHOW_DS_STORE_FILES)) {
            dsStoreFilenameFilter = event.getBooleanValue()?null:new DSStoreFileFilter();
            // Refresh current folder in a separate thread
            tryRefreshCurrentFolder();
        }
        // Show or hide system folders (Mac OS X option)
        else if (var.equals(ConfigurationVariables.SHOW_SYSTEM_FOLDERS)) {
            systemFoldersFilter = event.getBooleanValue()?null:new SystemFoldersFilter(); 
            // Refresh current folder in a separate thread
            tryRefreshCurrentFolder();
        }

        return true;
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


        public void run() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting folder change...");
            boolean folderChangedSuccessfully = false;

            // Notify listeners that location is changing
            locationManager.fireLocationChanging(folder==null?folderURL:folder.getURL());

            // Show some progress in the progress bar to give hope
            locationField.setProgressValue(10);

            // Disable automatic refresh
            fileTable.setAutoRefreshActive(false);

            // If folder URL doesn't contain any credentials but CredentialsManager found some matching the URL,
            // popup the authentication dialog to avoid having to wait for an AuthException to be thrown
            boolean userCancelled = false;
            if(!folderURL.containsCredentials() && CredentialsManager.getMatchingCredentials(folderURL).length>0) {
                MappedCredentials newCredentials = getCredentialsFromUser(folderURL, null);

                // User cancelled the authentication dialog, stop
                if(newCredentials==null)
                    userCancelled = true;
                // Use the provided credentials and invalidate folder instance (if any)
                else {
                    folderURL.setCredentials(newCredentials);
                    folder = null;
                }
            }

            if(!userCancelled) {
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

                            if(file==null || !file.exists()) {
                                // Restore default cursor
                                mainFrame.setCursor(Cursor.getDefaultCursor());

                                showFolderDoesNotExistDialog();
                                break;
                            }

                            // File is a regualar directory, all good
                            if(file.isDirectory()) {
                                // Just continue
                            }
                            // File is a browsable file (Zip archive for instance) but not a directory : Browse or Download ? => ask the user
                            else if(file.isBrowsable()) {
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
                            // File is a regular file: show download dialog
                            else {
                                showDownloadDialog(file);
                                break;
                            }

                            this.folder = file;
                        }
                        // Thread was created using an AbstractFile instance, check for existence
                        else if(!folder.exists()) {
                            showFolderDoesNotExistDialog();
                            break;
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
                        AbstractFile children[] = applyFilters(folder.ls());

                        // Filter out Mac OS X .DS_Store files if the option is enabled
                        if(dsStoreFilenameFilter!=null)
                            children = dsStoreFilenameFilter.filter(children);

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
if(Debug.ON) Debug.trace("Final credentials="+credentials);

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
                                folderURL.setCredentials(newCredentials);
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

            // Re-enable automatic refresh
            fileTable.setAutoRefreshActive(true);

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
     * @param colorId identifier of the color that has changed.
     * @param color   new value for the color.
     */
    public void colorChanged(int colorId, Color color) {
        if(colorId == Theme.FILE_TABLE_BORDER)
            scrollPane.setBorder(BorderFactory.createLineBorder(color, 1));
        else if(colorId == Theme.FILE_BACKGROUND)
            scrollPane.getViewport().setBackground(color);
    }

    /**
     * Not used.
     */
    public void fontChanged(int fontId, Font font) {}
}

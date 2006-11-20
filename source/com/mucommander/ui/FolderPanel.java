
package com.mucommander.ui;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.file.*;
import com.mucommander.file.filter.DSStoreFileFilter;
import com.mucommander.file.filter.HiddenFileFilter;
import com.mucommander.file.filter.SystemFoldersFilter;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.ui.dnd.FileDragSourceListener;
import com.mucommander.ui.dnd.FileDropTargetListener;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.TablePopupMenu;
import com.mucommander.ui.auth.AuthDialog;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.MappedCredentials;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.Debug;

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
public class FolderPanel extends JPanel implements FocusListener, ConfigurationListener {
	
    private MainFrame mainFrame;

    private AbstractFile currentFolder;
    private ChangeFolderThread changeFolderThread;

    private LocationManager locationManager = new LocationManager(this);

    /*  We're NOT using JComboBox anymore because of its strange behavior:
        it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
        so there is no way to tell if it's the final selection (ENTER) or not.
    */
    private DriveButton driveButton;
    private ProgressTextField locationField;
    private FileTable fileTable;
    private JScrollPane scrollPane;
	
    private FolderHistory folderHistory = new FolderHistory(this);
    
    private static Color backgroundColor;
    
    private Object lastFocusedComponent;

    private long lastFolderChangeTime;

    private FileDragSourceListener fileDragSourceListener;

    /** Filters out hidden files, null when 'show hidden files' option is enabled */
    private HiddenFileFilter hiddenFileFilter = ConfigurationManager.getVariableBoolean("prefs.file_table.show_hidden_files", true)?null:new HiddenFileFilter();

    /** Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled */
    private DSStoreFileFilter dsStoreFilenameFilter = ConfigurationManager.getVariableBoolean("prefs.file_table.show_ds_store_files", true)?null:new DSStoreFileFilter();

    /** Filters out Mac OS X system folders, null when 'show system folders' option is enabled */
    private SystemFoldersFilter systemFoldersFilter = ConfigurationManager.getVariableBoolean("prefs.file_table.show_system_folders", true)?null:new SystemFoldersFilter();

    private final static int CANCEL_ACTION = 0;
    private final static int BROWSE_ACTION = 1;
    private final static int DOWNLOAD_ACTION = 2;
	
    private final static String CANCEL_TEXT = Translator.get("cancel");
    private final static String BROWSE_TEXT = Translator.get("browse");
    private final static String DOWNLOAD_TEXT = Translator.get("download");


    static {
        // Set background color
        backgroundColor = ConfigurationManager.getVariableColor("prefs.colors.background", "000084");
    }


    public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
        super(new BorderLayout());

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(" initialFolder="+initialFolder);
        this.mainFrame = mainFrame;

        // No decoration for this panel
        setBorder(null);

//        setBorder(BorderFactory.createEtchedBorder());

        JPanel locationPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        // Create and add drive button
        this.driveButton = new DriveButton(this);
        c.weightx = 0;
        c.gridx = 0;        
        locationPanel.add(driveButton, c);

        // Create location combo box and retrieve location field instance
        LocationComboBox locationComboBox = new LocationComboBox(this);
        this.locationField = (ProgressTextField)locationComboBox.getTextField();
        locationField.addFocusListener(this);
        // Give location field all the remaining space
        c.weightx = 1;
        c.gridx = 1;
        // Add some space between drive button and location combo box (none by default)
        c.insets = new Insets(0, 4, 0, 0);
        locationPanel.add(locationComboBox, c);

        add(locationPanel, BorderLayout.NORTH);
		
        fileTable = new FileTable(mainFrame, this);
        this.lastFocusedComponent = fileTable;
        fileTable.addFocusListener(this);
		
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

        // Set a 1-line gray border
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(64, 64, 64), 1));

        //		// Enable double buffering on scroll pane
        //		scrollPane.setDoubleBuffered(true);
        // Set scroll pane's background color to match the one of this panel and FileTable
        scrollPane.getViewport().setBackground(backgroundColor);		

        // Catch mouse events to popup a contextual 'folder' menu
        scrollPane.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int modifiers = e.getModifiers();

                    // Right-click brings a contextual popup menu
                    if ((modifiers & MouseEvent.BUTTON3_MASK)!=0 || (com.mucommander.PlatformManager.OS_FAMILY==com.mucommander.PlatformManager.MAC_OS_X && (modifiers & MouseEvent.BUTTON1_MASK)!=0 && e.isControlDown())) {
                        AbstractFile currentFolder = getCurrentFolder();
                        new TablePopupMenu(FolderPanel.this.mainFrame, currentFolder, null, false, fileTable.getFileTableModel().getMarkedFiles()).show(scrollPane, e.getX(), e.getY());
                    }
                }
            });


        add(scrollPane, BorderLayout.CENTER);

        // Listens to some configuration variables
        ConfigurationManager.addConfigurationListener(this);

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

    public FileTable getFileTable() {
        return this.fileTable;
    }

    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    public FolderHistory getFolderHistory() {
        return this.folderHistory;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * Causes the DriveButton to popup and show root folder, bookmarks, server shortcuts...
     */
    public void popDriveButton() {
        driveButton.popup();
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
     * Displays a popup message notifying the user that the request folder couldn't be opened.
     */
    private void showAccessErrorDialog(IOException e) {
        String exceptionMsg = e==null?null:e.getMessage();
        String errorMsg = Translator.get("table.folder_access_error")+(exceptionMsg==null?"":": "+exceptionMsg);

        JOptionPane.showMessageDialog(mainFrame, errorMsg, Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Pops up an authentication dialog where the user can enter credentials to grant him access to the folder or file
     * which caused the specified AuthException.
     *
     * @param e the AuthException thrown when trying to access the restricted file or folder.
     * @return the credentials the user entered/chose and validated, null if he cancelled the dialog.
     */
    private MappedCredentials showAuthDialog(AuthException e) {
        AuthDialog authDialog = new AuthDialog(mainFrame, e);
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
    public synchronized void trySetCurrentFolder(AbstractFile folder) {
        trySetCurrentFolder(folder, null);
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
    public synchronized void trySetCurrentFolder(AbstractFile folder, AbstractFile selectThisFileAfter) {
        // Make sure there is not an existing thread running,
        // this should not normally happen but if it does, report the error
        if(changeFolderThread!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
            return;
        }
		
        //		if (folder==null || !folder.exists()) {
        //			JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
        //			return;
        //		}

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
    public synchronized void trySetCurrentFolder(String folderPath) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folderPath="+folderPath);

        // Make sure there is not an existing thread running,
        // this should not normally happen but if it does, report the error
        if(changeFolderThread!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
            return;
        }

        this.changeFolderThread = new ChangeFolderThread(folderPath);
        changeFolderThread.start();
    }

    /**
     * Tries to change current folder to the new specified URL.
     * The user is notified by a dialog if the folder could not be changed.
     * 
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param folderURL folder's URL to be made current folder. If this URL does not resolve into an existing file, an error message will be displayed
     */
    public synchronized void trySetCurrentFolder(FileURL folderURL) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folderURL="+folderURL);

        // Make sure there is not an existing thread running,
        // this should not normally happen but if it does, report the error
        if(changeFolderThread!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
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
        trySetCurrentFolder(currentFolder, null);
    }

    /**
     * Refreshes current folder's contents and notifies the user if current folder could not be refreshed.
     *
     * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
     *
     * @param selectThisFileAfter file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file 
     */
    public synchronized void tryRefreshCurrentFolder(AbstractFile selectThisFileAfter) {
        trySetCurrentFolder(currentFolder, selectThisFileAfter);
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
        locationManager.fireLocationChanged(folder.getAbsolutePath());
    }


    /**
     * Changes current folder to be the current folder's parent.
     * Does nothing if current folder doesn't have a parent. 
     */
    public synchronized void goToParent() {
        AbstractFile parent;
        if((parent=getCurrentFolder().getParent())!=null)
            trySetCurrentFolder(parent);
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
     * Overrides JComponent's requestFocus() method to request focus
     * on the last focused component inside this FolderPanel: on the file table or on the location field
     */
    public void requestFocus() {
        if(!mainFrame.getNoEventsMode()) {
            if(Debug.ON) Debug.trace("requesting focus on last focused component: "+lastFocusedComponent.getClass().getName());

            ((JComponent)lastFocusedComponent).requestFocus();
        }
    }



    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////
	
    public void focusGained(FocusEvent e) {
        Object source = e.getSource();

//        if(Debug.ON) Debug.trace("called, source="+source.getClass().getName()+", isFolderChanging="+isFolderChanging()+" folderChangeTimeDiff="+(System.currentTimeMillis()-lastFolderChangeTime));

        // Ignore focus gained events while folder is being changed (or shortly after it has been changed)
        // in order to remember if focus was on the location field before the folder was changed.
        // Location field gives up focus when it gets disabled when the folder starts changing, hence this method
        // is called to notify that FileTable gained focus.
        if(source==fileTable && (isFolderChanging()|| System.currentTimeMillis()-lastFolderChangeTime<300))
            return;

        if(Debug.ON) Debug.trace("last focused component is now: "+lastFocusedComponent.getClass().getName());

        this.lastFocusedComponent = source;

        // Notify MainFrame that we are in control now! (our table/location field is active)
        mainFrame.setActiveTable(fileTable);
    }
	
    public void focusLost(FocusEvent e) {
    }
	
	 
    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /** 
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();
    
        // Set new background color and repaint panel
        if (var.equals("prefs.colors.background"))  {
            scrollPane.getViewport().setBackground(backgroundColor=event.getColorValue());
            repaint();    		
        }
        // Show or hide hidden files
        else if (var.equals("prefs.file_table.show_hidden_files")) {
            hiddenFileFilter = event.getBooleanValue()?null:new HiddenFileFilter();
            // Refresh current folder in a separate thread
            tryRefreshCurrentFolder();
        }
        // Show or hide .DS_Store files (Mac OS X option)
        else if (var.equals("prefs.file_table.show_ds_store_files")) {
            dsStoreFilenameFilter = event.getBooleanValue()?null:new DSStoreFileFilter();
            // Refresh current folder in a separate thread
            tryRefreshCurrentFolder();
        }
        // Show or hide system folders (Mac OS X option)
        else if (var.equals("prefs.file_table.show_system_folders")) {
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
        private String folderPath;
        private FileURL folderURL;
        private AbstractFile fileToSelect;

        private boolean userInterrupted;
        private boolean doNotKill;

        //		private boolean noWaitDialog;
        //		private QuestionDialog waitDialog;

        private final Object lock = new Object();

        //		private boolean hadFocus;


        public ChangeFolderThread(AbstractFile folder) {
            this.folder = folder;
            setPriority(Thread.MAX_PRIORITY);
        }

        public ChangeFolderThread(String folderPath) {
            this.folderPath = folderPath;
            setPriority(Thread.MAX_PRIORITY);
        }

        public ChangeFolderThread(FileURL folderURL) {
            this.folderURL = folderURL;
            setPriority(Thread.MAX_PRIORITY);
        }

        public void selectThisFileAfter(AbstractFile fileToSelect) {
            this.fileToSelect = fileToSelect;
        }

        public void tryKill() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
            synchronized(lock) {
                if(userInterrupted)
                    return;

                userInterrupted = true;
                if(!doNotKill) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killing thread");
                    super.stop();

                    /*
                      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("waitdialog = "+waitDialog);
                      if(waitDialog!=null) {
                      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling dispose() on waitdialog");
                      waitDialog.dispose();
                      waitDialog = null;
                      }
                    */
                    // post processing as it would normally have been done by run()
                    finish(false);
                }
            }
        }


        public void run() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting folder change...");
            boolean folderChangedSuccessfully = false;

            // Update timestamp
            lastFolderChangeTime = System.currentTimeMillis();
            
            // Notify listeners that location is changing
            locationManager.fireLocationChanging(folder==null?folderPath==null?folderURL.getStringRep(false):folderPath:folder.getAbsolutePath());

            // Show some progress in the progress bar to give hope
            locationField.setProgressValue(10);

            /*
            // Start a new thread which will popup a dialog after a number of seconds
            new Thread() {
            public void run() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting and waiting");

            try { sleep(3000); }
            catch(InterruptedException e) {}

            while(noWaitDialog) {
            try { sleep(200); }
            catch(InterruptedException e) {}
            }

            synchronized(lock) {
            if(changeFolderThread==null || changeFolderThread!=ChangeFolderThread.this || userInterrupted || !ChangeFolderThread.this.isAlive())
            return;

            YBoxPanel panel = new YBoxPanel();
            panel.add(new JLabel(Translator.get("table.connecting_to_folder")));
            panel.addSpace(5);
            JProgressBar fullProgressBar = new JProgressBar();
            fullProgressBar.setValue(50);
            panel.add(fullProgressBar);

            // Download or browse file ?
            waitDialog = new QuestionDialog(mainFrame,
            null,
            panel,
            mainFrame,
            new String[] {CANCEL_TEXT},
            new int[] {CANCEL_ACTION},
            0);
            }

            int ret = waitDialog.getActionValue();
            if(ret==-1 || ret==CANCEL_ACTION)
            tryKill();
            }
            }.start();
            */

            // Disable automatic refresh
            fileTable.setAutoRefreshActive(false);

            //			// Save focus state
            //			this.hadFocus = fileTable.hasFocus();
            MappedCredentials newCredentials = null;
            do {
                //				noWaitDialog = false;

                // Set cursor to hourglass/wait
                mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                // Render all actions inactive while changing folder
                mainFrame.setNoEventsMode(true);

                try {
                    // 2 cases here :
                    // - Thread was created using an AbstractFile instance
                    // - Thread was created using a FileURL or FolderPath, corresponding AbstractFile needs to be resolved

                    // Thread was created using a FileURL or FolderPath
                    if(folder==null) {
                        AbstractFile file;
                        if(folderURL!=null)
                            file = FileFactory.getFile(folderURL, true);
                        else
                            file = FileFactory.getFile(folderPath, true);

                        synchronized(lock) {
                            if(userInterrupted) {
                                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
                                break;
                            }
                        }

                        // File resolved -> 25% complete
                        locationField.setProgressValue(25);

                        if(file==null || !file.exists()) {
                            //							noWaitDialog = true;

                            // Restore default cursor
                            mainFrame.setCursor(Cursor.getDefaultCursor());

                            JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
                            break;
                        }

                        // File is a regualar directory, all good
                        if(file.isDirectory()) {
                            // Just continue
                        }
                        // File is a browsable file (Zip archive for instance) but not a directory : Browse or Download ? => ask the user
                        else if(file.isBrowsable()) {
                            //							noWaitDialog = true;

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
                                //								noWaitDialog = true;

                                showDownloadDialog(file);
                                break;

                                /*
                                  FileSet fileSet = new FileSet(currentFolder);
                                  fileSet.add(file);

                                  // Show confirmation/path modification dialog
                                  new DownloadDialog(mainFrame, fileSet);

                                  break;
                                */
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
                        JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
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
                    setCurrentFolder(folder, children, fileToSelect);

                    // folder set -> 95% complete
                    locationField.setProgressValue(95);

                    // If some new credentials were entered by the user, these can be considered valid
                    // (folder was changed successfully) and added to the credentials list.
                    if(newCredentials!=null)
                        CredentialsManager.addCredentials(newCredentials);

                    // All good !
                    folderChangedSuccessfully = true;

                    break;
                }
                catch(IOException e) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);
                    //					noWaitDialog = true;

                    // Restore default cursor
                    mainFrame.setCursor(Cursor.getDefaultCursor());

                    if(e instanceof AuthException) {
                        AuthException authException = (AuthException)e;
                        // Retry (loop) if user provided new credentials
                        newCredentials = showAuthDialog(authException);
                        if(newCredentials!=null) {
//                            folder = FileFactory.getFile(authException.getFileURL().getStringRep(true));
                            folder = null;
                            folderPath = null;
                            folderURL = newCredentials.getURL();
                            continue;
                        }
                    }
                    else {
                        showAccessErrorDialog(e);
                    }

                    // Break!
                    break;
                }
            }
            while(true);

            //			noWaitDialog = true;

            synchronized(lock) {
                /*
                  if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("waitdialog = "+waitDialog);
                  if(waitDialog!=null) {
                  if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling dispose() on waitdialog");
                  waitDialog.dispose();
                  waitDialog = null;
                  }
                */
                // Clean things up
                finish(folderChangedSuccessfully);
            }
        }


        public void finish(boolean folderChangedSuccessfully) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cleaning up and restoring focus, success="+folderChangedSuccessfully);
            // Reset location field's progress bar
            locationField.setProgressValue(0);

            // Restore normal mouse cursor
            mainFrame.setCursor(Cursor.getDefaultCursor());

            // Re-enable automatic refresh
            fileTable.setAutoRefreshActive(true);

            changeFolderThread = null;

            // Make all actions active again
            mainFrame.setNoEventsMode(false);

            if(folderChangedSuccessfully) {
                // Make sure FileTable will receive focus, in case the folder was entered by the user in the location field
                FolderPanel.this.lastFocusedComponent = fileTable;
            }
            else {
                String failedPath = folder==null?folderPath==null?folderURL.getStringRep(false):folderPath:folder.getAbsolutePath();
                // Notifies listeners that location change has been cancelled by the user or has failed
                if(userInterrupted)
                    locationManager.fireLocationCancelled(failedPath);
                else
                    locationManager.fireLocationFailed(failedPath);

//                // Make sure FileTable will receive focus, in case the folder was entered by the user in the location field
//                FolderPanel.this.lastFocusedComponent = locationFieldHadFocus?locationField:(Component)fileTable;
            }

            // Update timestamp
            lastFolderChangeTime = System.currentTimeMillis();

            // Use FocusRequester to request focus after all other UI events have been processed,
            // calling requestFocus() on table directly could get ignored
            FocusRequester.requestFocus(FolderPanel.this);
        }
    }
}

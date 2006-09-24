
package com.mucommander.ui;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.file.*;
import com.mucommander.file.filter.HiddenFileFilter;
import com.mucommander.file.filter.DSStoreFileFilter;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.table.TablePopupMenu;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
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

    /** Filters out hidden files, null when 'show hidden files' option is enabled */
    private static HiddenFileFilter hiddenFileFilter = ConfigurationManager.getVariableBoolean("prefs.file_table.show_hidden_files", true)?null:new HiddenFileFilter();

    /** Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled */
    private static DSStoreFileFilter dsStoreFilenameFilter = ConfigurationManager.getVariableBoolean("prefs.file_table.show_ds_store_files", true)?null:new DSStoreFileFilter();

    private final static int CANCEL_ACTION = 0;
    private final static int BROWSE_ACTION = 1;
    private final static int DOWNLOAD_ACTION = 2;
	
    private final static String CANCEL_TEXT = Translator.get("cancel");
    private final static String BROWSE_TEXT = Translator.get("browse");
    private final static String DOWNLOAD_TEXT = Translator.get("download");


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
		
        private boolean isKilled;
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
                if(isKilled)
                    return;

                isKilled = true;
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
	
	
        private void enableNoEventsMode() {
            // Prevents mouse/keybaord events from reaching the application and display hourglass/wait cursor
            mainFrame.setNoEventsMode(true);

            // Register a cutom action for the ESCAPE key which stops current folder change
            JRootPane rootPane = mainFrame.getRootPane();
            InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = rootPane.getActionMap();
            AbstractAction killAction = new AbstractAction() {
                    public void actionPerformed(ActionEvent e){
                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("escape pressed");
                        tryKill();
                    }
                };
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "customEscapeAction");
            actionMap.put("customEscapeAction", killAction);
        }

        private void disableNoEventsMode() {
            // Restore mouse/keybaord events and default cursor
            mainFrame.setNoEventsMode(false);
            // Remove 'escape' action
            JRootPane rootPane = mainFrame.getRootPane();
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
            rootPane.getActionMap().remove("customEscapeAction");
        }
		
		
        public void run() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("run starts");
            boolean folderChangedSuccessfully = false;

            // Notify listeners that location is changing
            locationManager.fireLocationChanging();

            // Set new folder's path in location field
            locationField.setText(folder==null?folderPath==null?folderURL.getStringRep(false):folderPath:folder.getAbsolutePath());

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
            if(changeFolderThread==null || changeFolderThread!=ChangeFolderThread.this || isKilled || !ChangeFolderThread.this.isAlive())
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
            do {
                //				noWaitDialog = false;

                // Set cursor to hourglass/wait
                mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                // Prevents mouse and keybaord events from reaching the main frame and menus
                enableNoEventsMode();

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
                            if(isKilled) {
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
                        if(isKilled) {
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
                        if(isKilled) {
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
                        // Retry (loop) if user authentified
                        if(showAuthDialog(authException)) {
                            folder = FileFactory.getFile(authException.getFileURL().getStringRep(false));
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
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cleaning up and restoring focus...");
            // Reset location field's progress bar
            locationField.setProgressValue(0);

            // Restore normal mouse cursor
            mainFrame.setCursor(Cursor.getDefaultCursor());

            // Re-enable automatic refresh
            fileTable.setAutoRefreshActive(true);

            changeFolderThread = null;

            // Restore mouse/keybaord events and default cursor
            disableNoEventsMode();

            if(!folderChangedSuccessfully) {
                // Restore current folder's path
                locationField.setText(currentFolder.getAbsolutePath());

                // Notifies listeners that location change has been cancelled
                locationManager.fireLocationCancelled();
            }

            // /!\ Focus should be restored after disableNoEventsMode has been called
            // Use FocusRequester to request focus after all other UI events have been processed,
            // calling requestFocus() on table directly could get ignored 
            FocusRequester.requestFocus(mainFrame.getLastActiveTable());
        }
    }


	
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
        this.locationField = locationComboBox.getLocationField();
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

        //		// Change location field's text to reflect new current folder's path
        //		locationField.setText(currentFolder.getAbsolutePath());
				
        scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
		
                public Border getBorder() {
                    return null;
                }
            };
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
                        new TablePopupMenu(FolderPanel.this.mainFrame, currentFolder, null, false, ((FileTableModel)fileTable.getModel()).getMarkedFiles()).show(scrollPane, e.getX(), e.getY());
                    }
                }
            });

        add(scrollPane, BorderLayout.CENTER);

        // Listens to some configuration variables
        ConfigurationManager.addConfigurationListener(this);
    }


    private AbstractFile[] applyFilters(AbstractFile[] files) {
        // Filter out hidden files (if enabled)
        if(hiddenFileFilter!=null)
            files = hiddenFileFilter.filter(files);

        // Filter out Mac OS X .DS_Store files (if enabled)
        if(dsStoreFilenameFilter!=null)
            files = dsStoreFilenameFilter.filter(files);

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
        locationField.setSelectionStart(0);
        locationField.setSelectionEnd(locationField.getText().length());
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
     * Pops up an authentication dialog where the user can enter a new login and password.
     *
     * @return true if the user entered a (potentially) new login and password and pressed OK.
     */
    private boolean showAuthDialog(AuthException e) {
        AuthDialog authDialog = new AuthDialog(mainFrame, e);
        authDialog.showDialog();
        return authDialog.okPressed();
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
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folder="+folder, -1);

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

        // Change location field's text to reflect new current folder's path
        locationField.setText(folder.getAbsolutePath());

        // Add folder to history
        folderHistory.addToHistory(folder);

        // Notify listeners that location has changed
        locationManager.fireLocationChanged();
    }


    public ChangeFolderThread getChangeFolderThread() {
        return changeFolderThread;
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


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overrides JComponent's requestFocus() method to request focus
     * on the last focused component inside this FolderPanel: on the file able or on the location field
     */
    public void requestFocus() {
        if(!mainFrame.getNoEventsMode())
            ((JComponent)lastFocusedComponent).requestFocus();
    }



    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////
	
    public void focusGained(FocusEvent e) {
        this.lastFocusedComponent = e.getSource();
		
        // Notify MainFrame that we are in control now! (our table/location field is active)
        mainFrame.setLastActiveTable(fileTable);
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
        // Show or hide .DS_Store files
        else if (var.equals("prefs.file_table.show_ds_store_files")) {
            dsStoreFilenameFilter = event.getBooleanValue()?null:new DSStoreFileFilter();
            // Refresh current folder in a separate thread
            tryRefreshCurrentFolder();
        }


        return true;
    }
}

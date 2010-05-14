/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.main;

import com.mucommander.AppLogger;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.impl.CachedFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.conf.MuConfiguration;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.impl.FocusNextAction;
import com.mucommander.ui.action.impl.FocusPreviousAction;
import com.mucommander.ui.border.MutableLineBorder;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.auth.AuthDialog;
import com.mucommander.ui.dialog.file.DownloadDialog;
import com.mucommander.ui.dnd.FileDragSourceListener;
import com.mucommander.ui.dnd.FileDropTargetListener;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.main.menu.TablePopupMenu;
import com.mucommander.ui.main.quicklist.BookmarksQL;
import com.mucommander.ui.main.quicklist.ParentFoldersQL;
import com.mucommander.ui.main.quicklist.RecentExecutedFilesQL;
import com.mucommander.ui.main.quicklist.RecentLocationsQL;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableConfiguration;
import com.mucommander.ui.main.table.FolderChangeMonitor;
import com.mucommander.ui.main.tree.FoldersTreePanel;
import com.mucommander.ui.quicklist.QuickList;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;

/**
 * Folder pane that contains the table that displays the contents of the current directory and allows navigation, the
 * drive button, and the location field.
 *
 * @author Maxence Bernard
 */
public class FolderPanel extends JPanel implements FocusListener, ThemeListener {

    private MainFrame  mainFrame;

    private AbstractFile currentFolder;
    private ChangeFolderThread changeFolderThread;

    private long lastFolderChangeTime;
    private FolderChangeMonitor folderChangeMonitor;

    private LocationManager locationManager = new LocationManager(this);

    /*  We're NOT using JComboBox anymore because of its strange behavior:
        it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
        so there is no way to tell if it's the final selection (ENTER) or not.
    */
    private DrivePopupButton driveButton;
    private LocationTextField locationTextField;
    private FileTable fileTable;
    private JScrollPane scrollPane;
    private FoldersTreePanel foldersTreePanel;
    private JSplitPane treeSplitPane;
	
    private LocationHistory folderHistory = new LocationHistory(this);
    
    private FileDragSourceListener fileDragSourceListener;

    private Color borderColor;
    private Color unfocusedBorderColor;
    private Color backgroundColor;
    private Color unfocusedBackgroundColor;
    private Color unmatchedBackgroundColor;

    /** Filters out unwanted files when listing folder contents */
    private ConfigurableFolderFilter configurableFolderFilter = new ConfigurableFolderFilter();

    /** The lock object used to prevent simultaneous folder change operations */
    private final Object FOLDER_CHANGE_LOCK = new Object();

    private final static int CANCEL_ACTION = 0;
    private final static int BROWSE_ACTION = 1;
    private final static int DOWNLOAD_ACTION = 2;
	
    private final static String CANCEL_TEXT = Translator.get("cancel");
    private final static String BROWSE_TEXT = Translator.get("browse");
    private final static String DOWNLOAD_TEXT = Translator.get("download");
    
    /** Is directory tree visible */
    private boolean treeVisible = false;

    /** Saved width of a directory tree (when it's not visible) */ 
    private int oldTreeWidth = 150;

    /** Array of all the existing pop ups for this panel's FileTable **/
    private QuickList[] fileTablePopups;
    protected static RecentLocationsQL recentLocationsQL = new RecentLocationsQL();
    protected static RecentExecutedFilesQL recentExecutedFilesQL = new RecentExecutedFilesQL();
    protected static BookmarksQL bookmarksQL = new BookmarksQL();

    public static final int PARENT_FOLDERS_QUICK_LIST_INDEX = 0;
    public static final int RECENT_ACCESSED_LOCATIONS_QUICK_LIST_INDEX = 1;
    public static final int RECENT_EXECUTED_FILES_QUICK_LIST_INDEX = 2;
    public static final int BOOKMARKS_QUICK_LIST_INDEX = 3;

    /* TODO branch private boolean branchView; */


    FolderPanel(MainFrame mainFrame, AbstractFile initialFolder, FileTableConfiguration conf) {
        super(new BorderLayout());

        AppLogger.finest(" initialFolder="+initialFolder);
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

        // Create location text field
        this.locationTextField = new LocationTextField(this);

        // Give location field all the remaining space until the PoupupsButton
        c.weightx = 1;
        c.gridx = 1;
        // Add some space between drive button and location combo box (none by default)
        c.insets = new Insets(0, 4, 0, 0);
        locationPanel.add(locationTextField, c);

        add(locationPanel, BorderLayout.NORTH);

        // Create the FileTable
        fileTable = new FileTable(mainFrame, this, conf);
        
        // Init quick lists
    	locationManager.addLocationListener(recentLocationsQL);
    	fileTablePopups = new QuickList[]{
    			new ParentFoldersQL(this),
    			recentLocationsQL,
                recentExecutedFilesQL,
                bookmarksQL};

        try {
            // Set initial folder to current directory
            setCurrentFolder(initialFolder, initialFolder.ls(configurableFolderFilter), null);
        }
        catch(Exception e) {
            AbstractFile rootFolders[] = LocalFile.getVolumes();
            // If that failed, try to read any other drive
            for(int i=0; i<rootFolders.length; i++) {
                try  {
                    setCurrentFolder(rootFolders[i], rootFolders[i].ls(configurableFolderFilter), null);
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
        scrollPane.setBorder(new MutableLineBorder(unfocusedBorderColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_BORDER_COLOR), 1));
        borderColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BORDER_COLOR);

        // Set scroll pane's background color to match the one of this panel and FileTable
        scrollPane.getViewport().setBackground(unfocusedBackgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR));
        fileTable.setBackground(unfocusedBackgroundColor);
        backgroundColor          = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR);
        unmatchedBackgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);

        // Remove default action mappings that conflict with corresponding mu actions
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.clear();
        inputMap.setParent(null);

        // Catch mouse events on the ScrollPane
        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Left-click requests focus on the FileTable
                if (DesktopManager.isLeftMouseButton(e)) {
                    fileTable.requestFocus();
                }
                // Right-click brings a contextual popup menu
                else if (DesktopManager.isRightMouseButton(e)) {
                    if(!fileTable.hasFocus())
                        fileTable.requestFocus();
                    AbstractFile currentFolder = getCurrentFolder();
                    new TablePopupMenu(FolderPanel.this.mainFrame, currentFolder, null, false, fileTable.getFileTableModel().getMarkedFiles()).show(scrollPane, e.getX(), e.getY());
                }
            }
        });

        // create folders tree on a JSplitPane 
        foldersTreePanel = new FoldersTreePanel(this);
        foldersTreePanel.setVisible(false);
        treeSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, foldersTreePanel, scrollPane);
        treeSplitPane.setDividerSize(0);
        treeSplitPane.setDividerLocation(0);
        // Remove default border
        treeSplitPane.setBorder(null);
        add(treeSplitPane, BorderLayout.CENTER);
                
        // Listens to theme events
        ThemeManager.addCurrentThemeListener(this);

        // Disable Ctrl+Tab and Shift+Ctrl+Tab focus traversal keys
        disableCtrlFocusTraversalKeys(locationTextField);
        disableCtrlFocusTraversalKeys(foldersTreePanel.getTree());
        disableCtrlFocusTraversalKeys(fileTable);
        registerCycleThruFolderPanelAction(locationTextField);
        registerCycleThruFolderPanelAction(foldersTreePanel.getTree());
        // No need to register cycle actions for FileTable, they already are 

        // Listen to focus event in order to notify MainFrame of changes of the current active panel/table
        fileTable.addFocusListener(this);
        locationTextField.addFocusListener(this);

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
        locationTextField.setDropTarget(new DropTarget(locationTextField, dropTargetListener));
        driveButton.setDropTarget(new DropTarget(driveButton, dropTargetListener));
    }


    /**
     * Removes the Control+Tab and Shift+Control+Tab focus traversal keys from the given component so that those
     * shortcuts can be used for other purposes.
     *
     * @param component the component for which to remove the Control+Tab and Shift+Control+Tab focus traversal keys
     */
    private void disableCtrlFocusTraversalKeys(Component component) {
        // Remove Ctrl+Tab from forward focus traversal keys
        HashSet<AWTKeyStroke> keyStrokeSet = new HashSet<AWTKeyStroke>();
        keyStrokeSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keyStrokeSet);

        // Remove Shift+Ctrl+Tab from backward focus traversal keys
        keyStrokeSet = new HashSet<AWTKeyStroke>();
        keyStrokeSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keyStrokeSet);
    }

    /**
     * Registers the {@link FocusNextAction} and {@link FocusPreviousAction} actions onto the given component's
     * input map.
     *  
     * @param component the component for which to register the cycle actions
     */
    private void registerCycleThruFolderPanelAction(JComponent component) {
        ActionKeymap.registerActionAccelerators(
                ActionManager.getActionInstance(FocusNextAction.Descriptor.ACTION_ID, mainFrame),
                component,
                JComponent.WHEN_FOCUSED);

        ActionKeymap.registerActionAccelerators(
                ActionManager.getActionInstance(FocusPreviousAction.Descriptor.ACTION_ID, mainFrame),
                component,
                JComponent.WHEN_FOCUSED);
    }


    public FileDragSourceListener getFileDragSourceListener() {
        return this.fileDragSourceListener;
    }


    /**
     * Returns the MainFrame that contains this panel.
     *
     * @return the MainFrame that contains this panel
     */
    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    /**
     * Returns the FileTable contained by this panel.
     *
     * @return the FileTable contained by this panel
     */
    public FileTable getFileTable() {
        return this.fileTable;
    }

    /**
     * Returns the JScrollPane that contains the FileTable component and allows it to scroll.
     *
     * @return the JScrollPane that contains the FileTable component and allows it to scroll
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Returns the LocationTextField contained by this panel.
     *
     * @return the LocationTextField contained by this panel
     */
    public LocationTextField getLocationTextField() {
        return locationTextField;
    }

    /**
     * Returns the DrivePopupButton contained by this panel.
     *
     * @return the DrivePopupButton contained by this panel
     */
    public DrivePopupButton getDriveButton() {
        return driveButton; 
    }

    /**
     * Returns the visited folders history, wrapped in a FolderHistory object.
     *
     * @return the visited folders history, wrapped in a FolderHistory object
     */
    public LocationHistory getFolderHistory() {
        return this.folderHistory;
    }

    /**
     * Returns the LocationManager instance that notifies registered listeners of location changes
     * that occur in this FolderPanel.
     *
     * @return the LocationManager instance that notifies registered listeners of location changes that occur in
     * this FolderPanel
     */
    public LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * Allows the user to easily change the current folder and type a new one: requests focus 
     * on the location field and selects the folder string.
     */
    public void changeCurrentLocation() {
    	locationTextField.selectAll();
    	locationTextField.requestFocus();
    }
	

    /**
     * Returns the folder that is currently being displayed by this panel.
     *
     * @return the folder that is currently being displayed by this panel
     */
    public AbstractFile getCurrentFolder() {
        return currentFolder;
    }


    /**
     * Displays a popup dialog informing the user that the requested folder doesn't exist or isn't available.
     */
    private void showFolderDoesNotExistDialog() {
        InformationDialog.showErrorDialog(mainFrame, Translator.get("table.folder_access_error_title"), Translator.get("folder_does_not_exist"));
    }


    /**
     * Displays a popup dialog informing the user that the requested folder couldn't be opened.
     *
     * @param e the Exception that was caught while changing the folder
     */
    private void showAccessErrorDialog(Exception e) {
        InformationDialog.showErrorDialog(mainFrame, Translator.get("table.folder_access_error_title"), Translator.get("table.folder_access_error"), e==null?null:e.getMessage(), e);
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
        FileSet fileSet = new FileSet(currentFolder);
        fileSet.add(file);
		
        // Show confirmation/path modification dialog
        new DownloadDialog(mainFrame, fileSet).showDialog();
    }
	
    /**
     * Tries to change the current folder to the new specified one and notifies the user in case of a problem.
     *
     * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
     * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
     *
     * <p>
     * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
     * </p>
     *
     * @param folder the folder to be made current folder
     * @return the thread that performs the actual folder change, null if another folder change is already underway
     */
    public ChangeFolderThread tryChangeCurrentFolder(AbstractFile folder) {
        /* TODO branch setBranchView(false); */
        return tryChangeCurrentFolder(folder, null, false);
    }

    /**
     * Tries to change current folder to the new specified one, and selects the given file after the folder has been
     * changed. The user is notified by a dialog if the folder could not be changed.
     *
     * <p>If the current folder could not be changed to the requested folder and <code>findWorkableFolder</code> is
     * <code>true</code>, the current folder will be changed to the first existing parent of the request folder if there
     * is one, to the first existing local volume otherwise. In the unlikely event that no local volume is workable,
     * the user will be notified that the folder could not be changed.</p>
     *
     * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
     * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
     *
     * <p>
     * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
     * </p>
     *
     * @param folder the folder to be made current folder
     * @param selectThisFileAfter the file to be selected after the folder has been changed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
     * @return the thread that performs the actual folder change, null if another folder change is already underway  
     */
    public ChangeFolderThread tryChangeCurrentFolder(AbstractFile folder, AbstractFile selectThisFileAfter, boolean findWorkableFolder) {
        AppLogger.finer("folder="+folder+" selectThisFileAfter="+selectThisFileAfter);

        synchronized(FOLDER_CHANGE_LOCK) {
            // Make sure a folder change is not already taking place. This can happen under rare but normal
            // circumstances, if this method is called before the folder change thread has had the time to call
            // MainFrame#setNoEventsMode.
            if(changeFolderThread!=null) {
                AppLogger.fine("A folder change is already taking place ("+changeFolderThread+"), returning null");
                return null;
            }

            this.changeFolderThread = new ChangeFolderThread(folder, findWorkableFolder);
            if(selectThisFileAfter!=null)
                this.changeFolderThread.selectThisFileAfter(selectThisFileAfter);
            changeFolderThread.start();

            return changeFolderThread;
        }
    }

    /**
     * Tries to change the current folder to the specified path and notifies the user in case of a problem.
     *
     * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
     * It does nothing and returns <code>null</code> if another folder change is already underway or if the given
     * path could not be resolved.</p>
     *
     * <p>
     * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
     * </p>
     *
     * @param folderPath path to the new current folder. If this path does not resolve into a file, an error message will be displayed.
     * @return the thread that performs the actual folder change, null if another folder change is already underway or if the given path could not be resolved
     */
    public ChangeFolderThread tryChangeCurrentFolder(String folderPath) {
        try {
            return tryChangeCurrentFolder(FileURL.getFileURL(folderPath), null);
        }
        catch(MalformedURLException e) {
            // FileURL could not be resolved, notify the user that the folder doesn't exist
            showFolderDoesNotExistDialog();

            return null;
        }
    }

    /**
     * Tries to change current folder to the new specified URL and notifies the user in case of a problem.
     *
     * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
     * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
     *
     * <p>
     * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
     * </p>
     *
     * @param folderURL location to the new current folder. If this URL does not resolve into a file, an error message will be displayed.
     * @return the thread that performs the actual folder change, null if another folder change is already underway
     */
    public ChangeFolderThread tryChangeCurrentFolder(FileURL folderURL) {
        return tryChangeCurrentFolder(folderURL, null);
    }

    /**
     * Tries to change current folder to the new specified path and notifies the user in case of a problem.
     * If not <code>null</code>, the specified {@link com.mucommander.auth.CredentialsMapping} is used to authenticate
     * the folder, and added to {@link CredentialsManager} if the folder has been successfully changed.</p>
     *
     * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
     * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
     *
     * <p>
     * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
     * </p>
     *
     * @param folderURL folder's URL to be made current folder. If this URL does not resolve into an existing file, an error message will be displayed.
     * @param credentialsMapping the CredentialsMapping to use for authentication, can be null
     * @return the thread that performs the actual folder change, null if another folder change is already underway
     */
    public ChangeFolderThread tryChangeCurrentFolder(FileURL folderURL, CredentialsMapping credentialsMapping) {
        AppLogger.finer("folderURL="+folderURL);

        synchronized(FOLDER_CHANGE_LOCK) {
            // Make sure a folder change is not already taking place. This can happen under rare but normal
            // circumstances, if this method is called before the folder change thread has had the time to call
            // MainFrame#setNoEventsMode.
            if(changeFolderThread!=null) {
                AppLogger.fine("A folder change is already taking place ("+changeFolderThread+"), returning null");
                return null;
            }

            this.changeFolderThread = new ChangeFolderThread(folderURL);
            changeFolderThread.setCredentialsMapping(credentialsMapping);
            changeFolderThread.start();

            return changeFolderThread;
        }
    }

    /**
     * Shorthand for {@link #tryRefreshCurrentFolder(AbstractFile)} called with no specific file (<code>null</code>)
     * to select after the folder has been changed.
     *
     * @return the thread that performs the actual folder change, null if another folder change is already underway
     */
    public ChangeFolderThread tryRefreshCurrentFolder() {
        return tryRefreshCurrentFolder(null);
    }

    /**
     * Refreshes the current folder's contents. If the folder is no longer available, the folder will be changed to a
     * 'workable' folder (see {@link #tryChangeCurrentFolder(AbstractFile, AbstractFile, boolean)}.
     *
     * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
     * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
     *
     * <p>This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.</p>
     *
     * @param selectThisFileAfter file to be selected after the folder has been refreshed (if it exists in the folder),
     * can be null in which case FileTable rules will be used to select current file
     * @return the thread that performs the actual folder change, null if another folder change is already underway
     * @see #tryChangeCurrentFolder(AbstractFile, AbstractFile, boolean)
     */
    public ChangeFolderThread tryRefreshCurrentFolder(AbstractFile selectThisFileAfter) {
        foldersTreePanel.refreshFolder(currentFolder);
        return tryChangeCurrentFolder(currentFolder, selectThisFileAfter, true);
    }
		
    /**
     * Changes current folder using the given folder and children files.
     *
     * <p>
     * This method <b>is</b> I/O-bound and locks the calling thread until the folder has been changed. It may under
     * certain circumstances lock indefinitely, for example when accessing network-based filesystems.
     * </p>
     *
     * @param folder folder to be made current folder
     * @param children current folder's files (value of folder.ls())
     * @param fileToSelect file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
     */
    private void setCurrentFolder(AbstractFile folder, AbstractFile children[], AbstractFile fileToSelect) {
        // Update the timestamp right before the folder is set in case FolderChangeMonitor checks the timestamp
        // while FileTable#setCurrentFolder is being called. 
        lastFolderChangeTime = System.currentTimeMillis();

        // Change the current folder in the table and select the given file if not null
        if(fileToSelect == null)
            fileTable.setCurrentFolder(folder, children);
        else
            fileTable.setCurrentFolder(folder, children, fileToSelect);

        // Update the current folder's value now that it is set
        this.currentFolder = folder;

        // Add the folder to history
        folderHistory.addToHistory(folder);

        // Notify listeners that the location has changed
        locationManager.fireLocationChanged(folder.getURL());
    }


    /**
     * Returns <code>true</code> ´if the current folder is currently being changed, <code>false</code> otherwise.
     *
     * @return <code>true</code> ´if the current folder is currently being changed, <code>false</code> otherwise
     */
    public boolean isFolderChanging() {
        return changeFolderThread!=null;
    }

    /**
     * Returns the time at which the last folder change completed successfully.
     *
     * @return the time at which the last folder change completed successfully.
     */
    public long getLastFolderChangeTime() {
        return lastFolderChangeTime;
    }

    /**
     * Returns the thread that is currently changing the current folder, <code>null</code> is the folder is not being
     * changed.
     *
     * @return the thread that is currently changing the current folder, <code>null</code> is the folder is not being
     * changed
     */
    public ChangeFolderThread getChangeFolderThread() {
        return changeFolderThread;
    }


    /**
     * Returns the FolderChangeMonitor which monitors changes in the current folder and automatically refreshes it.
     *
     * @return the FolderChangeMonitor which monitors changes in the current folder and automatically refreshes it
     */
    public FolderChangeMonitor getFolderChangeMonitor() {
        return folderChangeMonitor;
    }


    /**
     * Dims the scrollpane's background, called by {@link com.mucommander.ui.main.table.FileTable.QuickSearch} when a quick search is started.
     */
    public void dimBackground() {
        fileTable.setBackground(unmatchedBackgroundColor);
        scrollPane.getViewport().setBackground(unmatchedBackgroundColor);
    }

    /**
     * Stops dimming the scrollpane's background (returns to a normal background color), called by
     * {@link com.mucommander.ui.main.table.FileTable.QuickSearch} when a quick search is over.
     */
    public void undimBackground() {
        Color newColor;

        // Identifies the new background color.
        if(fileTable.hasFocus())
            newColor = backgroundColor;
        else
            newColor = unfocusedBackgroundColor;

        // If the old and new background color differ, set the new background
        // color.
        // Otherwise, repaint the table - if we were to skip that step, quicksearch
        // cancellation might result in a corrupt display.
        if(newColor.equals(scrollPane.getViewport().getBackground()))
            fileTable.repaint();
        else {
            fileTable.setBackground(newColor);
            scrollPane.getViewport().setBackground(newColor);
        }
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
    public void setBorderColor(Color color) {
        Border border;
        // Some (rather evil) look and feels will change borders outside of muCommander's control,
        // this check is necessary to ensure no exception is thrown.
        if((border = scrollPane.getBorder()) instanceof MutableLineBorder)
            ((MutableLineBorder)border).setLineColor(color);
    }

    public void focusGained(FocusEvent e) {
        // Notify MainFrame that we are in control now! (our table/location field is active)
        mainFrame.setActiveTable(fileTable);
        if(e.getSource() == fileTable) {
            setBorderColor(borderColor);
            scrollPane.getViewport().setBackground(backgroundColor);
            fileTable.setBackground(backgroundColor);
        }
    }

    public void focusLost(FocusEvent e) {
        if(e.getSource() == fileTable) {
            setBorderColor(unfocusedBorderColor);
            scrollPane.getViewport().setBackground(unfocusedBackgroundColor);
            fileTable.setBackground(unfocusedBackgroundColor);
        }
        fileTable.getQuickSearch().stop();
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
     * then become very hard to track. Sun's Javadoc doesn't make it clear enough... just don't!</p>
     *
     * @author Maxence Bernard
     */
    public class ChangeFolderThread extends Thread {

        private AbstractFile folder;
        private boolean findWorkableFolder;
        private FileURL folderURL;
        private AbstractFile fileToSelect;
        private CredentialsMapping credentialsMapping;

        /** True if this thread has been interrupted by the user using #tryKill */
        private boolean killed;
        /** True if an attempt to kill this thread using Thread#interrupt() has already been made */
        private boolean killedByInterrupt;
        /** True if an attempt to kill this thread using Thread#stop() has already been made */
        private boolean killedByStop;
        /** True if it is unsafe to kill this thread */
        private boolean doNotKill;

        private boolean disposed;

        /** Lock object used to ensure consistency and thread safeness when killing the thread */
        private final Object KILL_LOCK = new Object();
        
        /* TODO branch private ArrayList childrenList; */


        public ChangeFolderThread(AbstractFile folder, boolean findWorkableFolder) {
            // Ensure that we work on a raw file instance and not a cached one
            this.folder = (folder instanceof CachedFile)?((CachedFile)folder).getProxiedFile():folder;
            this.folderURL = folder.getURL();
            this.findWorkableFolder = findWorkableFolder;

            setPriority(Thread.MAX_PRIORITY);
        }

        public ChangeFolderThread(FileURL folderURL) {
            this.folderURL = folderURL;

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
         * Sets the {@link CredentialsMapping} to use for accessing the folder, <code>null</code> for none.
         * 
         * @param credentialsMapping the CredentialsMapping to use
         */
        public void setCredentialsMapping(CredentialsMapping credentialsMapping) {
            this.credentialsMapping = credentialsMapping;
        }

        /**
         * Returns a 'workable' folder as a substitute for the given non-existing folder. This method will return the
         * first existing parent if there is one, to the first existing local volume otherwise. In the unlikely event
         * that no local volume exists, <code>null</code> will be returned.
         *
         * @param folder folder for which to find a workable folder
         * @return a 'workable' folder for the given non-existing folder, <code>null</code> if there is none.
         */
        private AbstractFile getWorkableFolder(AbstractFile folder) {
            // Look for an existing parent
            AbstractFile newFolder = folder;
            do {
                newFolder = newFolder.getParent();
                if(newFolder!=null && newFolder.exists())
                    return newFolder;
            }
            while(newFolder!=null);

            // Fall back to the first existing volume
            AbstractFile[] localVolumes = LocalFile.getVolumes();
            for(AbstractFile volume : localVolumes) {
                if(volume.exists())
                    return volume;
            }

            // No volume could be found, return null
            return null;
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
            return (MuConfiguration.getVariable(MuConfiguration.CD_FOLLOWS_SYMLINKS, MuConfiguration.DEFAULT_CD_FOLLOWS_SYMLINKS)
                || file.getURL().getScheme().equals(FileProtocols.HTTP))
            && !file.getAbsolutePath(false).equals(file.getCanonicalPath(false));
        }

        /**
         * Attempts to stop this thread and returns <code>true</code> if an attempt was made.
         * An attempt to stop this thread will be made using one of the methods detailed hereunder, only if
         * it is still safe to do so: if the thread is too far into the process of changing the current folder,
         * this method will have no effect and return <code>false</code>.
         *
         * <p>The first time this method is called, {@link #interrupt()} is called, giving the thread a chance to stop
         * gracefully should it be waiting for a thread or blocked in an interruptible operation such as an
         * InterruptibleChannel. This may have no immediate effect if the thread is blocked in a non-interruptible
         * operation. This thread will however be marked as 'killed' which will sooner or later cause {@link #run()}
         * to stop the thread by simply returning.</p> 
         *
         * <p>The second time this method is called, the deprecated (and unsafe) {@link #stop()} method is called,
         * forcing the thread to abort.</p>
         *
         * <p>Any subsequent calls to this method will have no effect and return <code>false</code>.</p>
         *
         * @return true if an attempt was made to stop this thread.
         */
        public boolean tryKill() {
            synchronized(KILL_LOCK) {
                if(killedByStop) {
                    AppLogger.fine("Thread already killed by #interrupt() and #stop(), there's nothing we can do, returning");
                    return false;
                }

                if(doNotKill) {
                    AppLogger.fine("Can't kill thread now, it's too late, returning");
                    return false;
                }

                // This field needs to be set before actually killing the thread, #run() relies on it
                killed = true;

                // Call Thread#interrupt() the first time this method is called to give the thread a chance to stop
                // gracefully if it is waiting in Thread#sleep() or Thread#wait() or Thread#join() or in an
                // interruptible operation such as java.nio.channel.InterruptibleChannel. If this is the case,
                // InterruptedException or ClosedByInterruptException will be thrown and thus need to be catched by
                // #run().
                if(!killedByInterrupt) {
                    AppLogger.fine("Killing thread using #interrupt()");

                    // This field needs to be set before actually interrupting the thread, #run() relies on it
                    killedByInterrupt = true;
                    interrupt();
                }
                // Call Thread#stop() the first time this method is called
                else {
                    AppLogger.fine("Killing thread using #stop()");

                    killedByStop = true;
                    super.stop();
                    // Execute #cleanup() as it would have been done by #run() had the thread not been stopped.
                    // Note that #run() may end pseudo-gracefully and catch the underlying Exception. In this case
                    // it will also call #cleanup() but the (2nd) call to #cleanup() will be ignored.
                    cleanup(false);
                }

                return true;
            }
        }


        @Override
        public void start() {
            // Notify listeners that location is changing
            locationManager.fireLocationChanging(folder==null?folderURL:folder.getURL());

            super.start();
        }


        @Override
        public void run() {
            AppLogger.finer("starting folder change...");
            boolean folderChangedSuccessfully = false;

            // Show some progress in the progress bar to give hope
            locationTextField.setProgressValue(10);

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
            else if(!folderURL.containsCredentials() &&
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

                            synchronized(KILL_LOCK) {
                                if(killed) {
                                    AppLogger.fine("this thread has been killed, returning");
                                    break;
                                }
                            }

                            // File resolved -> 25% complete
                            locationTextField.setProgressValue(25);

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
                                AbstractFile newFolder = getWorkableFolder(folder);
                                if(newFolder.equals(folder)) {
                                    // If we've already tried the returned folder, give up (avoids a potentially endless loop)
                                    showFolderDoesNotExistDialog();
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
                                showFolderDoesNotExistDialog();
                                break;
                            }
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
                                AppLogger.fine("this thread has been killed, returning");
                                break;
                            }
                        }

                        // File tested -> 50% complete
                        locationTextField.setProgressValue(50);

                        AppLogger.finer("calling ls()");

                        /* TODO branch 
                        AbstractFile children[] = new AbstractFile[0];
                        if (branchView) {
                            childrenList = new ArrayList();
                            readBranch(folder);
                            children = (AbstractFile[]) childrenList.toArray(children);
                        } else {
                            children = folder.ls(chainedFileFilter);                            
                        } */
                        AbstractFile children[] = folder.ls(configurableFolderFilter);
                        
                        synchronized(KILL_LOCK) {
                            if(killed) {
                                AppLogger.fine("this thread has been killed, returning");
                                break;
                            }
                            // From now on, thread cannot be killed (would comprise table integrity)
                            doNotKill = true;
                        }

                        // files listed -> 75% complete
                        locationTextField.setProgressValue(75);

                        AppLogger.finer("calling setCurrentFolder");

                        // Change the file table's current folder and select the specified file (if any)
                        setCurrentFolder(folder, children, fileToSelect);

                        // folder set -> 95% complete
                        locationTextField.setProgressValue(95);

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
                        AppLogger.fine("Caught exception", e);

                        if(killed) {
                            // If #tryKill() called #interrupt(), the exception we just caught was most likely
                            // thrown as a result of the thread being interrupted.
                            //
                            // The exception can be a java.lang.InterruptedException (Thread throws those),
                            // a java.nio.channels.ClosedByInterruptException (InterruptibleChannel throws those)
                            // or any other exception thrown by some code that swallowed the original exception
                            // and threw a new one.

                            AppLogger.fine("Thread was interrupted, ignoring exception");
                            break;
                        }

                        // Restore default cursor
                        mainFrame.setCursor(Cursor.getDefaultCursor());

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
                                AbstractFile newFolder = getWorkableFolder(folder);
                                if(newFolder.equals(folder)) {
                                    // If we've already tried the returned folder, give up (avoids a potentially endless loop)
                                    showFolderDoesNotExistDialog();
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


        /* TODO branch         
        /**
         * Reads all files in the current directory and all its subdirectories.
         * @param parent
         * /
        private void readBranch(AbstractFile parent) {
            AbstractFile[] children;
            try {
                children = parent.ls(chainedFileFilter);
                for (int i=0; i<children.length; i++) {
                    if (children[i].isDirectory()) {
                        readBranch(children[i]);
                    } else {
                        childrenList.add(children[i]);
                    }
                }
            } catch (IOException e) {
                AppLogger.fine("Caught exception", e);
            }
        }
        */

        public void cleanup(boolean folderChangedSuccessfully) {
            // Ensures that this method is called only once
            synchronized(KILL_LOCK) {
                if(disposed) {
                    AppLogger.fine("already called, returning");
                    return;
                }

                disposed = true;
            }

            AppLogger.finer("cleaning up, folderChangedSuccessfully="+folderChangedSuccessfully);

            // Clear the interrupted flag in case this thread has been killed using #interrupt().
            // Not doing this could cause some of the code called by this method to be interrupted (because this thread
            // is interrupted) and throw an exception
            interrupted();

            // Reset location field's progress bar
            locationTextField.setProgressValue(0);

            // Restore normal mouse cursor
            mainFrame.setCursor(Cursor.getDefaultCursor());

            changeFolderThread = null;

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


        // For debugging purposes
        public String toString() {
            return super.toString()+" folderURL="+folderURL+" folder="+folder;
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
            borderColor = event.getColor();
            if(fileTable.hasFocus()) {
                setBorderColor(borderColor);
                scrollPane.repaint();
            }
            break;
        case Theme.FILE_TABLE_INACTIVE_BORDER_COLOR:
            unfocusedBorderColor = event.getColor();
            if(!fileTable.hasFocus()) {
                setBorderColor(unfocusedBorderColor);
                scrollPane.repaint();
            }
            break;
        case Theme.FILE_TABLE_BACKGROUND_COLOR:
            backgroundColor = event.getColor();
            if(fileTable.hasFocus()) {
                scrollPane.getViewport().setBackground(backgroundColor);
                fileTable.setBackground(backgroundColor);
            }
            break;
        case Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR:
            unfocusedBackgroundColor = event.getColor();
            if(!fileTable.hasFocus()) {
                scrollPane.getViewport().setBackground(unfocusedBackgroundColor);
                fileTable.setBackground(unfocusedBackgroundColor);
            }
            break;

        case Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR:
            unmatchedBackgroundColor = event.getColor();
            break;
        }
    }

    /**
     * Not used.
     */
    public void fontChanged(FontChangedEvent event) {}

    /**
     * Shows the pop up which is located the given index in fileTablePopups.
     * 
     * @param index - index of the FileTablePopup in fileTablePopups.
     */
    public void showQuickList(int index) {
    	fileTablePopups[index].show(this);
    }
    
    /**
     * Returns true if a directory tree is visible.
     */
    public boolean isTreeVisible() {
        return treeVisible;
    }
    
    /**
     * Returns width of a folders tree.
     * @return a width of a folders tree
     */
    public int getTreeWidth() {
        if (!treeVisible) {
            return oldTreeWidth;
        } else {
        	return treeSplitPane.getDividerLocation();
        }
    }

    /**
     * Sets a width of a folders tree.
     * @param width new width
     */
    public void setTreeWidth(int width) {
        if (!treeVisible) {
            oldTreeWidth = width;
        } else {
        	treeSplitPane.setDividerLocation(width);
        	treeSplitPane.doLayout();
        }
    }

    /**
     * Returns a panel with a folders tree.
     * @return a panel with a folders tree
     */
    public FoldersTreePanel getFoldersTreePanel() {
        return foldersTreePanel;
    }


    /**
     * Enables/disables a directory tree visibility. Invoked by {@link com.mucommander.ui.action.impl.ToggleTreeAction}.
     */
    public void setTreeVisible(boolean treeVisible) {
    	if (this.treeVisible != treeVisible) {
	        this.treeVisible = treeVisible;
	        if (!treeVisible) {
	            // save width of a tree panel
	            oldTreeWidth = treeSplitPane.getDividerLocation();
	        }
	        foldersTreePanel.setVisible(treeVisible);
	        // hide completly divider if a tree isn't visible
	        treeSplitPane.setDividerLocation(treeVisible ? oldTreeWidth : 0);
	        treeSplitPane.setDividerSize(treeVisible ? 5 : 0);
	        foldersTreePanel.requestFocus();
    	}
    }

    /* TODO branch 
    /**
     * Returns true if branch view is enabled for this folder panel.
     * @return
     * /
    public boolean isBranchView() {
        return branchView;
    }
    
    /**
     * Enables/disables branch view.
     * @see ToggleBranchViewAction
     * @param branchView
     * /
    public void setBranchView(boolean branchView) {
        this.branchView = branchView;
    }
    */

}

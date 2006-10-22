package com.mucommander.ui;

import com.mucommander.cache.LRUCache;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;
import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * StatusBar is the component that sits at the bottom of each MainFrame, between the folder panels and command bar.
 * There is one and only one StatusBar per MainFrame, created by the associated MainFrame. It can be hidden, 
 * but the instance will always remain, until the MainFrame is disposed. 
 *
 * <p>StatusBar is used to display info about the total/selected number of files in the current folder and current volume's
 * free/total space. When a folder is being changed, a waiting message is displayed. When quick search is being used,
 * the current quick search string is displayed.
 *
 * <p>StatusBar receives LocationListener events when the folder has or is being changed, and automatically updates
 * selected files and volume info, and display the waiting message when the folder is changing. Quick search info
 * is set by FileTable.QuickSearch.
 *
 * <p>When StatusBar is visible, a Thread runs in the background to periodically update free/total space volume info.
 * This thread stops when the StatusBar is hidden.
 *
 * @author Maxence Bernard
 */
public class StatusBar extends JPanel implements Runnable, MouseListener, ActivePanelListener, TableSelectionListener, LocationListener, ComponentListener {

    private MainFrame mainFrame;

    /** Label that displays info about current selected file(s) */
    private JLabel statusBarFilesLabel;
	
    /** Label that displays info about current volume (free/total space) */
    private JLabel statusBarVolumeLabel;

    /** Thread which auto updates volume info */
    private Thread autoUpdateThread;

    /** Number of volume info strings that can be temporarily cached */
    private final static int VOLUME_INFO_CACHE_CAPACITY = 50;

    /** Number of milliseconds before cached volume info strings expire */
    private final static int VOLUME_INFO_TIME_TO_LIVE = 60000;

    /** Number of milliseconds between each volume info update by auto-update thread */
    private final static int AUTO_UPDATE_PERIOD = 6000;

    /** Caches volume info strings (free/total space) for a while, since it is quite costly and we don't want
     * to recalculate it each time this information is requested.
     * Each cache item maps a path to a volume info string */
    private static LRUCache volumeInfoCache = LRUCache.createInstance(VOLUME_INFO_CACHE_CAPACITY);
	
    /** SizeFormatter's format used to display volume info in status bar */
    private final static int VOLUME_INFO_SIZE_FORMAT = SizeFormatter.DIGITS_SHORT|SizeFormatter.UNIT_SHORT|SizeFormatter.INCLUDE_SPACE|SizeFormatter.ROUND_TO_KB;

    /** Icon that is displayed when folder is changing */
    private final static String WAITING_ICON = "waiting.png";


    /**
     * Creates a new StatusBar instance.
     */
    public StatusBar(MainFrame mainFrame) {
        // Create and add status bar
        super(new BorderLayout());
		
        this.mainFrame = mainFrame;
		
        this.statusBarFilesLabel = new JLabel("");
//        // Display any icon after the text
//        this.statusBarFilesLabel.setHorizontalTextPosition(JLabel.LEADING);
        add(statusBarFilesLabel, BorderLayout.CENTER);

        this.statusBarVolumeLabel = new JLabel("");
        add(statusBarVolumeLabel, BorderLayout.EAST);

        // Show/hide this status bar based on user preferences
        // Note: setVisible has to be called even with true for the auto-update thread to be initialized
        setVisible(ConfigurationManager.getVariableBoolean("prefs.status_bar.visible", true));
        
        // Catch location events to update status bar info when folder is changed
        FolderPanel folderPanel1 = mainFrame.getFolderPanel1();
        folderPanel1.getLocationManager().addLocationListener(this);

        FolderPanel folderPanel2 = mainFrame.getFolderPanel2();
        folderPanel2.getLocationManager().addLocationListener(this);

        // Catch table selection change events to update the selected files info when the selected files have changed on
        // one of the file tables
        folderPanel1.getFileTable().addTableSelectionListener(this);
        folderPanel2.getFileTable().addTableSelectionListener(this);

        // Catch active panel change events to update status bar info when current table has changed
        mainFrame.addActivePanelListener(this);
		
        // Catch mouse events to pop up a menu on right-click
        statusBarFilesLabel.addMouseListener(this);
        statusBarVolumeLabel.addMouseListener(this);
        addMouseListener(this);
		
        // Catch component events to be notified when this component is made visible
        // and update status info
        addComponentListener(this);
    }


    /**
     * Updates info displayed on the status bar: currently selected files and volume info.
     */
    private void updateStatusInfo() {
        // No need to waste precious cycles if status bar is not visible
        if(!isVisible())
            return;

        updateSelectedFilesInfo();
        updateVolumeInfo();
    }
	

    /**
     * Updates info about currently selected files ((nb of selected files, combined size), displayed on the left-side of this status bar.
     */
// Making this method synchronized creates a deadlock with FileTable
//    public synchronized void updateSelectedFilesInfo() {
    public void updateSelectedFilesInfo() {
        // No need to waste precious cycles if status bar is not visible
        if(!isVisible())
            return;

        FileTable currentFileTable = mainFrame.getLastActiveTable();

        // Currently select file, can be null
        AbstractFile selectedFile = currentFileTable.getSelectedFile();
        FileTableModel tableModel = currentFileTable.getFileTableModel();
        // Number of marked files, can be 0
        int nbMarkedFiles = tableModel.getNbMarkedFiles();
        // Combined size of marked files, 0 if no file has been marked
        long markedTotalSize = tableModel.getTotalMarkedSize();
        // number of files in folder
        int fileCount = tableModel.getFileCount();

        // Update files info based on marked files if there are some, or currently selected file otherwise
        int nbSelectedFiles;
        if(nbMarkedFiles==0 && selectedFile!=null)
            nbSelectedFiles = 1;
        else
            nbSelectedFiles = nbMarkedFiles;

        boolean compactFileSize = ConfigurationManager.getVariableBoolean("prefs.display_compact_file_size", true);
        String filesInfo;
		
        if(fileCount==0) {
            // Set status bar to a space character, not an empty string
            // otherwise it will disappear
            filesInfo = " ";
        }
        else {
            filesInfo = Translator.get("status_bar.selected_files", ""+nbSelectedFiles, ""+fileCount);
			
            if(nbMarkedFiles>0)
                filesInfo += " - "+SizeFormatter.format(markedTotalSize, (compactFileSize?SizeFormatter.DIGITS_SHORT:SizeFormatter.DIGITS_FULL)|(compactFileSize?SizeFormatter.UNIT_SHORT:SizeFormatter.UNIT_LONG)|SizeFormatter.INCLUDE_SPACE|SizeFormatter.ROUND_TO_KB);
	
            if(selectedFile!=null)
                filesInfo += " - "+selectedFile.getName();
        }		

        // Update label
        setStatusInfo(filesInfo);
    }
	
	
    /**
     * Updates info about current volume (free space, total space), displayed on the right-side of this status bar.
     */
    private synchronized void updateVolumeInfo() {
        // No need to waste precious cycles if status bar is not visible
        if(!isVisible())
            return;

        final AbstractFile currentFolder = mainFrame.getLastActiveTable().getCurrentFolder();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, currentFolder="+currentFolder);

        String cachedVolumeInfo = (String)volumeInfoCache.get(currentFolder.getAbsolutePath());
        if(cachedVolumeInfo!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Cache hit!");
            statusBarVolumeLabel.setText(cachedVolumeInfo);
        }
        else {
            // Retrieves free and total volume space.
            // Perform volume info retrieval in a separate thread as this method may be called
            // by the event thread and it can take a while, we want to return as soon as possible
            new Thread() {
                public void run() {
                    // Free space on current volume, -1 if this information is not available 
                    long volumeFree;
                    // Total space on current volume, -1 if this information is not available 
                    long volumeTotal;

                    // Folder is a local file : call getVolumeInfo() instead of separate calls to getFreeSpace()
                    // and getTotalSpace() as it is twice as fast
                    if(currentFolder instanceof FSFile) {
                        long volumeInfo[] = ((FSFile)currentFolder).getVolumeInfo();
                        volumeTotal = volumeInfo[0];
                        volumeFree = volumeInfo[1];
                    }
                    // Any other file kind
                    else {
                        volumeFree = currentFolder.getFreeSpace();
                        volumeTotal = currentFolder.getTotalSpace();
                    }

                    String volumeInfo;
                    if(volumeFree!=-1) {
                        volumeInfo = SizeFormatter.format(volumeFree, VOLUME_INFO_SIZE_FORMAT);
                        if(volumeTotal!=-1)
                            volumeInfo += " / "+ SizeFormatter.format(volumeTotal, VOLUME_INFO_SIZE_FORMAT);
                        volumeInfo = Translator.get("status_bar.volume_free", volumeInfo);
                    }
                    else if(volumeTotal!=-1) {
                        volumeInfo = SizeFormatter.format(volumeTotal, VOLUME_INFO_SIZE_FORMAT);
                        volumeInfo = Translator.get("status_bar.volume_capacity", volumeInfo);
                    }
                    else {
                        volumeInfo = "";
                    }
                    statusBarVolumeLabel.setText(volumeInfo);
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Adding to cache");
                    volumeInfoCache.add(currentFolder.getAbsolutePath(), volumeInfo, VOLUME_INFO_TIME_TO_LIVE);
                }
            }.start();
        }
    }


    /**
     * Displays the specified text and icon on the left-side of the status bar, 
     * replacing any previous information.
     *
     * @param text the piece of text to display
     * @param icon the icon to display next to the text
     * @param iconBeforeText if true, icon will be placed on the left side of the text, if not on the right side
     */
    public void setStatusInfo(String text, Icon icon, boolean iconBeforeText) {
        //if(com.mucommander.Debug.ON) text += " - freeMem="+Runtime.getRuntime().freeMemory()+" - totalMem="+Runtime.getRuntime().totalMemory();
        statusBarFilesLabel.setText(text);
        statusBarFilesLabel.setIcon(icon);
        if(icon!=null)
            statusBarFilesLabel.setHorizontalTextPosition(iconBeforeText?JLabel.TRAILING:JLabel.LEADING);
        //		statusBarVolumeLabel.setText("");
    }

	
    /**
     * Displays the specified text on the left-side of the status bar, 
     * replacing any previous text and icon.
     *
     * @param infoMessage the piece of text to display
     */
    public void setStatusInfo(String infoMessage) {
        setStatusInfo(infoMessage, null, false);
    }
	

    /**
     * Starts a volume info auto-update thread, only if there isn't already one running.
     */    
    private synchronized void startAutoUpdate() {
        if(autoUpdateThread==null) {
            // Start volume info auto-update thread
            autoUpdateThread = new Thread(this);
            // Set the thread as a daemon thread
            autoUpdateThread.setDaemon(true);
            autoUpdateThread.start();
        }
    }

    
    /**
     * Overrides JComponent.setVisible(boolean) to start/stop volume info auto-update thread.
     */
    public void setVisible(boolean visible) {
        if(visible) {
            // Start auto-update thread
            startAutoUpdate();
            super.setVisible(true);
            // Update status bar info
            updateStatusInfo();
        }
        else {
            // Stop auto-update thread
            this.autoUpdateThread = null;
            super.setVisible(false);
        }
    }
    
    
    //////////////////////
    // Runnable methods //
    //////////////////////

    /**
     * Periodically updates volume info (free / total space).
     */
    public void run() {
        do {
            // Sleep for a while
            try { Thread.sleep(AUTO_UPDATE_PERIOD); }
            catch (InterruptedException e) {}
            
            // Update volume info if:
            // - status bar is visible
            // - MainFrame isn't changing folders
            // - MainFrame is active and in the foreground
            // Volume info update will potentially hit the LRU cache and not actually update volume info
            if(isVisible() && !mainFrame.getNoEventsMode() && mainFrame.isForegroundActive())
                updateVolumeInfo();
        }
        while(autoUpdateThread!=null && mainFrame.isVisible());   // Stop when MainFrame is disposed
    }
    

    ////////////////////////////////////////
    // ActivePanelListener implementation //
    ////////////////////////////////////////
	
    public void activePanelChanged(FolderPanel folderPanel) {
        updateStatusInfo();
    }


    ///////////////////////////////////////////
    // TableSelectionListener implementation //
    ///////////////////////////////////////////

    public void selectedFileChanged(FileTable source) {
        // No need to update if the originating FileTable is not the currently active one
        if(source==mainFrame.getLastActiveTable())
            updateSelectedFilesInfo();
    }

    public void markedFilesChanged(FileTable source) {
        // No need to update if the originating FileTable is not the currently active one
        if(source==mainFrame.getLastActiveTable())
            updateSelectedFilesInfo();
    }


    /////////////////////////////////////
    // LocationListener implementation //
    /////////////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        updateStatusInfo();
    }

    public void locationChanging(LocationEvent e) {
        // Show a message in the status bar saying that folder is being changed
        setStatusInfo(Translator.get("status_bar.connecting_to_folder"), IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, WAITING_ICON), true);
    }
	
    public void locationCancelled(LocationEvent e) {
        updateStatusInfo();
    }
	
	
    //////////////////////////////////
    // MouseListener implementation //
    //////////////////////////////////
	
    public void mouseClicked(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        // Right clicking on the toolbar brings up a popup menu that allows the user to hide this status bar
        int modifiers = e.getModifiers();
        if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {
            //		if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleStatusBarAction.class, mainFrame));
            popupMenu.show(this, e.getX(), e.getY());
            popupMenu.setVisible(true);
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }
	
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }	
	
	
    //////////////////////////////////////
    // ComponentListener implementation //
    //////////////////////////////////////
	
    public void componentShown(ComponentEvent e) {
        // Invoked when the component has been made visible (apparently not called when just created)
        // Status bar needs to be updated sihce it is not updated when not visible
        updateStatusInfo();
    }     

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }
}

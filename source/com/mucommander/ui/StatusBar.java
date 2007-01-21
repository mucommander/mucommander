package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;

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
public class StatusBar extends JPanel implements Runnable, MouseListener, ActivePanelListener, TableSelectionListener, LocationListener, ComponentListener, ThemeListener {

    private MainFrame mainFrame;

    /** Label that displays info about current selected file(s) */
    private JLabel selectedFilesLabel;
	
    /** Label that displays info about current volume (free/total space) */
    private VolumeSpaceLabel volumeSpaceLabel;

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
	
    /** SizeFormat's format used to display volume info in status bar */
    private final static int VOLUME_INFO_SIZE_FORMAT = SizeFormat.DIGITS_SHORT| SizeFormat.UNIT_SHORT| SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB;

    /** Icon that is displayed when folder is changing */
    public final static String WAITING_ICON = "waiting.png";


    /**
     * Creates a new StatusBar instance.
     */
    public StatusBar(MainFrame mainFrame) {
        // Create and add status bar
        super(new BorderLayout());

        this.mainFrame = mainFrame;
		
        this.selectedFilesLabel = new JLabel("");
        add(selectedFilesLabel, BorderLayout.CENTER);

        this.volumeSpaceLabel = new VolumeSpaceLabel();
        add(volumeSpaceLabel, BorderLayout.EAST);

        // Show/hide this status bar based on user preferences
        // Note: setVisible has to be called even with true for the auto-update thread to be initialized
        setVisible(ConfigurationManager.getVariableBoolean(ConfigurationVariables.STATUS_BAR_VISIBLE, ConfigurationVariables.DEFAULT_STATUS_BAR_VISIBLE));
        
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
        selectedFilesLabel.addMouseListener(this);
        volumeSpaceLabel.addMouseListener(this);
        addMouseListener(this);
		
        // Catch component events to be notified when this component is made visible
        // and update status info
        addComponentListener(this);

        // Initialises theme.
        selectedFilesLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR));
        selectedFilesLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_TEXT));
        volumeSpaceLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR));
        volumeSpaceLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_TEXT));
        ThemeManager.addThemeListener(this);
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

        FileTable currentFileTable = mainFrame.getActiveTable();

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

        boolean compactFileSize = ConfigurationManager.getVariableBoolean(ConfigurationVariables.DISPLAY_COMPACT_FILE_SIZE,
                                                                          ConfigurationVariables.DEFAULT_DISPLAY_COMPACT_FILE_SIZE);
        String filesInfo;
		
        if(fileCount==0) {
            // Set status bar to a space character, not an empty string
            // otherwise it will disappear
            filesInfo = " ";
        }
        else {
            filesInfo = Translator.get("status_bar.selected_files", ""+nbSelectedFiles, ""+fileCount);
			
            if(nbMarkedFiles>0)
                filesInfo += " - "+ SizeFormat.format(markedTotalSize, (compactFileSize? SizeFormat.DIGITS_SHORT: SizeFormat.DIGITS_FULL)|(compactFileSize? SizeFormat.UNIT_SHORT: SizeFormat.UNIT_LONG)| SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB);
	
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

        final AbstractFile currentFolder = mainFrame.getActiveTable().getCurrentFolder();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, currentFolder="+currentFolder);

        long cachedVolumeInfo[] = (long[])volumeInfoCache.get(currentFolder.getAbsolutePath());
        if(cachedVolumeInfo!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Cache hit!");
            volumeSpaceLabel.setVolumeSpace(cachedVolumeInfo[0], cachedVolumeInfo[1]);
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

                    // Folder is a local file and Java version is 1.5 or lower: call getVolumeInfo() instead of 
                    // separate calls to getFreeSpace() and getTotalSpace() as it is twice as fast.
                    if(currentFolder instanceof LocalFile && PlatformManager.JAVA_VERSION<=PlatformManager.JAVA_1_5) {
                        long volumeInfo[] = ((LocalFile)currentFolder).getVolumeInfo();
                        volumeTotal = volumeInfo[0];
                        volumeFree = volumeInfo[1];
                    }
                    // Java 1.6 and up or any other file type
                    else {
                        volumeFree = currentFolder.getFreeSpace();
                        volumeTotal = currentFolder.getTotalSpace();
                    }

                    volumeSpaceLabel.setVolumeSpace(volumeTotal, volumeFree);

                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Adding to cache");
                    volumeInfoCache.add(currentFolder.getAbsolutePath(), new long[]{volumeTotal, volumeFree}, VOLUME_INFO_TIME_TO_LIVE);
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
        selectedFilesLabel.setText(text);
        selectedFilesLabel.setIcon(icon);
        if(icon!=null)
            selectedFilesLabel.setHorizontalTextPosition(iconBeforeText?JLabel.TRAILING:JLabel.LEADING);
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
        if(source==mainFrame.getActiveTable())
            updateSelectedFilesInfo();
    }

    public void markedFilesChanged(FileTable source) {
        // No need to update if the originating FileTable is not the currently active one
        if(source==mainFrame.getActiveTable())
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

    public void locationFailed(LocationEvent e) {
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
        if (PlatformManager.isRightMouseButton(e)) {
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


    public void fontChanged(int fontId, Font newFont) {
        if(fontId == Theme.STATUS_BAR) {
            selectedFilesLabel.setFont(newFont);
            volumeSpaceLabel.setFont(newFont);
            repaint();
        }
    }

    public void colorChanged(int colorId, Color newColor) {
        if(colorId == Theme.STATUS_BAR_TEXT) {
            selectedFilesLabel.setForeground(newColor);
            volumeSpaceLabel.setForeground(newColor);
            repaint();
        }
    }

    /**
     * This label displays the amount of free and/or total space on a volume.
     */
    private static class VolumeSpaceLabel extends JLabel implements ThemeListener {

        private long freeSpace;
        private long totalSpace;

        private Color backgroundColor;
        private Color borderColor;
        private Color okColor;
        private Color warningColor;
        private Color criticalColor;

        private final static float SPACE_WARNING_THRESHOLD = 0.1f;
        private final static float SPACE_CRITICAL_THRESHOLD = 0.01f;


        private VolumeSpaceLabel() {
            super("");
            setHorizontalAlignment(CENTER);
            backgroundColor = ThemeManager.getCurrentColor(Theme.STATUS_BAR_BACKGROUND);
            borderColor     = ThemeManager.getCurrentColor(Theme.STATUS_BAR_BORDER);
            okColor         = ThemeManager.getCurrentColor(Theme.STATUS_BAR_OK);
            warningColor    = ThemeManager.getCurrentColor(Theme.STATUS_BAR_WARNING);
            criticalColor   = ThemeManager.getCurrentColor(Theme.STATUS_BAR_CRITICAL);
            ThemeManager.addThemeListener(this);
        }

        /**
         * Sets the new volume total and free space, and updates the label's text to show the new values and,
         * only if both total and free space are available (different from -1), paint a graphical representation
         * of the amount of free space available and set a tooltip showing the percentage of free space on the volume.
         *
         * @param totalSpace total volume space, -1 if not available
         * @param freeSpace free volume space, -1 if not available
         */
        private void setVolumeSpace(long totalSpace, long freeSpace) {
            this.freeSpace = freeSpace;
            this.totalSpace = totalSpace;

            // Set new label's text
            String volumeInfo;
            if(freeSpace!=-1) {
                volumeInfo = SizeFormat.format(freeSpace, VOLUME_INFO_SIZE_FORMAT);
                if(totalSpace!=-1)
                    volumeInfo += " / "+ SizeFormat.format(totalSpace, VOLUME_INFO_SIZE_FORMAT);

                volumeInfo = Translator.get("status_bar.volume_free", volumeInfo);
            }
            else if(totalSpace!=-1) {
                volumeInfo = SizeFormat.format(totalSpace, VOLUME_INFO_SIZE_FORMAT);
                volumeInfo = Translator.get("status_bar.volume_capacity", volumeInfo);
            }
            else {
                volumeInfo = "";
            }
            setText(volumeInfo);

            // Set tooltip
            if(freeSpace==-1 || totalSpace==-1)
                setToolTipText(null);       // Removes any previous tooltip
            else
                setToolTipText(""+(int)(100*freeSpace/(float)totalSpace)+"%");

            repaint();
        }


        /**
         * Adds some empty space around the label.
         */
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width+4, d.height+2);
        }

//        public Insets getInsets() {
//            return new Insets(2, 0, 2, 0);
//        }
//

        public void paint(Graphics g) {

            // If free or total space is not available, this label will just be painted as a normal JLabel
            if(freeSpace!=-1 && totalSpace!=-1) {
                int width = getWidth();
                int height = getHeight();

                // Fill background
                g.setColor(backgroundColor);
                g.fillRect(0, 0, width, height);

                // Paint border
                g.setColor(borderColor);
                g.drawRect(0, 0, width-1, height-1);

                // Paint amount of free volume space if both free and total space are available
                float freeSpacePercentage = freeSpace/(float)totalSpace;

                g.setColor(freeSpacePercentage<=SPACE_CRITICAL_THRESHOLD?criticalColor
                           :freeSpacePercentage<=SPACE_WARNING_THRESHOLD?warningColor
                           :okColor);

                int freeSpaceWidth = Math.max(Math.round(freeSpacePercentage*(float)(width-2)), 1);
                g.fillRect(1, 1, freeSpaceWidth, height-2);
            }

            super.paint(g);
        }

        public void fontChanged(int fontId, Font newFont) {}

        public void colorChanged(int colorId, Color newColor) {
            if(colorId == Theme.STATUS_BAR_BACKGROUND)
                backgroundColor = newColor;
            else if(colorId == Theme.STATUS_BAR_BORDER)
                borderColor = newColor;
            else if(colorId == Theme.STATUS_BAR_OK)
                okColor = newColor;
            else if(colorId == Theme.STATUS_BAR_WARNING)
                warningColor = newColor;
            else if(colorId == Theme.STATUS_BAR_CRITICAL)
                criticalColor = newColor;
            else
                return;
            repaint();
        }
    }
}

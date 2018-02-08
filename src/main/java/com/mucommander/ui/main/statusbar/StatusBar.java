/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

package com.mucommander.ui.main.statusbar;

import com.mucommander.cache.FastLRUCache;
import com.mucommander.cache.LRUCache;
import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;
import com.mucommander.ui.theme.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * StatusBar is the component that sits at the bottom of each MainFrame, between the folder panels and command bar.
 * There is one and only one StatusBar per MainFrame, created by the associated MainFrame. It can be hidden,
 * but the instance will always remain, until the MainFrame is disposed.
 * <p>
 * <p>StatusBar is used to display info about the total/selected number of files in the current folder and current volume's
 * free/total space. When a folder is being changed, a waiting message is displayed. When quick search is being used,
 * the current quick search string is displayed.
 * <p>
 * <p>StatusBar receives LocationListener events when the folder has or is being changed, and automatically updates
 * selected files and volume info, and display the waiting message when the folder is changing. Quick search info
 * is set by FileTable.QuickSearch.
 * <p>
 * <p>When StatusBar is visible, a Thread runs in the background to periodically update free/total space volume info.
 * This thread stops when the StatusBar is hidden.
 *
 * @author Maxence Bernard
 */
public class StatusBar extends JPanel implements Runnable, MouseListener, ActivePanelListener, TableSelectionListener, LocationListener, ComponentListener, ThemeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusBar.class);

    /**
     * Listens to configuration changes and updates static fields accordingly
     */
    private static final ConfigurationListener CONFIGURATION_ADAPTER;

    /**
     * Number of volume info strings that can be temporarily cached
     */
    private static final int VOLUME_INFO_CACHE_CAPACITY = 50;

    /**
     * Number of milliseconds before cached volume info strings expire
     */
    private static final int VOLUME_INFO_TIME_TO_LIVE = 60000;

    /**
     * Number of milliseconds between each volume info update by auto-update thread
     */
    private static final int AUTO_UPDATE_PERIOD = 6000;

    static {
        // Initialize the size column format based on the configuration
        setSelectedFileSizeFormat(MuConfigurations.getPreferences().getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE, MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));

        // Listens to configuration changes and updates static fields accordingly.
        // Note: a reference to the listener must be kept to prevent it from being garbage-collected.
        CONFIGURATION_ADAPTER = new ConfigurationListener() {
            @Override
            public synchronized void configurationChanged(ConfigurationEvent event) {
                String var = event.getVariable();
                if (var.equals(MuPreferences.DISPLAY_COMPACT_FILE_SIZE))
                    setSelectedFileSizeFormat(event.getBooleanValue());
            }
        };
        MuConfigurations.addPreferencesListener(CONFIGURATION_ADAPTER);
    }

    /**
     * Caches volume info strings (free/total space) for a while, since this information is expensive to retrieve
     * (I/O bound). This map uses folders' volume path as its key.
     */
    private static LRUCache<String, Long[]> volumeInfoCache = new FastLRUCache<>(VOLUME_INFO_CACHE_CAPACITY);

    /**
     * SizeFormat format used to create the selected file(s) size string
     */
    private static int selectedFileSizeFormat;

    private final MainFrame mainFrame;

    /**
     * Label that displays info about current selected file(s)
     */
    private final JLabel selectedFilesLabel;

    /**
     * Icon used while loading is in progress.
     */
    private final SpinningDial dial;

    /**
     * Label that displays info about current volume (free/total space)
     */
    private final VolumeSpaceLabel volumeSpaceLabel;

    /**
     * Label that displays info about memory
     */
    private final MemoryLabel memoryLabel;

    /**
     * Thread which auto updates volume info
     */
    private Thread autoUpdateThread;

    /**
     * Creates a new StatusBar instance.
     */
    public StatusBar(MainFrame mainFrame) {
        // Create and add status bar
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.mainFrame = mainFrame;

        selectedFilesLabel = new JLabel("");
        dial = new SpinningDial();
        add(selectedFilesLabel);

        add(Box.createHorizontalGlue());

        final RolloverButtonAdapter rolloverButtonAdapter = new RolloverButtonAdapter();

        final JobsPopupButton jobsButton = new JobsPopupButton();
        jobsButton.addMouseListener(rolloverButtonAdapter);
        add(jobsButton);
        add(Box.createRigidArea(new Dimension(2, 0)));


        final PerformanceMonitorButton performanceMonitorButton = new PerformanceMonitorButton(mainFrame);
        performanceMonitorButton.addMouseListener(rolloverButtonAdapter);
        add(performanceMonitorButton);
        add(Box.createRigidArea(new Dimension(2, 0)));

        memoryLabel = new MemoryLabel();
        add(memoryLabel);

        // Add a button for interacting with the trash, only if the current platform has a trash implementation
        if (DesktopManager.getTrash() != null) {
            final TrashPopupButton trashButton = new TrashPopupButton(mainFrame);
            trashButton.addMouseListener(rolloverButtonAdapter);
            add(trashButton);
            add(Box.createRigidArea(new Dimension(2, 0)));
        }

        volumeSpaceLabel = new VolumeSpaceLabel();
        add(volumeSpaceLabel);

        // Show/hide this status bar based on user preferences
        // Note: setVisible has to be called even with true for the auto-update thread to be initialized
        setVisible(MuConfigurations.getPreferences().getVariable(MuPreference.STATUS_BAR_VISIBLE, MuPreferences.DEFAULT_STATUS_BAR_VISIBLE));

        // Catch location events to update status bar info when folder is changed
        final FolderPanel leftPanel = mainFrame.getLeftPanel();
        leftPanel.getLocationManager().addLocationListener(this);

        final FolderPanel rightPanel = mainFrame.getRightPanel();
        rightPanel.getLocationManager().addLocationListener(this);

        // Catch table selection change events to update the selected files info when the selected files have changed on
        // one of the file tables
        leftPanel.getFileTable().addTableSelectionListener(this);
        rightPanel.getFileTable().addTableSelectionListener(this);

        // Catch active panel change events to update status bar info when current table has changed
        mainFrame.addActivePanelListener(this);

        // Catch mouse events to pop up a menu on right-click
        selectedFilesLabel.addMouseListener(this);
        volumeSpaceLabel.addMouseListener(this);
        memoryLabel.addMouseListener(this);
        addMouseListener(this);

        // Catch component events to be notified when this component is made visible
        // and update status info
        addComponentListener(this);

        // Initialises theme.
        selectedFilesLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR_FONT));
        selectedFilesLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_FOREGROUND_COLOR));
        volumeSpaceLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR_FONT));
        volumeSpaceLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_FOREGROUND_COLOR));
        memoryLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR_FONT));
        memoryLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_FOREGROUND_COLOR));
        ThemeManager.addCurrentThemeListener(this);
    }

    /**
     * Sets the SizeFormat format used to create the selected file(s) size string.
     *
     * @param compactSize true to use a compact size format, false for full size in bytes
     */
    private static void setSelectedFileSizeFormat(boolean compactSize) {
        if (compactSize)
            selectedFileSizeFormat = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT | SizeFormat.ROUND_TO_KB;
        else
            selectedFileSizeFormat = SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG;

        selectedFileSizeFormat |= SizeFormat.INCLUDE_SPACE;
    }

    /**
     * Updates info displayed on the status bar: currently selected files and volume info.
     */
    private void updateStatusInfo() {
        // No need to waste precious cycles if status bar is not visible
        if (!isVisible())
            return;

        updateSelectedFilesInfo();
        updateVolumeInfo();
        updateMemoryInfo();
    }

    /**
     * Updates info about currently selected files ((nb of selected files, combined size), displayed on the left-side of this status bar.
     */
    public void updateSelectedFilesInfo() {
        // No need to waste precious cycles if status bar is not visible
        if (!isVisible())
            return;

        FileTable currentFileTable = mainFrame.getActiveTable();

        // Currently select file, can be null
        AbstractFile selectedFile = currentFileTable.getSelectedFile(false, true);
        FileTableModel tableModel = currentFileTable.getFileTableModel();
        // Number of marked files, can be 0
        int nbMarkedFiles = tableModel.getNbMarkedFiles();
        // Combined size of marked files, 0 if no file has been marked
        long markedTotalSize = tableModel.getTotalMarkedSize();
        // number of files in folder
        int fileCount = tableModel.getFileCount();

        // Update files info based on marked files if there are some, or currently selected file otherwise
        int nbSelectedFiles;
        if (nbMarkedFiles == 0 && selectedFile != null)
            nbSelectedFiles = 1;
        else
            nbSelectedFiles = nbMarkedFiles;

        String filesInfo;

        if (fileCount == 0) {
            // Set status bar to a space character, not an empty string
            // otherwise it will disappear
            filesInfo = " ";
        } else {
            filesInfo = Translator.get("status_bar.selected_files", "" + nbSelectedFiles, "" + fileCount);

            if (nbMarkedFiles > 0)
                filesInfo += " - " + SizeFormat.format(markedTotalSize, selectedFileSizeFormat);

            if (selectedFile != null)
                filesInfo += " - " + selectedFile.getName();
        }

        // Update label
        setStatusInfo(filesInfo);
    }

    /**
     * Updates info about current volume (free space, total space), displayed on the right-side of this status bar.
     */
    private synchronized void updateVolumeInfo() {
        // No need to waste precious cycles if status bar is not visible
        if (!isVisible())
            return;

        final AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        // Resolve the current folder's volume and use its path as a key for the volume info cache
        final String volumePath = currentFolder.exists() ?
                currentFolder.getVolume().getAbsolutePath(true) : "";

        Long cachedVolumeInfo[] = volumeInfoCache.get(volumePath);
        if (cachedVolumeInfo != null) {
            LOGGER.debug("Cache hit!");
            volumeSpaceLabel.setVolumeSpace(cachedVolumeInfo[0], cachedVolumeInfo[1]);
        } else {
            // Retrieves free and total volume space.
            // Perform volume info retrieval in a separate thread as this method may be called
            // by the event thread and it can take a while, we want to return as soon as possible
            new Thread("StatusBar.updateVolumeInfo") {
                @Override
                public void run() {
                    // Free space on current volume, -1 if this information is not available 
                    long volumeFree;
                    // Total space on current volume, -1 if this information is not available 
                    long volumeTotal;

                    // Folder is a local file and Java version is 1.5: call getVolumeInfo() instead of
                    // separate calls to getFreeSpace() and getTotalSpace() as it is twice as fast.
                    if (currentFolder instanceof LocalFile && JavaVersion.JAVA_1_5.isCurrentOrLower()) {
                        try {
                            long volumeInfo[] = ((LocalFile) currentFolder).getVolumeInfo();
                            volumeTotal = volumeInfo[0];
                            volumeFree = volumeInfo[1];
                        } catch (IOException e) {
                            volumeTotal = -1;
                            volumeFree = -1;
                        }
                    }
                    // Java 1.6 and up or any other file type
                    else {
                        try {
                            volumeFree = currentFolder.getFreeSpace();
                        } catch (IOException e) {
                            volumeFree = -1;
                        }

                        try {
                            volumeTotal = currentFolder.getTotalSpace();
                        } catch (IOException e) {
                            volumeTotal = -1;
                        }
                    }

                    volumeSpaceLabel.setVolumeSpace(volumeTotal, volumeFree);

                    LOGGER.debug("Adding to cache");
                    volumeInfoCache.add(volumePath, new Long[]{volumeTotal, volumeFree}, VOLUME_INFO_TIME_TO_LIVE);
                }
            }.start();
        }
    }

    /**
     * Updates info about memory (used , total), displayed on the right-side of this status bar.
     */
    private void updateMemoryInfo() {
        final Runtime runtime = Runtime.getRuntime();
        memoryLabel.setMemory(runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory());
    }

    /**
     * Displays the specified text and icon on the left-side of the status bar,
     * replacing any previous information.
     *
     * @param text           the piece of text to display
     * @param icon           the icon to display next to the text
     * @param iconBeforeText if true, icon will be placed on the left side of the text, if not on the right side
     */
    public void setStatusInfo(String text, Icon icon, boolean iconBeforeText) {
        selectedFilesLabel.setText(text);

        if (icon == null) {
            // What we don't want here is the label's height to change depending on whether it has an icon or not.
            // This would result in having to revalidate the status bar and in turn the whole MainFrame.
            // A label's height is roughly the max of the text's font height and the icon (if any). So if there is no
            // icon for the label, we use a transparent image for padding in case the text's font height is smaller
            // than a 'standard' (16x16) icon. This ensures that the label's height remains constant.
            BufferedImage bi = new BufferedImage(1, 16, BufferedImage.TYPE_INT_ARGB);
            icon = new ImageIcon(bi);
        }
        selectedFilesLabel.setIcon(icon);

        selectedFilesLabel.setHorizontalTextPosition(iconBeforeText ? JLabel.TRAILING : JLabel.LEADING);

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
        if (autoUpdateThread == null) {
            // Start volume info auto-update thread
            autoUpdateThread = new Thread(this, "StatusBar autoUpdateThread");
            // Set the thread as a daemon thread
            autoUpdateThread.setDaemon(true);
            autoUpdateThread.start();
        }
    }

    /**
     * Overrides JComponent.setVisible(boolean) to start/stop volume info auto-update thread.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // Start auto-update thread
            startAutoUpdate();
            super.setVisible(true);
            // Update status bar info
            updateStatusInfo();
        } else {
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
    @Override
    public void run() {
        do {
            // Sleep for a while
            try {
                Thread.sleep(AUTO_UPDATE_PERIOD);
            } catch (InterruptedException ignored) {

            }

            // Update volume info if:
            // - status bar is visible
            // - MainFrame isn't changing folders
            // - MainFrame is active and in the foreground
            // Volume info update will potentially hit the LRU cache and not actually update volume info
            if (isVisible() && !mainFrame.getNoEventsMode() && mainFrame.isForegroundActive()) {
                updateVolumeInfo();
                updateMemoryInfo();
            }
        }
        while (autoUpdateThread != null && mainFrame.isVisible());   // Stop when MainFrame is disposed
    }

    ////////////////////////////////////////
    // ActivePanelListener implementation //
    ////////////////////////////////////////
    @Override
    public void activePanelChanged(FolderPanel folderPanel) {
        updateStatusInfo();
    }

    ///////////////////////////////////////////
    // TableSelectionListener implementation //
    ///////////////////////////////////////////
    @Override
    public void selectedFileChanged(FileTable source) {
        // No need to update if the originating FileTable is not the currently active one
        if (source == mainFrame.getActiveTable() && mainFrame.isForegroundActive())
            updateSelectedFilesInfo();
    }

    @Override
    public void markedFilesChanged(FileTable source) {
        // No need to update if the originating FileTable is not the currently active one
        if (source == mainFrame.getActiveTable() && mainFrame.isForegroundActive())
            updateSelectedFilesInfo();
    }

    /////////////////////////////////////
    // LocationListener implementation //
    /////////////////////////////////////
    @Override
    public void locationChanged(LocationEvent e) {
        dial.setAnimated(false);
        updateStatusInfo();
    }

    @Override
    public void locationChanging(LocationEvent e) {
        // Show a message in the status bar saying that folder is being changed
        setStatusInfo(Translator.get("status_bar.connecting_to_folder"), dial, true);
        dial.setAnimated(true);
    }

    @Override
    public void locationCancelled(LocationEvent e) {
        dial.setAnimated(false);
        updateStatusInfo();
    }

    @Override
    public void locationFailed(LocationEvent e) {
        dial.setAnimated(false);
        updateStatusInfo();
    }

    //////////////////////////////////
    // MouseListener implementation //
    //////////////////////////////////
    @Override
    public void mouseClicked(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if (mainFrame.getNoEventsMode())
            return;

        // Right clicking on the toolbar brings up a popup menu that allows the user to hide this status bar
        if (DesktopManager.isRightMouseButton(e)) {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.impl.ToggleStatusBarAction.Descriptor.ACTION_ID, mainFrame));
            popupMenu.show(this, e.getX(), e.getY());
            popupMenu.setVisible(true);
            return;
        }

        if (DesktopManager.isLeftMouseButton(e) && memoryLabel.equals(e.getSource())) {
            System.gc();
            updateMemoryInfo();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    //////////////////////////////////////
    // ComponentListener implementation //
    //////////////////////////////////////
    @Override
    public void componentShown(ComponentEvent e) {
        // Invoked when the component has been made visible (apparently not called when just created)
        // Status bar needs to be updated sihce it is not updated when not visible
        updateStatusInfo();
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void fontChanged(FontChangedEvent event) {
        if (event.getFontId() == Theme.STATUS_BAR_FONT) {
            selectedFilesLabel.setFont(event.getFont());
            volumeSpaceLabel.setFont(event.getFont());
            memoryLabel.setFont(event.getFont());
            repaint();
        }
    }

    @Override
    public void colorChanged(ColorChangedEvent event) {
        if (event.getColorId() == Theme.STATUS_BAR_FOREGROUND_COLOR) {
            selectedFilesLabel.setForeground(event.getColor());
            volumeSpaceLabel.setForeground(event.getColor());
            memoryLabel.setForeground(event.getColor());
            repaint();
        }
    }

}

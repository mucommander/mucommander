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

package com.mucommander.ui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.cache.FastLRUCache;
import com.mucommander.commons.util.cache.LRUCache;
import com.mucommander.commons.util.ui.border.MutableLineBorder;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;


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
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusBar.class);
	
    private MainFrame mainFrame;

    /** Label that displays info about current selected file(s) */
    private JLabel selectedFilesLabel;

    /** Icon used while loading is in progress. */
    private SpinningDial dial;
	
    /** Label that displays info about current volume (free/total space) */
    private VolumeSpaceLabel volumeSpaceLabel;

    /** Thread which auto updates volume info */
    private Thread autoUpdateThread;

    /** Number of milliseconds between each volume info update by auto-update thread */
    private final static int AUTO_UPDATE_PERIOD = 60000;

    /** Icon that is displayed when folder is changing */
    public final static String WAITING_ICON = "waiting.png";

    /** SizeFormat's format used to display volume info in status bar */
    private final static int VOLUME_INFO_SIZE_FORMAT = SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT| SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB;

    /** Listens to configuration changes and updates static fields accordingly */
    public final static ConfigurationListener CONFIGURATION_ADAPTER;

    /** SizeFormat format used to create the selected file(s) size string */
    private static int selectedFileSizeFormat;

    /** Indicates whether the main frame that holds this status bar has been disposed */
    private boolean mainFrameDisposed;

    /** Indicates whether {@link #autoUpdateThread} has been notified */
    private boolean autoUpdateThreadNotified;

    /** Holds the path of the volume for which free/total space was last retrieved by {@link #autoUpdateThread} */
    private String volumePath;

    static {
        // Initialize the size column format based on the configuration
        setSelectedFileSizeFormat(MuConfigurations.getPreferences().getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE,
                                                  MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));

        // Listens to configuration changes and updates static fields accordingly.
        // Note: a reference to the listener must be kept to prevent it from being garbage-collected.
        CONFIGURATION_ADAPTER = new ConfigurationListener() {
            public synchronized void configurationChanged(ConfigurationEvent event) {
                String var = event.getVariable();

                if (var.equals(MuPreferences.DISPLAY_COMPACT_FILE_SIZE))
                    setSelectedFileSizeFormat(event.getBooleanValue());
            }
        };
        MuConfigurations.addPreferencesListener(CONFIGURATION_ADAPTER);
    }


    /**
     * Sets the SizeFormat format used to create the selected file(s) size string.
     *
     * @param compactSize true to use a compact size format, false for full size in bytes
     */
    private static void setSelectedFileSizeFormat(boolean compactSize) {
        if(compactSize)
            selectedFileSizeFormat = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT | SizeFormat.ROUND_TO_KB;
        else
            selectedFileSizeFormat = SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG;

        selectedFileSizeFormat |= SizeFormat.INCLUDE_SPACE;
    }


    /**
     * Creates a new StatusBar instance.
     */
    public StatusBar(MainFrame mainFrame) {
        // Create and add status bar
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.mainFrame = mainFrame;
		
        selectedFilesLabel = new JLabel("");
        dial               = new SpinningDial();
        add(selectedFilesLabel);

        add(Box.createHorizontalGlue());

        JobsPopupButton jobsButton = new JobsPopupButton();
        jobsButton.setPopupMenuLocation(SwingConstants.TOP);

        add(jobsButton);
        add(Box.createRigidArea(new Dimension(2, 0)));

        // Add a button for interacting with the trash, only if the current platform has a trash implementation
        if(DesktopManager.getTrash()!=null) {
            TrashPopupButton trashButton = new TrashPopupButton(mainFrame);
            trashButton.setPopupMenuLocation(SwingConstants.TOP);

            add(trashButton);
            add(Box.createRigidArea(new Dimension(2, 0)));
        }

        volumeSpaceLabel = new VolumeSpaceLabel();
        add(volumeSpaceLabel);

        // Show/hide this status bar based on user preferences
        // Note: setVisible has to be called even with true for the auto-update thread to be initialized
        setVisible(MuConfigurations.getPreferences().getVariable(MuPreference.STATUS_BAR_VISIBLE, MuPreferences.DEFAULT_STATUS_BAR_VISIBLE));
        
        // Catch location events to update status bar info when folder is changed
        FolderPanel leftPanel = mainFrame.getLeftPanel();
        leftPanel.getLocationManager().addLocationListener(this);

        FolderPanel rightPanel = mainFrame.getRightPanel();
        rightPanel.getLocationManager().addLocationListener(this);

        // Catch table selection change events to update the selected files info when the selected files have changed on
        // one of the file tables
        leftPanel.getFileTable().addTableSelectionListener(this);
        rightPanel.getFileTable().addTableSelectionListener(this);

        // Catch active panel change events to update status bar info when current table has changed
        mainFrame.addActivePanelListener(this);

        // Catch main frame close events to make sure autoUpdateThread is finished
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                mainFrameDisposed = true;
                triggerVolumeInfoUpdate();
            }
        });

        // Catch window gained focus events to update the volume info when current windows has changed
        mainFrame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                triggerVolumeInfoUpdate();
            }
        });
		
        // Catch mouse events to pop up a menu on right-click
        selectedFilesLabel.addMouseListener(this);
        volumeSpaceLabel.addMouseListener(this);
        addMouseListener(this);
		
        // Catch component events to be notified when this component is made visible
        // and update status info
        addComponentListener(this);

        // Initialises theme.
        selectedFilesLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR_FONT));
        selectedFilesLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_FOREGROUND_COLOR));
        volumeSpaceLabel.setFont(ThemeManager.getCurrentFont(Theme.STATUS_BAR_FONT));
        volumeSpaceLabel.setForeground(ThemeManager.getCurrentColor(Theme.STATUS_BAR_FOREGROUND_COLOR));
        ThemeManager.addCurrentThemeListener(this);
    }


    /**
     * Updates info displayed on the status bar: currently selected files and volume info.
     */
    private void updateStatusInfo() {
        // No need to waste precious cycles if status bar is not visible
        if (!isVisible())
            return;

        updateSelectedFilesInfo();
        if (isVolumeChanged())
            triggerVolumeInfoUpdate();
    }

    /**
     * Updates info about currently selected files ((nb of selected files, combined size), displayed on the left-side of this status bar.
     */
// Making this method synchronized creates a deadlock with FileTable
//    public synchronized void updateSelectedFilesInfo() {
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
        if(nbMarkedFiles==0 && selectedFile!=null)
            nbSelectedFiles = 1;
        else
            nbSelectedFiles = nbMarkedFiles;

        String filesInfo;
		
        if(fileCount==0) {
            // Set status bar to a space character, not an empty string
            // otherwise it will disappear
            filesInfo = " ";
        }
        else {
            filesInfo = Translator.get("status_bar.selected_files", ""+nbSelectedFiles, ""+fileCount);
			
            if(nbMarkedFiles>0)
                filesInfo += " - " + SizeFormat.format(markedTotalSize, selectedFileSizeFormat);
	
            if(selectedFile!=null)
                filesInfo += " - "+selectedFile.getName();
        }		

        // Update label
        setStatusInfo(filesInfo);
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

        if(icon==null) {
            // What we don't want here is the label's height to change depending on whether it has an icon or not.
            // This would result in having to revalidate the status bar and in turn the whole MainFrame.
            // A label's height is roughly the max of the text's font height and the icon (if any). So if there is no
            // icon for the label, we use a transparent image for padding in case the text's font height is smaller
            // than a 'standard' (16x16) icon. This ensures that the label's height remains constant.
            BufferedImage bi = new BufferedImage(1, 16, BufferedImage.TYPE_INT_ARGB);
            icon = new ImageIcon(bi);
        }
        selectedFilesLabel.setIcon(icon);

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
        if (autoUpdateThread==null) {
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
        super.setVisible(visible);
        if (visible) {
            // Start auto-update thread
            startAutoUpdate();
            // Update status bar info
            updateStatusInfo();
        }
    }
    
    
    //////////////////////
    // Runnable methods //
    //////////////////////

    /**
     * Periodically updates volume info (free / total space).
     */
    public void run() {
        while (!mainFrameDisposed) { // Stop when MainFrame is disposed
            // Update volume info if:
            // - status bar is visible
            // - MainFrame is active and in the foreground
            // Volume info update will potentially hit the LRU cache and not actually update volume info
            if (isVisible() && mainFrame.isForegroundActive()) {
                final AbstractFile currentFolder = getCurrentFolder();
                volumePath = getVolumePath(currentFolder);

                // Retrieves free and total volume space.
                long volumeFree = getFreeSpace(currentFolder);
                long volumeTotal = getTotalSpace(currentFolder);

                volumeSpaceLabel.setVolumeSpace(volumeTotal, volumeFree);
            }

            // Sleep for a while
            sleep();
        }
    }

    private AbstractFile getCurrentFolder() {
        return mainFrame.getActivePanel().getCurrentFolder();
    }

    private String getVolumePath(AbstractFile folder) {
        return folder.exists() ? folder.getVolume().getAbsolutePath(true) : "";
    }

    private boolean isVolumeChanged() {
        return volumePath == null || !volumePath.equals(getVolumePath(getCurrentFolder()));
    }

    private void sleep() {
        if (!autoUpdateThreadNotified) {
            synchronized(autoUpdateThread) {
                if (!autoUpdateThreadNotified) {
                    try { autoUpdateThread.wait(AUTO_UPDATE_PERIOD); }
                    catch (InterruptedException e) {}
                }
            }
        }
        autoUpdateThreadNotified = false;
    }

    private void triggerVolumeInfoUpdate() {
        if (!autoUpdateThreadNotified) {
            synchronized(autoUpdateThread) {
                if (!autoUpdateThreadNotified) {
                    autoUpdateThreadNotified = true;
                    autoUpdateThread.notify();
                }
            }
        }
    }

    /**
     * @return Free space on current volume, -1 if this information is not available
     */
    private long getFreeSpace(AbstractFile currentFolder) {
        try { return currentFolder.getFreeSpace(); }
        catch(IOException e) { return -1; }
    }

    /**
     * @return Total space on current volume, -1 if this information is not available
     */
    private long getTotalSpace(AbstractFile currentFolder) {
        try { return currentFolder.getTotalSpace(); }
        catch(IOException e) { return -1; }
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
        if(source==mainFrame.getActiveTable() && mainFrame.isForegroundActive())
            updateSelectedFilesInfo();
    }

    public void markedFilesChanged(FileTable source) {
        // No need to update if the originating FileTable is not the currently active one
        if(source==mainFrame.getActiveTable() && mainFrame.isForegroundActive())
            updateSelectedFilesInfo();
    }


    /////////////////////////////////////
    // LocationListener implementation //
    /////////////////////////////////////

    public void locationChanged(LocationEvent e) {
        dial.setAnimated(false);
        updateStatusInfo();
    }

    public void locationChanging(LocationEvent e) {
        // Show a message in the status bar saying that folder is being changed
        setStatusInfo(Translator.get("status_bar.connecting_to_folder"), dial, true);
        dial.setAnimated(true);
    }
	
    public void locationCancelled(LocationEvent e) {
        dial.setAnimated(false);
        updateStatusInfo();
    }

    public void locationFailed(LocationEvent e) {
        dial.setAnimated(false);
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
        if (DesktopManager.isRightMouseButton(e)) {
            //		if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.impl.ToggleStatusBarAction.Descriptor.ACTION_ID, mainFrame));
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
        // Status bar needs to be updated since it is not updated when not visible
        updateStatusInfo();
    }     

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }


    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.STATUS_BAR_FONT) {
            selectedFilesLabel.setFont(event.getFont());
            volumeSpaceLabel.setFont(event.getFont());
            repaint();
        }
    }

    public void colorChanged(ColorChangedEvent event) {
        if(event.getColorId() == Theme.STATUS_BAR_FOREGROUND_COLOR) {
            selectedFilesLabel.setForeground(event.getColor());
            volumeSpaceLabel.setForeground(event.getColor());
            repaint();
        }
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * This label displays the amount of free and/or total space on a volume.
     */
    private static class VolumeSpaceLabel extends JLabel implements ThemeListener {

        private long freeSpace;
        private long totalSpace;

        private Color backgroundColor;
        private Color okColor;
        private Color warningColor;
        private Color criticalColor;

        private final static float SPACE_WARNING_THRESHOLD = 0.1f;
        private final static float SPACE_CRITICAL_THRESHOLD = 0.05f;


        private VolumeSpaceLabel() {
            super("");
            setHorizontalAlignment(CENTER);
            backgroundColor = ThemeManager.getCurrentColor(Theme.STATUS_BAR_BACKGROUND_COLOR);
            //            borderColor     = ThemeManager.getCurrentColor(Theme.STATUS_BAR_BORDER_COLOR);
            okColor         = ThemeManager.getCurrentColor(Theme.STATUS_BAR_OK_COLOR);
            warningColor    = ThemeManager.getCurrentColor(Theme.STATUS_BAR_WARNING_COLOR);
            criticalColor   = ThemeManager.getCurrentColor(Theme.STATUS_BAR_CRITICAL_COLOR);
            setBorder(new MutableLineBorder(ThemeManager.getCurrentColor(Theme.STATUS_BAR_BORDER_COLOR)));
            ThemeManager.addCurrentThemeListener(this);
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
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width+4, d.height+2);
        }

        /**
         * Returns an interpolated color value, located at percent between c1 and c2 in the RGB space.
         *
         * @param c1 first color
         * @param c2 end color
         * @param percent distance between c1 and c2, comprised between 0 and 1.
         * @return an interpolated color value, located at percent between c1 and c2 in the RGB space.
         */
        private Color interpolateColor(Color c1, Color c2, float percent) {
            return new Color(
                    (int)(c1.getRed()+(c2.getRed()-c1.getRed())*percent),
                    (int)(c1.getGreen()+(c2.getGreen()-c1.getGreen())*percent),
                    (int)(c1.getBlue()+(c2.getBlue()-c1.getBlue())*percent)
            );
        }

        @Override
        public void paint(Graphics g) {

            // If free or total space is not available, this label will just be painted as a normal JLabel
            if(freeSpace!=-1 && totalSpace!=-1) {
                int width = getWidth();
                int height = getHeight();

                // Paint amount of free volume space if both free and total space are available
                float freeSpacePercentage = freeSpace/(float)totalSpace;

                Color c;
                if(freeSpacePercentage<=SPACE_CRITICAL_THRESHOLD) {
                    c = criticalColor;
                }
                else if(freeSpacePercentage<=SPACE_WARNING_THRESHOLD) {
                    c = interpolateColor(warningColor, criticalColor, (SPACE_WARNING_THRESHOLD-freeSpacePercentage)/SPACE_WARNING_THRESHOLD);
                }
                else {
                    c = interpolateColor(okColor, warningColor, (1-freeSpacePercentage)/(1-SPACE_WARNING_THRESHOLD));
                }

                g.setColor(c);

                int freeSpaceWidth = Math.max(Math.round(freeSpacePercentage*(float)(width-2)), 1);
                g.fillRect(1, 1, freeSpaceWidth + 1, height - 2);

                // Fill background
                g.setColor(backgroundColor);
                g.fillRect(freeSpaceWidth + 1, 1, width - freeSpaceWidth - 1, height - 2);
            }

            super.paint(g);
        }


// Total/Free space reversed, doesn't look quite right

//        @Override
//        public void paint(Graphics g) {
//            // If free or total space is not available, this label will just be painted as a normal JLabel
//            if(freeSpace!=-1 && totalSpace!=-1) {
//                int width = getWidth();
//                int height = getHeight();
//
//                // Paint amount of free volume space if both free and total space are available
//                float freeSpacePercentage = freeSpace/(float)totalSpace;
//                float usedSpacePercentage = (totalSpace-freeSpace)/(float)totalSpace;
//
//                Color c;
//                if(freeSpacePercentage<=SPACE_CRITICAL_THRESHOLD) {
//                    c = criticalColor;
//                }
//                else if(freeSpacePercentage<=SPACE_WARNING_THRESHOLD) {
//                    c = interpolateColor(warningColor, criticalColor, (SPACE_WARNING_THRESHOLD-freeSpacePercentage)/SPACE_WARNING_THRESHOLD);
//                }
//                else {
//                    c = interpolateColor(okColor, warningColor, (1-freeSpacePercentage)/(1-SPACE_WARNING_THRESHOLD));
//                }
//
//                g.setColor(c);
//
//                int usedSpaceWidth = Math.max(Math.round(usedSpacePercentage*(float)(width-2)), 1);
//                g.fillRect(1, 1, usedSpaceWidth + 1, height - 2);
//
//                // Fill background
//                g.setColor(backgroundColor);
//                g.fillRect(usedSpaceWidth + 1, 1, width - usedSpaceWidth - 1, height - 2);
//            }
//
//            super.paint(g);
//        }

        public void fontChanged(FontChangedEvent event) {}

        public void colorChanged(ColorChangedEvent event) {
            switch(event.getColorId()) {
            case Theme.STATUS_BAR_BACKGROUND_COLOR:
                backgroundColor = event.getColor();
                break;
            case Theme.STATUS_BAR_BORDER_COLOR:
                // Some (rather evil) look and feels will change borders outside of muCommander's control,
                // this check is necessary to ensure no exception is thrown.
                if(getBorder() instanceof MutableLineBorder)
                    ((MutableLineBorder)getBorder()).setLineColor(event.getColor());
                break;
            case Theme.STATUS_BAR_OK_COLOR:
                okColor = event.getColor();
                break;
            case Theme.STATUS_BAR_WARNING_COLOR:
                warningColor = event.getColor();
                break;
            case Theme.STATUS_BAR_CRITICAL_COLOR:
                criticalColor = event.getColor();
                break;
            default:
                return;
            }
            repaint();
        }
    }
}

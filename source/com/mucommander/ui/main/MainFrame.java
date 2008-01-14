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

package com.mucommander.ui.main;

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntryFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.CloseWindowAction;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.ProportionalSplitPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.menu.MainMenuBar;
import com.mucommander.ui.main.table.Columns;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableConfiguration;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * This is the main frame, which contains all other UI components visible on a mucommander window.
 * 
 * @author Maxence Bernard
 */
public class MainFrame extends JFrame implements LocationListener {
	
    private ProportionalSplitPane splitPane;

    private FolderPanel folderPanel1;
    private FolderPanel folderPanel2;
	
    private FileTable table1;
    private FileTable table2;
    
    /** Active table in the MainFrame */
    private FileTable activeTable;

    /** Tool bar instance */
    private ToolBar toolbar;

    /** Status bar instance */
    private StatusBar statusBar;
	
    /** Command bar instance */
    private CommandBar commandBar;
	
    /** Is no events mode enabled ? */
    private boolean noEventsMode;

    /** Is this MainFrame active in the foreground ? */
    private boolean foregroundActive;

    /** Contains all registered ActivePanelListener instances, stored as weak references */
    private WeakHashMap activePanelListeners = new WeakHashMap();

    /** Split pane orientation */
    private final static String SPLIT_ORIENTATION = MuConfiguration.SPLIT_ORIENTATION;

    private void init(FolderPanel panel1, FolderPanel panel2) {
        // Set frame icon fetched in an image inside the JAR file
        setIconImage(IconManager.getIcon("/icon16.gif").getImage());

        // Enable window resize
        setResizable(true);

        // Sets the content pane.
        JPanel contentPane = new JPanel(new BorderLayout()) {
                // Add an x=3,y=3 gap around content pane
                public Insets getInsets() {
                    return new Insets(3, 3, 3, 3);
                }
            };
        setContentPane(contentPane);

        // Initialises the folder panels and file tables.
        folderPanel1 = panel1;
        folderPanel2 = panel2;
        table1       = folderPanel1.getFileTable();
        table2       = folderPanel2.getFileTable();
        activeTable  = table1;


        // Create toolbar and show it only if it hasn't been disabled in the preferences
        // Note: Toolbar.setVisible() has to be called no matter if Toolbar is visible or not, in order for it to be properly initialized
        this.toolbar = new ToolBar(this);
        this.toolbar.setVisible(MuConfiguration.getVariable(MuConfiguration.TOOLBAR_VISIBLE, MuConfiguration.DEFAULT_TOOLBAR_VISIBLE));
        contentPane.add(toolbar, BorderLayout.NORTH);

        // Lister to location change events to display the current folder in the window's title
        folderPanel1.getLocationManager().addLocationListener(this);
        folderPanel2.getLocationManager().addLocationListener(this);

        // Create menu bar (has to be created after toolbar)
        MainMenuBar menuBar = new MainMenuBar(this);
        setJMenuBar(menuBar);

        // Create the split pane that separates folder panels and allows to resize how much space is allocated to the
        // both of them. The split orientation is loaded from and saved to the preferences.
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing.
        splitPane = new ProportionalSplitPane(this,
            MuConfiguration.getVariable(SPLIT_ORIENTATION, MuConfiguration.DEFAULT_SPLIT_ORIENTATION).equals(MuConfiguration.VERTICAL_SPLIT_ORIENTATION) ?
                                              JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT,
                                              false,
                                              folderPanel1,
                                              folderPanel2) {
                // We don't want any extra space around split pane
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
            };

        // Remove any default border the split pane has
        splitPane.setBorder(null);

        // Adds buttons that allow to collapse and expand the split pane in both directions
        splitPane.setOneTouchExpandable(true);

        // Disable all the JSPlitPane accessibility shortcuts that are registered by default, as some of them
        // conflict with default mucommander action shortcuts (e.g. F6 and F8) 
        splitPane.disableAccessibilityShortcuts();

        // Split pane will be given any extra space
        contentPane.add(splitPane, BorderLayout.CENTER);

        // Add a 2-pixel gap between the file table and status bar
        YBoxPanel southPanel = new YBoxPanel();
        southPanel.addSpace(2);

        // Add status bar
        this.statusBar = new StatusBar(this);
        southPanel.add(statusBar);
		
        // Show command bar only if it hasn't been disabled in the preferences
        this.commandBar = new CommandBar(this);
        // Note: CommandBar.setVisible() has to be called no matter if CommandBar is visible or not, in order for it to be properly initialized
        this.commandBar.setVisible(MuConfiguration.getVariable(MuConfiguration.COMMAND_BAR_VISIBLE, MuConfiguration.DEFAULT_COMMAND_BAR_VISIBLE));
        southPanel.add(commandBar);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        // Perform CloseAction when the user asked the window to close
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ActionManager.performAction(CloseWindowAction.class, MainFrame.this);
            }
        });

        ActionKeymap.registerActions(this);

        // Fire table change events on registered ActivePanelListener instances, to notify of the intial active table.
        fireActivePanelChanged(activeTable.getFolderPanel());

        // Set the custom FocusTraversalPolicy that manages focus for both FolderPanel and their subcomponents.
        setFocusTraversalPolicy(new CustomFocusTraversalPolicy());
    }

    private MainFrame() {
    }

    private FileTableConfiguration getFileTableConfiguration(boolean isLeft) {
        FileTableConfiguration conf;

        conf = new FileTableConfiguration();

        // Loop on columns
        for(int c=0; c<Columns.COLUMN_COUNT; c++) {
            if(c!=Columns.NAME) {       // Skip the special name column (always visible, width automatically calculated)
                // Sets the column's initial visibility.
                conf.setVisible(c,
                    MuConfiguration.getVariable(
                            MuConfiguration.getShowColumnVariable(c, isLeft),
                            MuConfiguration.getShowColumnDefault(c)
                    )
                );

                // Sets the column's initial width.
                conf.setWidth(c, MuConfiguration.getIntegerVariable(MuConfiguration.getColumnWidthVariable(c, isLeft)));
            }

            // Sets the column's initial order
            conf.setPosition(c,
                    MuConfiguration.getVariable(
                            MuConfiguration.getColumnPositionVariable(c, isLeft),
                            c
                    )
            );
        }

        return conf;
    }

    /**
     * Creates a new main frame, set to the given initial folders.
     */
    public MainFrame(AbstractFile initialFolder1, AbstractFile initialFolder2) {
        init(new FolderPanel(this, initialFolder1, getFileTableConfiguration(true)), new FolderPanel(this, initialFolder2, getFileTableConfiguration(false)));

        table1.sortBy(columnNameToIndex(MuConfiguration.getVariable(MuConfiguration.LEFT_SORT_BY, MuConfiguration.DEFAULT_SORT_BY)),
                      !MuConfiguration.getVariable(MuConfiguration.LEFT_SORT_ORDER, MuConfiguration.DEFAULT_SORT_ORDER).equals(MuConfiguration.SORT_ORDER_DESCENDING));
        table2.sortBy(columnNameToIndex(MuConfiguration.getVariable(MuConfiguration.RIGHT_SORT_BY, MuConfiguration.DEFAULT_SORT_BY)),
                      !MuConfiguration.getVariable(MuConfiguration.RIGHT_SORT_ORDER, MuConfiguration.DEFAULT_SORT_ORDER).equals(MuConfiguration.SORT_ORDER_DESCENDING));
    }

    /**
     * Returns the index of the column designated by the given name.
     *
     * @param column the name of a column, see {@link com.mucommander.ui.main.table.Columns#getColumnName(int)} for possible values
     * @return the index of the column, see {@link com.mucommander.ui.main.table.Columns} for possible values
     */
    private static int columnNameToIndex(String column) {
        for(int c=0; c<Columns.COLUMN_COUNT; c++)
            if(Columns.getColumnName(c).equals(column))
                return c;

        return columnNameToIndex(MuConfiguration.DEFAULT_SORT_BY);
    }


    MainFrame cloneMainFrame() {
        MainFrame mainFrame;

        mainFrame = new MainFrame();
        mainFrame.init(new FolderPanel(mainFrame, folderPanel1.getCurrentFolder(), table1.getConfiguration()),
                       new FolderPanel(mainFrame, folderPanel2.getCurrentFolder(), table2.getConfiguration()));
        mainFrame.table1.sortBy(table1.getSortByCriteria(), table1.isSortAscending());
        mainFrame.table2.sortBy(table2.getSortByCriteria(), table2.isSortAscending());
        return mainFrame;
    }

    /**
     * Registers the given ActivePanelListener to receive events when current table changes.
     */
    public void addActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.put(activePanelListener, null);
    }

    /**
     * Unregisters the given ActivePanelListener so that it will no longer receive events when current table changes.
     */
    public void removeActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.remove(activePanelListener);
    }

    /**
     * Fires table change events on registered ActivePanelListener instances.
     */
    private void fireActivePanelChanged(FolderPanel folderPanel) {
        Iterator iterator = activePanelListeners.keySet().iterator();
        while(iterator.hasNext())
            ((ActivePanelListener)iterator.next()).activePanelChanged(folderPanel);
    }


    /**
     * Returns true if 'no events mode' is enabled.
     */
    public boolean getNoEventsMode() {
        return this.noEventsMode;
    }
	
    /**
     * Enables/disables 'no events mode' which prevents mouse and keyboard events from being received
     * by the application (main frame and its subcomponents and menus).
     */
    public void setNoEventsMode(boolean enabled) {
        // Piece of code used in 0.8 beta1 and removed after because it's way too slow, kept here for the record 
        //		// Glass pane has empty mouse and key adapters (created in the constructor)
        //		// which will catch all mouse and keyboard events 
        //		getGlassPane().setVisible(enabled);
        //		getJMenuBar().setEnabled(!enabled);
        //		// Remove focus from whatever component in FolderPanel which had focus
        //		getGlassPane().requestFocus();

        this.noEventsMode = enabled;
    }


    /**
     * Returns the toolbar where shortcut buttons (go back, go forward, ...) are.
     */
    public ToolBar getToolBar() {
        return toolbar;
    }


    /**
     * Returns the command bar, i.e. the panel that contains
     * F3, F6... F10 buttons.
     */
    public CommandBar getCommandBar() {
        return commandBar;
    }


    /**
     * Returns the status bar where information about selected files and volume are displayed.
     */
    public StatusBar getStatusBar() {
        return this.statusBar;
    }


    /**
     * Returns the active FileTable in this MainFrame.
     *
     * The returned active table doesn't necessarily have focus, the focus can be in some other component
     * of the active {@link FolderPanel}, or nowhere in the MainFrame if the window is not in the foreground.
     *
     * <p>Use {@link FileTable#hasFocus()} to test if the table currently has focus.
     *
     * @see FileTable#isActiveTable()
     */
    public FileTable getActiveTable() {
        return activeTable;
    }

    public FileTable getLeftTable() {
        return table1;
    }

    public FileTable getRightTable() {
        return table2;
    }

    /**
     * Sets currently active FileTable (called by FolderPanel).
     */
    void setActiveTable(FileTable table) {
        boolean activeTableChanged = activeTable !=table;

        if(activeTableChanged) {
            this.activeTable = table;

            // Update window title to reflect new active table
            updateWindowTitle();

            // Fire table change events on registered ActivePanelListener instances.
            fireActivePanelChanged(table.getFolderPanel());
        }
    }

	
    /**
     * Returns the complement to getActiveTable().
     */
    public FileTable getInactiveTable() {
        return activeTable ==table1?table2:table1;
    }
    
    /**
     * Returns left FolderPanel.
     */
    public FolderPanel getFolderPanel1() {
        return folderPanel1;
    }

    /**
     * Returns right FolderPanel.
     */
    public FolderPanel getFolderPanel2() {
        return folderPanel2;
    }


    /**
     * Returns the {@link ProportionalSplitPane} component that manages how the two {@link FolderPanel} are split.   
     */
    public ProportionalSplitPane getSplitPane() {
        return splitPane;
    }

    /**
     * Specifies how folder panels are split: if true is passed, the folder panels will be split vertically
     * (default), horizontally otherwise.
     *
     * @param vertical if true, the folder panels will be split horizontally (default), vertically otherwise.
     */
    public void setSplitPaneOrientation(boolean vertical) {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        splitPane.setOrientation(vertical?JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT);
        // Save current split pane orientation to preferences
        saveSplitPaneOrientation();
    }

    /**
     * Returns how folder panels are currently split: if true is passed, the folder panels are split vertically (default),
     * horizontally otherwise.
     */
    public boolean getSplitPaneOrientation() {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        return splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
    }


    /**
     * Save current split pane orientation to preferences.
     */
    private void saveSplitPaneOrientation() {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        MuConfiguration.setVariable(SPLIT_ORIENTATION, splitPane.getOrientation()==JSplitPane.HORIZONTAL_SPLIT?MuConfiguration.VERTICAL_SPLIT_ORIENTATION:MuConfiguration.HORIZONTAL_SPLIT_ORIENTATION);
    }

    /**
     * Swaps the two FolderPanel instances: after a call to this method, folderPanel1 will be folderPanels2 and vice-versa.
     */
    public void swapFolders() {
        splitPane.remove(folderPanel1);
        splitPane.remove(folderPanel2);

        // Swaps the folder panels.
        FolderPanel tempPanel = folderPanel1;
        folderPanel1 = folderPanel2;
        folderPanel2 = tempPanel;

        // Resets the tables.
        FileTable tempTable = table1;
        table1 = table2;
        table2 = tempTable;

        // Makes sure the table models are properly applied.
        TableColumnModel model = table1.getColumnModel();
        table1.setColumnModel(table2.getColumnModel());
        table2.setColumnModel(model);

        // Makes sure the sort order is respected.
        boolean ascending;
        int     criteria;
        criteria  = table1.getSortByCriteria();
        ascending = table1.isSortAscending();
        table1.sortBy(table2.getSortByCriteria(), table2.isSortAscending());
        table2.sortBy(criteria, ascending);

        splitPane.setLeftComponent(folderPanel1);
        splitPane.setRightComponent(folderPanel2);

        splitPane.doLayout();

        // Update split pane divider's location
        splitPane.updateDividerLocation();

        activeTable.requestFocus();
    }


    /**
     * Makes both folders the same, choosing the one which is currently active. 
     */
    public void setSameFolder() {
        (activeTable ==table1?table2:table1).getFolderPanel().tryChangeCurrentFolder(activeTable.getCurrentFolder());
    }


    /**
     * Returns <code>true</code> if this MainFrame is active in the foreground.
     */
    public boolean isForegroundActive() {
        return foregroundActive;
    }

    /**
     * Sets whether this MainFrame is active in the foreground. Method to be called solely by WindowManager.
     */
    void setForegroundActive(boolean foregroundActive) {
        this.foregroundActive = foregroundActive;
    }

    /**
     * Forces a refrehs of the frame's folder panel.
     */
    public void tryRefreshCurrentFolders() {
        folderPanel1.tryRefreshCurrentFolder();
        folderPanel2.tryRefreshCurrentFolder();
    }


    /**
     * Returns <code>true</code> if this MainFrame is active, or is an ancestor of a Window that is currently active.
     */
    public boolean isAncestorOfActiveWindow() {
        if(isActive())
            return true;

        Window ownedWindows[] = getOwnedWindows();

        int nbWindows = ownedWindows.length;
        for(int i=0; i<nbWindows; i++)
            if(ownedWindows[i].isActive())
                return true;

        return false;
    }


    /**
     * Updates this window's title to show currently active folder and window number.
     * This method is called by this class and WindowManager.
     */
    public void updateWindowTitle() {
        // Update window title
        String title = activeTable.getCurrentFolder().getAbsolutePath();
        Vector mainFrames = WindowManager.getMainFrames();
        if(mainFrames.size()>1)
            title += " ["+(mainFrames.indexOf(this)+1)+"]";
        setTitle(title);

        // Use new Window decorations introduced in Mac OS X 10.5 (Leopard) with Java 1.5 and up
        if(OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher()) {
            // Displays the document icon in the window title bar, works only for local files
            AbstractFile currentFolder = activeTable.getCurrentFolder();
            Object javaIoFile;
            if(currentFolder.getURL().getProtocol().equals(FileProtocols.FILE)) {
                // If the current folder is an archive entry, display the archive file, this is the closest we can get
                // with a java.io.File
                if(currentFolder.hasAncestor(ArchiveEntryFile.class))
                    javaIoFile = currentFolder.getParentArchive().getUnderlyingFileObject();
                else
                    javaIoFile = currentFolder.getUnderlyingFileObject();
            }
            else {
                // If the current folder is not a local file, use the special /Network directory which is sort of
                // 'Network Neighborhood'.
                javaIoFile = new java.io.File("/Network");
            }

            // Note that for some strange reason (looks like a bug), setting the property to null won't remove
            // the previous icon.
            getRootPane().putClientProperty("Window.documentFile", javaIoFile);
        }
    }
    

    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        // Update window title to reflect the new current folder
        updateWindowTitle();
    }

    public void locationChanging(LocationEvent e) {
    }
	
    public void locationCancelled(LocationEvent e) {
    }

    public void locationFailed(LocationEvent e) {
    }


    ///////////////////////
    // Overriden methods //
    ///////////////////////

    /**
     * Overrides <code>java.awt.Window#dispose</code> to save last MainFrame's attributes in the preferences
     * before disposing this MainFrame.
     */
    public void dispose() {
        // Save last MainFrame's attributes (last folders, window position) in the preferences.

        // Save last folders
        MuConfiguration.setVariable(MuConfiguration.LAST_LEFT_FOLDER, 
                                         getFolderPanel1().getFolderHistory().getLastRecallableFolder());
        MuConfiguration.setVariable(MuConfiguration.LAST_RIGHT_FOLDER, 
                                         getFolderPanel2().getFolderHistory().getLastRecallableFolder());

        // Save window position, size and screen resolution
        Rectangle bounds = getBounds();
        MuConfiguration.setVariable(MuConfiguration.LAST_X, (int)bounds.getX());
        MuConfiguration.setVariable(MuConfiguration.LAST_Y, (int)bounds.getY());
        MuConfiguration.setVariable(MuConfiguration.LAST_WIDTH, (int)bounds.getWidth());
        MuConfiguration.setVariable(MuConfiguration.LAST_HEIGHT, (int)bounds.getHeight());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        MuConfiguration.setVariable(MuConfiguration.SCREEN_WIDTH, screenSize.width);
        MuConfiguration.setVariable(MuConfiguration.SCREEN_HEIGHT, screenSize.height);

        // Saves left and right table positions.
        for(boolean isLeft=true; ; isLeft=false) {
            FileTable table = isLeft?table1:table2;
            // Loop on columns
            for(int c=0; c<Columns.COLUMN_COUNT; c++) {
                if(c!=Columns.NAME) {       // Skip the special name column (always visible, width automatically calculated)
                    MuConfiguration.setVariable(
                        MuConfiguration.getShowColumnVariable(c, isLeft),
                        table.isColumnVisible(c)
                    );

                    MuConfiguration.setVariable(
                        MuConfiguration.getColumnWidthVariable(c, isLeft),
                        table.getColumnWidth(c)
                    );
                }

                MuConfiguration.setVariable(
                    MuConfiguration.getColumnPositionVariable(c, isLeft),
                    table.getColumnPosition(c)
                );
            }

            if(!isLeft)
                break;
        }

        // Saves left and right table sort order.
        MuConfiguration.setVariable(MuConfiguration.LEFT_SORT_BY, Columns.getColumnName(table1.getSortByCriteria()));
        MuConfiguration.setVariable(MuConfiguration.LEFT_SORT_ORDER, table1.isSortAscending() ? MuConfiguration.SORT_ORDER_ASCENDING : MuConfiguration.SORT_ORDER_DESCENDING);
        MuConfiguration.setVariable(MuConfiguration.RIGHT_SORT_BY, Columns.getColumnName(table2.getSortByCriteria()));
        MuConfiguration.setVariable(MuConfiguration.RIGHT_SORT_ORDER, table2.isSortAscending() ? MuConfiguration.SORT_ORDER_ASCENDING : MuConfiguration.SORT_ORDER_DESCENDING);

        // Save split pane orientation
        saveSplitPaneOrientation();

        // Finally, dispose the frame
        super.dispose(); 
    }


    /**
     * Overrides <code>java.awt.Window#toFront</code> to have the window return to a normal state if it is minimized.
     */
    public void toFront() {
        if((getExtendedState()&Frame.ICONIFIED)!=0)
            setExtendedState(Frame.NORMAL);
        super.toFront();
    }



    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Manages focus for both FolderPanel and their subcomponents.
     *
     * @author Maxence Bernard
     */
    protected class CustomFocusTraversalPolicy extends FocusTraversalPolicy {

        public Component getComponentAfter(Container container, Component component) {
            if(component==folderPanel1.getLocationComboBox().getTextField() || component==folderPanel1.getLocationComboBox())
                return table1;
            else if(component==table1)
                return table2;
            if(component==folderPanel2.getLocationComboBox().getTextField() || component==folderPanel2.getLocationComboBox())
                return table2;
            else    // component==table2
                return table1;
        }

        public Component getComponentBefore(Container container, Component component) {
            // Completly symetrical with getComponentAfter
            return getComponentAfter(container, component);
       }

        public Component getFirstComponent(Container container) {
            return table1;
        }

        public Component getLastComponent(Container container) {
            return table2;
        }

        public Component getDefaultComponent(Container container) {
            return getActiveTable();
        }
    }

    public boolean isAutoSizeColumnsEnabled() {
        return table1.isAutoSizeColumnsEnabled();
    }

    public void setAutoSizeColumnsEnabled(boolean b) {
        table1.setAutoSizeColumnsEnabled(b);
        table2.setAutoSizeColumnsEnabled(b);
    }
}

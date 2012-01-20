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

package com.mucommander.conf;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;

import javax.swing.JSplitPane;

import com.mucommander.AppLogger;
import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.conf.ConfigurationException;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.tabs.FileTableTab;

/**
 * 
 * @author Arik Hadas
 */
public class MuSnapshot {

	// - Startup folder variables --------------------------------------------
    // -----------------------------------------------------------------------
	/** Name for variables that describe the last visited folder of a panel. */
    public static final String  LAST_FOLDER                       = "last_folder";
    /** Last visited folder in the left panel. */
    public static final String  LAST_LEFT_FOLDER                  = MuPreferences.LEFT_STARTUP_FOLDER_SECTION + '.' + LAST_FOLDER;
    /** Last visited folder in the right panel. */
    public static final String  LAST_RIGHT_FOLDER                 = MuPreferences.RIGHT_STARTUP_FOLDER_SECTION + '.' + LAST_FOLDER;
    
    // - Last window variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing known information about the last muCommander window. */
    private static final String  LAST_WINDOW_SECTION                = "last_window";
    /** Last muCommander known x position. */
    public static final String  LAST_X                             = LAST_WINDOW_SECTION + '.' + "x";
    /** Last muCommander known y position. */
    public static final String  LAST_Y                             = LAST_WINDOW_SECTION + '.' + "y";
    /** Last muCommander known width. */
    public static final String  LAST_WIDTH                         = LAST_WINDOW_SECTION + '.' + "width";
    /** Last muCommander known height. */
    public static final String  LAST_HEIGHT                        = LAST_WINDOW_SECTION + '.' + "height";
    /** Last known screen width. */
    public static final String  SCREEN_WIDTH                       = LAST_WINDOW_SECTION + '.' + "screen_width";
    /** Last known screen height. */
    public static final String  SCREEN_HEIGHT                      = LAST_WINDOW_SECTION + '.' + "screen_height";
    /** Last orientation used to split folder panels. */
    public static final String  SPLIT_ORIENTATION                  = LAST_WINDOW_SECTION + '.' + "split_orientation";
    /** Vertical split pane orientation. */
    public static final String VERTICAL_SPLIT_ORIENTATION         = "vertical";
    /** Horizontal split pane orientation. */
    public static final String HORIZONTAL_SPLIT_ORIENTATION       = "horizontal";
    /** Default split pane orientation. */
    public static final String DEFAULT_SPLIT_ORIENTATION          = VERTICAL_SPLIT_ORIENTATION;
    
    // - Last panels variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Identifier of the left panel. */
    private static final String  LEFT                              = "left";
    /** Identifier of the right panel. */
    private static final String  RIGHT                             = "right";
    /** TODO: complete */
    private static final String  PANEL                             = "panel";
    /** Subsection describing the tree view CONFIGURATION. */
    private static final String  TREE                              = "tree";
    public static final String   TREE_VISIBLE                      = "visible";
    public static final String   TREE_WIDTH                        = "width";
    /** Subsection describing the folders view CONFIGURATION. */
    private static final String  FILE_TABLE                        = "file_table";
    /** Describes an ascending sort order. */
    public static final String  SORT_ORDER_ASCENDING               = "asc";
    /** Describes a descending sort order. */
    public static final String  SORT_ORDER_DESCENDING              = "desc";
    /** Default 'sort order' column for the file table. */
    public static final String  DEFAULT_SORT_ORDER                 = SORT_ORDER_ASCENDING;
    /** Name of the 'show column' variable. */
    public static final String  SHOW_COLUMN                        = "show";
    /** Name of the 'column position' variable. */
    public static final String  COLUMN_POSITION                    = "position";
    /** Name of the 'column width' variable. */
    public static final String  COLUMN_WIDTH                       = "width";
    /** Default 'sort by' column for the file table. */
    public static final String  DEFAULT_SORT_BY                    = "name";
    /** Identifier of the sort section in a file table's CONFIGURATION. */
    public static final String  SORT                               = "sort";
    /** Identifier of the sort criteria in a file table's CONFIGURATION. */
    public static final String  SORT_BY                            = "by";
    /** Identifier of the sort order in a file table's CONFIGURATION. */
    public static final String  SORT_ORDER                         = "order";
    /** Subsection describing the tabs CONFIGURATION. */
    private static final String  TABS                              = "tabs";
    /** TODO: complete */
    private static final String  TAB                               = "tab";
    /** TODO: complete */
    private static final String  TABS_COUNT                        = "count";
    /** TODO: complete */
    private static final String  TAB_LOCATION                      = "location";
    
	/** Cache the screen's size. this value isn't computed during the shutdown process since it cause a deadlock then */
	private Dimension screenSize;
	
	/**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.FolderPanel},
     * left or right one.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FolderPanel
     */
	private static String getFolderPanelSection(boolean left) {
        return PANEL + "." + (left?LEFT:RIGHT);
    }
	
	private static String getTreeSection(boolean left) {
    	return getFolderPanelSection(left) + "." + TREE;
    }
    
    public static String getTreeVisiblity(boolean left) {
    	return getTreeSection(left) + "." + TREE_VISIBLE;
    }
    
    public static String getTreeWidth(boolean left) {
    	return getTreeSection(left) + "." + TREE_WIDTH;
    }
	
    /**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.table.FileTable},
     * left or right one.
     *
     * @param left true for the left FileTable, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTable
     */
    private static String getFileTableSection(boolean left) {
        return getFolderPanelSection(left) + "." + FILE_TABLE;
    }
    
    private static String getFileTableSortSection(boolean left) {
    	return getFileTableSection(left) + "." + SORT;
    }
    
    public static String getFileTableSortBy(boolean left) {
    	return getFileTableSortSection(left) + "." + SORT_BY;
    }
    
    public static String getFileTableSortOrder(boolean left) {
    	return getFileTableSortSection(left) + "." + SORT_ORDER;
    }

    /**
     * Returns the CONFIGURATION section corresponding to the specified column in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTable
     */
    private static String getColumnSection(Column column, boolean left) {
        return getFileTableSection(left) + "." + column.toString().toLowerCase();
    }

    /**
     * Returns the variable that controls the visibility of the specified column, in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the variable that controls the visibility of the specified column
     */
    public static String getShowColumnVariable(Column column, boolean left) {
        return getColumnSection(column, left) + "." + SHOW_COLUMN;
    }

    /**
     * Returns the variable that holds the width of the specified column, in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the variable that holds the width of the specified column
     */
    public static String getColumnWidthVariable(Column column, boolean left) {
        return getColumnSection(column, left) + "." + COLUMN_WIDTH;
    }
    
    /**
     * Returns the variable that holds the position of the specified column, in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the variable that holds the position of the specified column
     */
    public static String getColumnPositionVariable(Column column, boolean left) {
        return getColumnSection(column, left) + "." + COLUMN_POSITION;
    }
    
// Tabs
    
    private static String getTabsSection(boolean left) {
    	return getFolderPanelSection(left) + "." + TABS;
    }
    
    private static String getTabsCount(boolean left) {
    	return getTabsSection(left) + "." + TABS_COUNT;
    }
    
    private static String getTab(boolean left, int index) {
    	return getTabsSection(left) + "." + TAB + "-" + index; 
    }
    
    private static String getTabLocation(boolean left, int index) {
    	return getTab(left, index) + "." + TAB_LOCATION;
    }
    
	// - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    private final Configuration configuration;


    /**
     * Prevents instantiation of this class from outside of this package.
     */
    MuSnapshot() {
    	configuration = new Configuration(MuSnapshotFile.getSnapshotFile(), new VersionedXmlConfigurationReaderFactory(),
    			new VersionedXmlConfigurationWriterFactory());
		
		try {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		}
		catch(Exception e) {
			AppLogger.finer("Could not fetch screen size: " + e.getMessage());
		}
    }
    
    /**
     * TODO: change this method such that it will return a more specific API
     */
    Configuration getConfiguration() {
    	return configuration;
    }
    
 // - Configuration reading / writing -------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Loads the muCommander CONFIGURATION.
     * @throws IOException            if an I/O error occurs.
     * @throws ConfigurationException if a CONFIGURATION related error occurs.
     */
    void read() throws IOException, ConfigurationException {
        String configurationVersion;

        VersionedXmlConfigurationReader reader = new VersionedXmlConfigurationReader();
        try {
            configuration.read(reader);}
        finally {
        }
    }

    /**
     * Saves the muCommander CONFIGURATION.
     * @throws IOException            if an I/O error occurs.
     * @throws ConfigurationException if a CONFIGURATION related error occurs.
     */
    void write() throws IOException, ConfigurationException {
    	MainFrame currentMainFrame = WindowManager.getCurrentMainFrame();
    	
    	//Clear the configuration before saving to drop preferences which are unused anymore
    	configuration.clear();
    	
        // Save last folders
    	configuration.setVariable(MuSnapshot.LAST_LEFT_FOLDER, 
    			currentMainFrame.getLeftPanel().getFolderHistory().getLastRecallableFolder());
    	configuration.setVariable(MuSnapshot.LAST_RIGHT_FOLDER, 
    			currentMainFrame.getRightPanel().getFolderHistory().getLastRecallableFolder());

    	// Save window position, size and screen resolution
        setWindowAttributes(currentMainFrame);
        
        // Save left panel dynamic properties
        setPanelAttributes(true, currentMainFrame.getLeftPanel());

        // Save right panel dynamic properties
        setPanelAttributes(false, currentMainFrame.getRightPanel());

        configuration.write();
    }
    
    private void setPanelAttributes(boolean isLeft, FolderPanel panel) {
    	// Save tree folders preferences
        setTreeAttributes(isLeft, panel);
    	
        setTableAttributes(isLeft, panel.getFileTable());
        
        setTabsAttributes(isLeft, panel.getTabs().getClonedTabs());
    }

    private void setTabsAttributes(boolean isLeft, List<FileTableTab> tabs) {
    	// Save tabs count
    	configuration.setVariable(getTabsCount(isLeft), tabs.size());
    	// Save tabs locations
    	for(int i=0; i<tabs.size(); i++) {
    		FileTableTab tab = tabs.get(i);
    		configuration.setVariable(getTabLocation(isLeft, i), tab.getLocation().getAbsolutePath());
    	}
    }
    
    private void setTableAttributes(boolean isLeft, FileTable table) {
    	// Saves table sort order.
    	configuration.setVariable(MuSnapshot.getFileTableSortBy(isLeft), table.getSortInfo().getCriterion().toString().toLowerCase());
    	configuration.setVariable(MuSnapshot.getFileTableSortOrder(isLeft), table.getSortInfo().getAscendingOrder() ? MuSnapshot.SORT_ORDER_ASCENDING : MuSnapshot.SORT_ORDER_DESCENDING);
    	
    	// Loop on columns
		for(Column c : Column.values()) {
			if(c!=Column.NAME) {       // Skip the special name column (always enabled, width automatically calculated)
				MuConfigurations.getSnapshot().setVariable(
						MuSnapshot.getShowColumnVariable(c, isLeft),
						table.isColumnEnabled(c)
						);

				MuConfigurations.getSnapshot().setVariable(
						MuSnapshot.getColumnWidthVariable(c, isLeft),
						table.getColumnWidth(c)
						);
			}

			MuConfigurations.getSnapshot().setVariable(
					MuSnapshot.getColumnPositionVariable(c, isLeft),
					table.getColumnPosition(c)
					);
		}
    }
    
    private void setTreeAttributes(boolean isLeft, FolderPanel panel) {
    	configuration.setVariable(MuSnapshot.getTreeVisiblity(isLeft), panel.isTreeVisible());
        configuration.setVariable(MuSnapshot.getTreeWidth(isLeft), panel.getTreeWidth());
    }
    
    private void setWindowAttributes(MainFrame currentMainFrame) {
        Rectangle bounds = currentMainFrame.getBounds();
        configuration.setVariable(MuSnapshot.LAST_X, (int)bounds.getX());
        configuration.setVariable(MuSnapshot.LAST_Y, (int)bounds.getY());
        configuration.setVariable(MuSnapshot.LAST_WIDTH, (int)bounds.getWidth());
        configuration.setVariable(MuSnapshot.LAST_HEIGHT, (int)bounds.getHeight());
        
        if (screenSize != null) {
        	configuration.setVariable(MuSnapshot.SCREEN_WIDTH, screenSize.width);
        	configuration.setVariable(MuSnapshot.SCREEN_HEIGHT, screenSize.height);
        }

        // Save split pane orientation
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
    	configuration.setVariable(MuSnapshot.SPLIT_ORIENTATION, currentMainFrame.getSplitPane().getOrientation()==JSplitPane.HORIZONTAL_SPLIT?MuSnapshot.VERTICAL_SPLIT_ORIENTATION:MuSnapshot.HORIZONTAL_SPLIT_ORIENTATION);
    }
}

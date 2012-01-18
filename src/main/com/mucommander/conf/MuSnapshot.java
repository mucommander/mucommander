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

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.conf.ConfigurationException;
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
    public static final String  LAST_WINDOW_SECTION                = "last_window";
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
    
    // - Tree variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the tree CONFIGURATION. */
    public static final String   TREE_SECTION                      = "tree";
    public static final String   LEFT_TREE_VISIBLE                 = TREE_SECTION + "." + MuSnapshot.LEFT + "." + "visible";
    public static final String   RIGHT_TREE_VISIBLE                = TREE_SECTION + "." + MuSnapshot.RIGHT + "." + "visible";
    public static final String   LEFT_TREE_WIDTH                   = TREE_SECTION + "." + MuSnapshot.LEFT + "." + "width";
    public static final String   RIGHT_TREE_WIDTH                  = TREE_SECTION + "." + MuSnapshot.RIGHT + "." + "width";
    
    // - FileTable variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the folders view CONFIGURATION. */
    public static final String  FILE_TABLE_SECTION                 = "file_table";
    /** Identifier of the left file table. */
    public static final String  LEFT                               = "left";
    /** Identifier of the right file table. */
    public static final String  RIGHT                              = "right";
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
    /** Section describing the left table's CONFIGURATION. */
    public static final String  LEFT_FILE_TABLE_SECTION            = MuPreferences.FILE_TABLE_SECTION + '.' + LEFT;
    /** Section describing the right table's CONFIGURATION. */
    public static final String  RIGHT_FILE_TABLE_SECTION           = MuPreferences.FILE_TABLE_SECTION + '.' + RIGHT;
    /** Identifier of the sort section in a file table's CONFIGURATION. */
    public static final String  SORT                               = "sort";
    /** Identifier of the sort criteria in a file table's CONFIGURATION. */
    public static final String  SORT_BY                            = "by";
    /** Identifier of the sort order in a file table's CONFIGURATION. */
    public static final String  SORT_ORDER                         = "order";
    /** Section described the sort order of the right file table. */
    public static final String  RIGHT_FILE_TABLE_SORT_SECTION      = LEFT_FILE_TABLE_SECTION + '.' + SORT;
    /** Section described the sort order of the left file table. */
    public static final String  LEFT_FILE_TABLE_SORT_SECTION       = RIGHT_FILE_TABLE_SECTION + '.' + SORT;
    /** Controls the column on which the left file table should be sorted. */
    public static final String  LEFT_SORT_BY                       = LEFT_FILE_TABLE_SORT_SECTION + '.' + SORT_BY;
    /** Controls the column on which the right file table should be sorted. */
    public static final String  RIGHT_SORT_BY                      = RIGHT_FILE_TABLE_SORT_SECTION + '.' + SORT_BY;
    /** Controls the column on which the left file table should be sorted. */
    public static final String  LEFT_SORT_ORDER                    = LEFT_FILE_TABLE_SORT_SECTION + '.' + SORT_ORDER;
    /** Controls the column on which the right file table should be sorted. */
    public static final String  RIGHT_SORT_ORDER                   = RIGHT_FILE_TABLE_SORT_SECTION + '.' + SORT_ORDER;
	
	/** Cache the screen's size. this value isn't computed during the shutdown process since it cause a deadlock then */
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
    /**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.table.FileTable},
     * left or right one.
     *
     * @param left true for the left FileTable, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTable
     */
    private static String getFileTableSection(boolean left) {
        return FILE_TABLE_SECTION + "." + (left?LEFT:RIGHT);
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
    
	// - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    private final Configuration configuration;


    /**
     * Prevents instantiation of this class from outside of this package.
     */
    MuSnapshot() {
    	configuration = new Configuration(MuSnapshotFile.getSnapshotFile(), new VersionedXmlConfigurationReaderFactory(),
    			new VersionedXmlConfigurationWriterFactory());
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
    	
    	// Save current MainFrame's attributes (last folders, window position) in the preferences.

        // Save last folders
    	configuration.setVariable(MuSnapshot.LAST_LEFT_FOLDER, 
    			currentMainFrame.getLeftPanel().getFolderHistory().getLastRecallableFolder());
    	configuration.setVariable(MuSnapshot.LAST_RIGHT_FOLDER, 
    			currentMainFrame.getRightPanel().getFolderHistory().getLastRecallableFolder());

        // Save window position, size and screen resolution
        Rectangle bounds = currentMainFrame.getBounds();
        configuration.setVariable(MuSnapshot.LAST_X, (int)bounds.getX());
        configuration.setVariable(MuSnapshot.LAST_Y, (int)bounds.getY());
        configuration.setVariable(MuSnapshot.LAST_WIDTH, (int)bounds.getWidth());
        configuration.setVariable(MuSnapshot.LAST_HEIGHT, (int)bounds.getHeight());
        configuration.setVariable(MuSnapshot.SCREEN_WIDTH, screenSize.width);
        configuration.setVariable(MuSnapshot.SCREEN_HEIGHT, screenSize.height);
        
        // Save tree folders preferences
        configuration.setVariable(MuSnapshot.LEFT_TREE_VISIBLE, currentMainFrame.getLeftPanel().isTreeVisible());
        configuration.setVariable(MuSnapshot.RIGHT_TREE_VISIBLE, currentMainFrame.getRightPanel().isTreeVisible());
        configuration.setVariable(MuSnapshot.LEFT_TREE_WIDTH, currentMainFrame.getLeftPanel().getTreeWidth());
        configuration.setVariable(MuSnapshot.RIGHT_TREE_WIDTH, currentMainFrame.getRightPanel().getTreeWidth());
    	
        // Save split pane orientation
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
    	configuration.setVariable(MuSnapshot.SPLIT_ORIENTATION, currentMainFrame.getSplitPane().getOrientation()==JSplitPane.HORIZONTAL_SPLIT?MuSnapshot.VERTICAL_SPLIT_ORIENTATION:MuSnapshot.HORIZONTAL_SPLIT_ORIENTATION);
    	
    	// Saves left and right table sort order.
    	configuration.setVariable(MuSnapshot.LEFT_SORT_BY, currentMainFrame.getLeftPanel().getFileTable().getSortInfo().getCriterion().toString().toLowerCase());
    	configuration.setVariable(MuSnapshot.LEFT_SORT_ORDER, currentMainFrame.getLeftPanel().getFileTable().getSortInfo().getAscendingOrder() ? MuSnapshot.SORT_ORDER_ASCENDING : MuSnapshot.SORT_ORDER_DESCENDING);
    	configuration.setVariable(MuSnapshot.RIGHT_SORT_BY, currentMainFrame.getRightPanel().getFileTable().getSortInfo().getCriterion().toString().toLowerCase());
    	configuration.setVariable(MuSnapshot.RIGHT_SORT_ORDER, currentMainFrame.getRightPanel().getFileTable().getSortInfo().getAscendingOrder() ? MuSnapshot.SORT_ORDER_ASCENDING : MuSnapshot.SORT_ORDER_DESCENDING);
        
    	// Saves left and right table positions.
    	for(boolean isLeft=true; ; isLeft=false) {
    		FileTable table = isLeft? currentMainFrame.getLeftPanel().getFileTable() : currentMainFrame.getRightPanel().getFileTable();
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
    		
    		// Save tabs count
    		List<FileTableTab> tabsList = (isLeft ? currentMainFrame.getLeftPanel() : currentMainFrame.getRightPanel()).getTabs().getClonedTabs();
    		configuration.setVariable((isLeft ? LEFT_FILE_TABLE_SECTION : RIGHT_FILE_TABLE_SECTION) +"."+"tabs" + "." +"count", tabsList.size());
    		// Save tabs locations
    		for(int i=0; i<tabsList.size(); i++) {
    			FileTableTab tab = tabsList.get(i);
    			configuration.setVariable((isLeft ? LEFT_FILE_TABLE_SECTION : RIGHT_FILE_TABLE_SECTION) +"."+"tabs" + "." + "tab"+ i + "." + "location", tab.getLocation().getAbsolutePath());
    		}

    		if(!isLeft)
    			break;
    	}
    	
        configuration.write();
    }
}

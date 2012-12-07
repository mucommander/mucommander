/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.main.frame;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.conf.MuSnapshot;
import com.mucommander.ui.main.FolderPanel.FolderPanelType;
import com.mucommander.ui.main.MainFrame;

/**
 * 
 * @author Arik Hadas
 */
public class DefaultMainFramesBuilder extends MainFrameBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMainFramesBuilder.class);
	
	private Configuration snapshot = MuConfigurations.getSnapshot();
	
	public DefaultMainFramesBuilder() {
		
	}

	@Override
	public MainFrame[] build() {
		int nbFrames = snapshot.getIntegerVariable(MuSnapshot.getWindowsCount());

		// if there're no windows saved in the snapshot file, open one window with default settings
		if (nbFrames == 0) {
			MainFrame mainFrame = new MainFrame(getInitialPaths(FolderPanelType.LEFT, 0), getInitialPaths(FolderPanelType.RIGHT, 0),
					getInitialSelectedTab(FolderPanelType.LEFT, 0), getInitialSelectedTab(FolderPanelType.RIGHT, 0),
					  getInitialHistory(FolderPanelType.LEFT, 0), getInitialHistory(FolderPanelType.RIGHT, 0));
			
			Dimension screenSize   = Toolkit.getDefaultToolkit().getScreenSize();
	        // Full screen bounds are not reliable enough, in particular under Linux+Gnome
	        // so we simply make the initial window 4/5 of screen's size, and center it.
	        // This should fit under any window manager / platform
	        int x      = screenSize.width / 10;
	        int y      = screenSize.height / 10;
	        int width  = (int)(screenSize.width * 0.8);
	        int height = (int)(screenSize.height * 0.8);

	        mainFrame.setBounds(new Rectangle(x, y, width, height));

	        return new MainFrame[] {mainFrame};
		}
		else {
			MainFrame[] mainFrames = new MainFrame[nbFrames];
			for (int i=0; i<mainFrames.length; ++i)
				mainFrames[i] = createMainFrame(i);

			return mainFrames;
		}
	}

	private MainFrame createMainFrame(int index) {
		MainFrame mainFrame = new MainFrame(getInitialPaths(FolderPanelType.LEFT, index), getInitialPaths(FolderPanelType.RIGHT, index),
				getInitialSelectedTab(FolderPanelType.LEFT, index), getInitialSelectedTab(FolderPanelType.RIGHT, index),
				  getInitialHistory(FolderPanelType.LEFT, index), getInitialHistory(FolderPanelType.RIGHT, index));
		
		// Retrieve last saved window bounds
		Dimension screenSize   = Toolkit.getDefaultToolkit().getScreenSize();
        int x      = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.getX(index));
        int y      = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.getY(index));
        int width  = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.getWidth(index));
        int height = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.getHeight(index));

        // Retrieves the last known size of the screen.
        int lastScreenWidth  = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.SCREEN_WIDTH);
        int lastScreenHeight = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.SCREEN_HEIGHT);

        // If no previous location was saved, or if the resolution has changed,
        // reset the window's dimensions to their default values.
        if(x == -1 || y == -1 || width == -1 || height == -1 ||
           screenSize.width != lastScreenWidth ||  screenSize.height != lastScreenHeight
           || width + x > screenSize.width + 5 || height + y > screenSize.height + 5) {

            // Full screen bounds are not reliable enough, in particular under Linux+Gnome
            // so we simply make the initial window 4/5 of screen's size, and center it.
            // This should fit under any window manager / platform
            x      = screenSize.width / 10;
            y      = screenSize.height / 10;
            width  = (int)(screenSize.width * 0.8);
            height = (int)(screenSize.height * 0.8);
        }

        mainFrame.setBounds(new Rectangle(x, y, width, height));
        
        return mainFrame;
	}
	
    /**
     * Retrieves the initial history, based on previous runs, for the specified frame.
     * @param folderPanelType panel for which the initial path should be returned (either {@link com.mucommander.ui.main.FolderPanel.FolderPanelType.LEFT} or
     *               {@link #@link com.mucommander.ui.main.FolderPanel.FolderPanelType.RIGHT}).
     * @return the locations that were presented in previous runs, which will be the initial history for the current run
     */
    private FileURL[] getInitialHistory(FolderPanelType folderPanelType, int window) {
    	// Checks which kind of initial path we're dealing with.
    	boolean isCustom = MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS, MuPreferences.DEFAULT_STARTUP_FOLDERS).equals(MuPreferences.STARTUP_FOLDERS_CUSTOM);

    	/*// Snapshot configuration
        Configuration snapshot = MuConfigurations.getSnapshot();
        
    	// Get the index of the window that was selected in the previous run
    	int indexOfPreviouslySelectedWindow = MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.getSelectedWindow());
    	
    	int nbLocations = snapshot.getVariable(MuSnapshot.getRecentLocationsCountVariable(indexOfPreviouslySelectedWindow, folderPanelType == FolderPanelType.LEFT), 0);
    	List<FileURL> locations = new LinkedList<FileURL>();
    	
    	for (int i=0; i<nbLocations; ++i) {
			try {
				FileURL location = FileURL.getFileURL(snapshot.getVariable(MuSnapshot.getRecentLocationVariable(indexOfPreviouslySelectedWindow, folderPanelType == FolderPanelType.LEFT, i)));
				locations.add(location);
			} catch (MalformedURLException e) {
				LOGGER.debug("Got invalid URL from the snapshot file", e);
			}
    	}
    	
    	LOGGER.debug("initial history:");
        for (FileURL location:locations)
       	 LOGGER.debug("\t"+location);
    	
    	return locations.toArray(new FileURL[0]);*/
    	return new FileURL[0];
    }
    
    private int getInitialSelectedTab(FolderPanelType folderPanelType, int window) {
    	// Checks which kind of initial path we're dealing with.
    	boolean isCustom = MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS, MuPreferences.DEFAULT_STARTUP_FOLDERS).equals(MuPreferences.STARTUP_FOLDERS_CUSTOM);
    	
    	return isCustom ? 
    		0 :
    		MuConfigurations.getSnapshot().getIntegerVariable(MuSnapshot.getTabsSelectionVariable(window, folderPanelType == FolderPanelType.LEFT));
    }
}

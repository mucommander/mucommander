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

package com.mucommander.ui.main.frame;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.impl.ToggleUseSinglePanelAction;
import com.mucommander.ui.main.FolderPanel.FolderPanelType;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.tabs.ConfFileTableTab;

/**
 * 
 * @author Arik Hadas
 */
public class DefaultMainFramesBuilder extends MainFrameBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMainFramesBuilder.class);

    private Configuration snapshot = MuSnapshot.getSnapshot();

    public DefaultMainFramesBuilder() { }

    @Override
    public int getSelectedFrame() {
        return Math.max(snapshot.getIntegerVariable(MuSnapshot.getSelectedWindow()), 0);
    }

    @Override
    public Collection<MainFrame> build() {
        int nbFrames = snapshot.getIntegerVariable(MuSnapshot.getWindowsCount());
        // if last configuration is requested and exists in the snapshot file, restore it
        if (nbFrames > 0 && MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS).equals(MuPreferences.STARTUP_FOLDERS_LAST)) {
            return IntStream.range(0, nbFrames)
                    .mapToObj(this::createMainFrame)
                    .collect(Collectors.toList());
        }
        else {
            int index = getSelectedFrame();

            MainFrame mainFrame = new MainFrame(
                    new ConfFileTableTab(getInitialPath(FolderPanelType.LEFT)),
                    getFileTableConfiguration(FolderPanelType.LEFT, index),
                    new ConfFileTableTab(getInitialPath(FolderPanelType.RIGHT)),
                    getFileTableConfiguration(FolderPanelType.RIGHT, index));

            // if there is no window saved in the snapshot file, use default settings
            if (nbFrames == 0) {
                mainFrame.setBounds(getDefaultSize());
            }
            // otherwise, use the settings of the selected window
            else {
                int x      = snapshot.getIntegerVariable(MuSnapshot.getX(index));
                int y      = snapshot.getIntegerVariable(MuSnapshot.getY(index));
                int width  = snapshot.getIntegerVariable(MuSnapshot.getWidth(index));
                int height = snapshot.getIntegerVariable(MuSnapshot.getHeight(index));

                mainFrame.setBounds(new Rectangle(x, y, width, height));
            }

            return Collections.singleton(mainFrame);
        }
    }

    private MainFrame createMainFrame(int index) {
        int nbTabsInLeftPanel = snapshot.getIntegerVariable(MuSnapshot.getTabsCountVariable(index, true));
        ConfFileTableTab[] leftTabs = new ConfFileTableTab[nbTabsInLeftPanel];
        for (int i=0; i<nbTabsInLeftPanel; ++i)
            leftTabs[i] = new ConfFileTableTab(
                    snapshot.getBooleanVariable(MuSnapshot.getTabLockedVariable(index, true, i)),
                    restoreFileURL(snapshot.getVariable(MuSnapshot.getTabLocationVariable(index, true, i))),
                    snapshot.getVariable(MuSnapshot.getTabTitleVariable(index, true, i)));

        int nbTabsInRightPanel = snapshot.getIntegerVariable(MuSnapshot.getTabsCountVariable(index, false));
        ConfFileTableTab[] rightTabs = new ConfFileTableTab[nbTabsInRightPanel];
        for (int i=0; i<nbTabsInRightPanel; ++i)
            rightTabs[i] = new ConfFileTableTab(
                    snapshot.getBooleanVariable(MuSnapshot.getTabLockedVariable(index, false, i)),
                    restoreFileURL(snapshot.getVariable(MuSnapshot.getTabLocationVariable(index, false, i))),
                    snapshot.getVariable(MuSnapshot.getTabTitleVariable(index, false, i)));

        MainFrame mainFrame = new MainFrame(
                leftTabs,
                getInitialSelectedTab(FolderPanelType.LEFT, index),
                getFileTableConfiguration(FolderPanelType.LEFT, index),
                rightTabs,
                getInitialSelectedTab(FolderPanelType.RIGHT, index),
                getFileTableConfiguration(FolderPanelType.RIGHT, index));

        // Retrieve last saved window bounds
        Dimension screenSize   = Toolkit.getDefaultToolkit().getScreenSize();
        int x      = MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.getX(index));
        int y      = MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.getY(index));
        int width  = MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.getWidth(index));
        int height = MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.getHeight(index));

        // Retrieves the last known size of the screen.
        int lastScreenWidth  = MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.SCREEN_WIDTH);
        int lastScreenHeight = MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.SCREEN_HEIGHT);

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

        // Retrieve the Frame's SinglePanelView toggle state...
        if (MuSnapshot.getSnapshot().getBooleanVariable(MuSnapshot.getSinglePanelViewToggleState(index))) {
            ActionManager.performAction(ToggleUseSinglePanelAction.Descriptor.ACTION_ID, mainFrame);
        }

        return mainFrame;
    }

    private int getInitialSelectedTab(FolderPanelType folderPanelType, int window) {
        // Checks which kind of initial path we're dealing with.
        boolean isCustom = MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS, MuPreferences.DEFAULT_STARTUP_FOLDERS).equals(MuPreferences.STARTUP_FOLDERS_CUSTOM);

        return isCustom ? 0
                : MuSnapshot.getSnapshot().getIntegerVariable(MuSnapshot.getTabsSelectionVariable(window, folderPanelType == FolderPanelType.LEFT));
    }

    private FileURL restoreFileURL(String url) {
        try {
            return FileURL.getFileURL(url);
        } catch (MalformedURLException e) {
            return getHomeFolder().getURL();
        }
    }
}

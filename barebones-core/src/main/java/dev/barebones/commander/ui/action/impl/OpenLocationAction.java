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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.bookmark.Bookmark;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.local.LocalFile;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action opens a specified location in the current active FileTable. The location can be designated by either a
 * FileURL, path, or AbstractFile.
 *
 * @author Maxence Bernard
 */
public class OpenLocationAction extends ActiveTabAction {

    private FileURL url;
    private AbstractFile file;
    private String path;


    /**
     * Creates a new OpenLocationAction instance using the provided url's string representation
     * (with credentials stripped out) as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, FileURL url) {
        this(mainFrame, properties, url, url.getScheme().equals(LocalFile.SCHEMA)?url.getPath():url.toString(false));
    }

    /**
     * Creates a new OpenLocationAction instance using the provided FileURL and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, FileURL url, String label) {
        super(mainFrame, properties);

        this.url = url;
        setLabel(label);
        setToolTipText(url.getScheme().equals(LocalFile.SCHEMA)?url.getPath():url.toString(false));
    }


    /**
     * Creates a new OpenLocationAction instance using the filename of the provided AbstractFile 
     * as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, AbstractFile file) {
        this(mainFrame, properties, file, file.getName());
    }

    /**
     * Creates a new OpenLocationAction instance using the provided AbstractFile and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, AbstractFile file, String label) {
        super(mainFrame, properties);

        this.file = file;
        setLabel(label);
        setToolTipText(file.getAbsolutePath());
    }


    /**
     * Creates a new OpenLocationAction instance using the provided path as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, String path) {
        this(mainFrame, properties, path, path);
    }

    /**
     * Creates a new OpenLocationAction instance using the provided path and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, String path, String label) {
        super(mainFrame, properties);

        this.path = path;
        setLabel(label);
        setToolTipText(path);
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, Map, String, String)} with
     * {@link Bookmark#getLocation()} and {@link Bookmark#getName()}.
     */
    public OpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, Bookmark bookmark) {
        this(mainFrame, properties, bookmark.getLocation(), bookmark.getName());
    }


    /**
     * Returns the {@link FolderPanel} on which to change the current folder. This method returns the currently active
     * panel but can be overridden if another panel should be used.
     *
     * @return the currently active panel
     */
    protected FolderPanel getFolderPanel() {
        return mainFrame.getActivePanel();
    }

    /**
     * Enables or disables this action based on the current tab is not locked,
     * this action will be enabled, if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        setEnabled(!mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked());
    }

    /////////////////////////////
    // MuAction implementation //
    /////////////////////////////

    @Override
    public void performAction() {
        FolderPanel folderPanel = getFolderPanel();
        if(url!=null) {
            folderPanel.tryChangeCurrentFolder(url);
        }
        else if(file!=null) {
            folderPanel.tryChangeCurrentFolder(file);
        }
        else if(path!=null) {
            folderPanel.tryChangeCurrentFolder(path);
        }
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new ActionProperties.NullActionDescriptor();
	}
}

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


package com.mucommander.ui.event;

import com.mucommander.file.FileURL;
import com.mucommander.ui.main.FolderPanel;


/**
 * Event used to indicate that a folder change is or has occurred. This event is passed to to every LocationListener
 * that registered to receive those events on a particular FolderPanel.
 *
 * @author Maxence Bernard
 */
public class LocationEvent {

    /** FolderPanel where location has or is being changed */
    private FolderPanel folderPanel;

    /** URL of the folder that has or is being changed */
    private FileURL folderURL;


    /**
     * Creates a new LocationEvent.
     *
     * @param folderPanel FolderPanel where location has or is being changed.
     * @param folderURL url of the folder that has or is being changed
     */
    public LocationEvent(FolderPanel folderPanel, FileURL folderURL) {
        this.folderPanel = folderPanel;
        this.folderURL = folderURL;
    }


    /**
     * Returns the FolderPanel instance where location has or is being changed.
     */
    public FolderPanel getFolderPanel() {
        return folderPanel;
    }


    /**
     * Returns the URL to the folder that has or is being changed.
     */
    public FileURL getFolderURL() {
        return folderURL;
    }
}

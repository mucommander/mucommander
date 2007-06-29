/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.ui.event;

import com.mucommander.ui.FolderPanel;


/**
 * Interface to be implemented by classes that wish to be notified of active table changes on a particular MainFrame.
 * Those classes need to be registered to receive those events, this can be done by calling
 * {@link com.mucommander.ui.MainFrame#addActivePanelListener(ActivePanelListener)}.
 *
 * @see com.mucommander.ui.MainFrame
 * @author Maxence Bernard
 */
public interface ActivePanelListener {

    /**
     * This method is invoked when the currently active (i.e. that has focus) folder panel has changed on the MainFrame.
     *
     * @param folderPanel the new active FolderPanel.
     */
    public void activePanelChanged(FolderPanel folderPanel);
}

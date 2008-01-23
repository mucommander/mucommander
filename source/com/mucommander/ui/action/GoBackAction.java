/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.action;

import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * This action recalls the previous folder in the current FolderPanel's history.
 *
 * @author Maxence Bernard
 */
public class GoBackAction extends GoToAction {

    public GoBackAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }


    public void performAction() {
        mainFrame.getActiveTable().getFolderPanel().getFolderHistory().goBack();
    }


    /**
     * Enables or disables this action based on the history of the currently active FolderPanel: if there is a previous
     * folder in the history, this action will be enabled, if not it will be disabled.
     */
    protected void toggleEnabledState() {
        setEnabled(mainFrame.getActiveTable().getFolderPanel().getFolderHistory().hasBackFolder());
    }
}

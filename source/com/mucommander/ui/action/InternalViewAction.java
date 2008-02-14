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

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.viewer.ViewerRegistrar;

import java.util.Hashtable;
import javax.swing.ImageIcon;

/**
 * Opens the current file in view mode.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class InternalViewAction extends AbstractViewerAction {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>InternalViewAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public InternalViewAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        ImageIcon icon;
        if((icon = getIcon(ViewAction.class)) != null)
            setIcon(icon);
    }



    // - AbstractViewerAction implementation ---------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the internal viewer on the specified file.
     * @param file file to view.
     */
    public void performInternalAction(AbstractFile file) {ViewerRegistrar.createViewerFrame(mainFrame, file, getIcon().getImage());}
}

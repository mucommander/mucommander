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


package com.mucommander.ui.dialog.server;

import com.mucommander.file.FileURL;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;


/**
 * 
 *
 * @author Maxence Bernard
 */
abstract class ServerPanel extends XAlignedComponentPanel {

    protected ServerConnectDialog dialog;
    protected MainFrame mainFrame;
	
	
    protected ServerPanel(ServerConnectDialog dialog, MainFrame mainFrame) {
        // Add a 10-pixel gap label and text component
        super(10);
		
        this.dialog = dialog;
        this.mainFrame = mainFrame;
    }
	
	
    public Insets getInsets() {
        return new Insets(8, 6, 8, 6);
    }
	
	
    protected void addTextFieldListeners(JTextField textField, boolean updateLabel) {
        textField.addActionListener(dialog);
        if(updateLabel)
            textField.getDocument().addDocumentListener(dialog);
    }

	
    /** 
     * Returns the current server URL represented by this panel, or null if it is not available.
     * This method can be called at any time by ServerConnectDialog.
     */
    abstract FileURL getServerURL() throws MalformedURLException;

    /**
     * Returns <code>true</code> if this panel allows the user to specify credentials for the connection protocol.
     */
    abstract boolean usesCredentials();
    
    /**
     * This method is called by ServerConnectDialog when this panel is no longer displayed and used. This is where
     * current text field values should be saved for later when a new instance is created.
     */
    abstract void dispose();
}

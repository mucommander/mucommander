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

package com.mucommander.ui.main.tabs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;

/**
* This panel is the header of the presented tabs under Java 1.6 and above.
* The panel contains a button for closing the tab.
* 
* @author Arik Hadas, Maxence Bernard
*/
public class FileTableTabHeader extends JPanel implements ActionListener {
	
	private FolderPanel folderPanel;
	
	private JLabel lockedIcon;

	private static final String CLOSE_ICON_NAME = "close.png";
    private static final String CLOSE_ROLLOVER_ICON_NAME = "close_rollover.png";
    private static final int CLOSE_ICON_SIZE = 12;
    
    public static final String LOCKED_ICON_NAME = "lock.png";
    public static final int LOCKED_ICON_SIZE = 12;

    FileTableTabHeader(FolderPanel folderPanel, boolean closable, FileTableTab tab) {
        super(new GridBagLayout());

        this.folderPanel = folderPanel;
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridy = 0;

        // Locked tab icon
        lockedIcon = new LockedIcon();
        gbc.weightx = 0;    // required otherwise extra width may be redistributed around the button
        gbc.gridx = 0;
        lockedIcon.setVisible(false);
        add(lockedIcon, gbc);
        
        // Label
        JLabel label = new JLabel();
        Font font = new JLabel().getFont();
        label.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
        // Add extra space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        gbc.weightx = 1;
        gbc.gridx = 1;
        add(label, gbc);

        if (closable && !tab.isLocked()) {
        	// Close tab button
        	JButton closeButton = new CloseButton();
        	closeButton.addActionListener(this);
        	gbc.weightx = 1;    // required otherwise extra width may be redistributed around the button
        	gbc.gridx = 2;
        	add(closeButton, gbc);
        }

        setText(tab.getDisplayableTitle());
        
        lockedIcon.setVisible(tab.isLocked());
    }

    private void setText(String text) {
    	JLabel label = (JLabel)getComponent(1); 

        // Truncate the title if it is too long.
        // Note: 31 is the maximum title length displayed in tabs by Firefox and Safari at the time of this writing
        if(text.length()>31)
            text = text.substring(0, 32) + "…";

    	label.setText(text);

    	validate();
    }
    
    /********************************
	 * ActionListener Implementation
	 ********************************/
    
	public void actionPerformed(ActionEvent e) {
    	folderPanel.getTabs().close(this);
	}
    
    /**************************************************
	 * Buttons which are presented in the tab's header
	 **************************************************/
    private class CloseButton extends JButton {
    	 
        public CloseButton() {
            setPreferredSize(new Dimension(CLOSE_ICON_SIZE, CLOSE_ICON_SIZE));
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorderPainted(false);
            setIcon(IconManager.getIcon(IconManager.COMMON_ICON_SET, CLOSE_ICON_NAME));
            //Making nice rollover effect
            setRolloverEnabled(true);
            setRolloverIcon(IconManager.getIcon(IconManager.COMMON_ICON_SET, CLOSE_ROLLOVER_ICON_NAME));
        }


        // Remove default insets
        @Override
        public Insets getInsets() {
            return new Insets(0,0,0,0);
        }

        // We don't want to update UI for this button
        @Override
        public void updateUI() {
        }
    }
    
    private class LockedIcon extends JLabel {
   	 
        public LockedIcon() {
        	super(IconManager.getIcon(IconManager.COMMON_ICON_SET, LOCKED_ICON_NAME));
            setPreferredSize(new Dimension(LOCKED_ICON_SIZE, LOCKED_ICON_SIZE));
            //No need to be focusable
            setFocusable(false);
        }

        // Remove default insets
        @Override
        public Insets getInsets() {
            return new Insets(0,0,0,0);
        }
    }
}

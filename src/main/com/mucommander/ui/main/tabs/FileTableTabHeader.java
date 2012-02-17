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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

import com.mucommander.ui.main.FolderPanel;

/**
* This panel is the header of the presented tabs under Java 1.6 and above.
* The panel contains a button for closing the tab.
* 
* @author Arik Hadas
*/
class FileTableTabHeader extends JPanel implements ActionListener {
	
	private FolderPanel folderPanel;
	
    public FileTableTabHeader(FolderPanel folderPanel) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        this.folderPanel = folderPanel;
        
        setOpaque(false);
        
        JLabel label = new JLabel();
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //should be the first component in the panel
        add(label);
        //tab button
        JButton button = new CloseButton();
        button.addActionListener(this);
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0)); // TODO: needed?
    }
    
    public void setTitle(String title) {
    	JLabel label = (JLabel)getComponent(0); 
    	label.setText(title);
    	validate();
    }
    
    public String getTitle() {
    	JLabel label = (JLabel)getComponent(0); 
    	return label.getText();
    }
    
    @Override
	public void actionPerformed(ActionEvent e) {
    	folderPanel.getTabs().close(this);
	}
    
    /**********************
	 * 
	 **********************/
    private class CloseButton extends JButton {
    	 
        public CloseButton() {
            int size = 16;
            setPreferredSize(new Dimension(size, size));
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
//            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
        }

        //we don't want to update UI for this button
        public void updateUI() {}
        
        //paint the cross
        protected void paintComponent(Graphics g) {
        	super.paintComponent(g);

        	Graphics2D g2 = (Graphics2D) g.create();
        	//shift the image for pressed buttons
        	if (getModel().isPressed()) {
        		g2.translate(1, 1);
        	}

        	setBorderPainted(getModel().isRollover());

        	g2.setStroke(new BasicStroke(2));
        	g2.setColor(Color.BLACK);
        	int delta = 6;
        	g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
        	g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
        	g2.dispose();
        }
    }
}

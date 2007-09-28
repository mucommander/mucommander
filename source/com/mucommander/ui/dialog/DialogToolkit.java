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


package com.mucommander.ui.dialog;

import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.main.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


/**
 * Placeholder for convenience methods that ease dialog creation.
 *
 * @author Maxence Bernard
 */
public class DialogToolkit {

    public static boolean fitToMinDimension(Window window, Dimension minD) {
        return fitToDimension(window, minD, true);
    }
	
    public static boolean fitToMaxDimension(Window window, Dimension maxD) {
        return fitToDimension(window, maxD, false);
    }

    public static boolean fitToScreen(Window window) {
        Rectangle screenBounds = WindowManager.getFullScreenBounds(window);
        return fitToMaxDimension(window, new Dimension((int)screenBounds.getWidth(), (int)screenBounds.getHeight()));
    }
	
    private static boolean fitToDimension(Window window, Dimension d, boolean min) {
        int maxWidth = (int)d.getWidth();
        int maxHeight = (int)d.getHeight();
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        boolean changeSize = false;
		
        // Minimum dimension
        if(min) {
            if(windowWidth<maxWidth) {
                windowWidth = maxWidth;
                changeSize = true;
            }
				
            if(windowHeight<maxHeight) {
                windowHeight = maxHeight;
                changeSize = true;
            }
        }
        // Maximum dimension
        else {
            if(windowWidth>maxWidth) {
                windowWidth = maxWidth;
                changeSize = true;
            }
				
            if(windowHeight>maxHeight) {
                windowHeight = maxHeight;
                changeSize = true;
            }
        }
		
        // Dimension needs to be changed
        if(changeSize)
            window.setSize(windowWidth, windowHeight);
		
        // Return true if dimension was changed 
        return changeSize;
    }
	
    
    /**
     * Sets the given component's (JFrame, JDialog...) location to be centered on screen.
     */
    public static void centerOnScreen(Component c) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        c.setLocation(screenSize.width/2 - c.getWidth()/2,
                    screenSize.height/2 - c.getHeight()/2);
    }

    /**
     * Centers the specified component on the specified window.
     * <p>
     * Note that this method assumes <code>c</code>'s dimension to be at most that of the screen.
     * This can be ensured through {@link #fitToScreen(Component)}. If this constraint is not respected,
     * behaviour is unpredictable.
     * </p>
     * @param c      component to center.
     * @param window window to center on.
     */
    public static void centerOnWindow(Component c, Window window) {
        Dimension screenSize;
        int       x;
        int       y;
        int       buffer;

        x          = Math.max(0, window.getX() + (window.getWidth() - c.getWidth()) / 2);
        y          = Math.max(0, window.getY() + (window.getHeight() - c.getHeight()) / 2);
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if((buffer = screenSize.width - (c.getWidth() + x)) < 0)
            x += buffer;
        if((buffer = screenSize.height - (c.getHeight() + y)) < 0)
            y += buffer;
        c.setLocation(x, y);
    }
    
	
    /**
     * Creates an OK/Cancel panel using the given buttons, and register the given listener for button actions.
     */
    public static JPanel createOKCancelPanel(JButton okButton, JButton cancelButton, ActionListener actionListener) {
        return createButtonPanel(new JButton[]{okButton, cancelButton}, actionListener);
    }

    /**
     * Creates an OK panel using the given button, and register the given listener for button actions.
     */
    public static JPanel createOKPanel(JButton okButton, ActionListener actionListener) {
        return createButtonPanel(new JButton[]{okButton}, actionListener);
    }

    /**
     * Creates a button panel using the given buttons, and register the given listener for button actions.
     * Buttons are disposed horizontally, aligned to the right.
     */
    public static JPanel createButtonPanel(JButton buttons[], ActionListener actionListener) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel tempPanel = new JPanel(new GridLayout(1,buttons.length));
        
        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        JButton button;
        for(int i=0; i<buttons.length; i++) {
            button = buttons[i];
            button.setMnemonic(mnemonicHelper.getMnemonic(button.getText()));
            button.addActionListener(actionListener);
            tempPanel.add(button);
        }
        
        panel.add(tempPanel);
        return panel;
    }

}

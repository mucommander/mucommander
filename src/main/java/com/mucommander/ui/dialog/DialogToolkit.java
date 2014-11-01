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


package com.mucommander.ui.dialog;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.mucommander.ui.button.ButtonChoicePanel;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.helper.ScreenServices;


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
        Rectangle screenBounds = ScreenServices.getFullScreenBounds(window);
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
     * This can be ensured through {@link #fitToScreen(Window)}. If this constraint is not respected,
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
    public static JPanel createOKCancelPanel(JButton okButton, JButton cancelButton, JRootPane rootPane, ActionListener actionListener) {
        return createButtonPanel(new JButton[]{okButton, cancelButton}, rootPane, actionListener);
    }

    /**
     * Creates an OK panel using the given button, and register the given listener for button actions.
     */
    public static JPanel createOKPanel(JButton okButton, JRootPane rootPane, ActionListener actionListener) {
        return createButtonPanel(new JButton[]{okButton}, rootPane, actionListener);
    }

    /**
     * Creates a button panel using the given buttons, and register the given listener for button actions.
     * Buttons are disposed horizontally, aligned to the right.
     */
    public static JPanel createButtonPanel(JButton buttons[], JRootPane rootPane, ActionListener actionListener) {
        JPanel panel = new ButtonChoicePanel(buttons, 0, rootPane);
        
        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        for (JButton button : buttons) {
            button.setMnemonic(mnemonicHelper.getMnemonic(button.getText()));
            button.addActionListener(actionListener);
            panel.add(button);
        }

        return panel;
    }

    /**
     * Returns the specified component's toplevel <code>Frame</code> or
     * <code>Dialog</code>.
     *
     * @param parentComponent the <code>Component</code> to check for a
     *		<code>Frame</code> or <code>Dialog</code>
     * @return the <code>Frame</code> or <code>Dialog</code> that
     *		contains the component, or the default
     *         	frame if the component is <code>null</code>,
     *		or does not have a valid
     *         	<code>Frame</code> or <code>Dialog</code> parent
     * @exception HeadlessException if
     *   <code>GraphicsEnvironment.isHeadless</code> returns
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static Window getWindowForComponent(Component parentComponent) throws HeadlessException {
        // Note: this method is a shameless rip from javax.swing.JOptionPane
        if (parentComponent == null)
            return JOptionPane.getRootFrame();
        if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
            return (Window)parentComponent;
        return getWindowForComponent(parentComponent.getParent());
    }
}

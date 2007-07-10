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

package com.mucommander.ui.button;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * CollapseExpandButton extends JButton to provide expand/collapse functionality to a JPanel: clicking the button
 * expands/collapses the associated component making it visible/unvisible, and resizes the window that contains it so that
 * it properly fits.
 *
 * <p>This button shows an down/right arrow icon to reflect the current expanded/collapsed state.
 *
 * @author Maxence Bernard
 */
public class CollapseExpandButton extends ArrowButton implements ActionListener, KeyListener {

    private Component comp;
    private Window window;

    private boolean expandedState;

    
    /**
     * Creates a new CollapseExpandButton attached to the given component and window, and makes the specified component
     * visible/invisible depending on the provided initial expanded/collapsed state.
     *
     * @param label the label to use for this button
     * @param component the component that is to be expanded/collapsed
     * @param window the window that contains the specified component
     * @param expanded initial expanded/collapsed state
     */
    public CollapseExpandButton(String label, Component component, Window window, boolean expanded) {
        this.comp = component;
        this.window = window;

        setText(label);
        setExpandedState(expanded, false);

        addActionListener(this);
        addKeyListener(this);
    }


    /**
     * Expands/collapses the associated component, resizes the window that contains it, and updates the button's icon
     * to reflect the new state.
     *
     * @param expanded if <code>true</code>, the component will be expanded, collapsed if <code>false</code>.
     */
    public void setExpandedState(boolean expanded) {
        setExpandedState(expanded, true);
    }


    /**
     * Returns the current expanded state: <code>true</code> for expanded, <code>false</code> for collapsed. 
     */
    public boolean getExpandedState() {
        return expandedState;
    }

    
    private void setExpandedState(boolean expanded, boolean packWindow) {
        if(expanded) {
            setArrowDirection(DOWN_DIRECTION);
            comp.setVisible(true);
        }
        else {
            setArrowDirection(RIGHT_DIRECTION);
            comp.setVisible(false);
        }

        this.expandedState = expanded;

        if(packWindow)
            window.pack();
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent actionEvent) {
        setExpandedState(!expandedState, true);
    }


    ////////////////////////////////
    // KeyListener implementation //
    ////////////////////////////////


    public void keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if(keyCode==KeyEvent.VK_RIGHT && !expandedState)
            setExpandedState(true, true);
        else if(keyCode==KeyEvent.VK_LEFT && expandedState)
            setExpandedState(false, true);
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
    }
}

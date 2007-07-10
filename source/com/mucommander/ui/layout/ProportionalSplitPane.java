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

package com.mucommander.ui.layout;

import com.mucommander.PlatformManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * ProportionalSplitPane is a JSplitPane that is able to maintain the divider's location constant proportionally when
 * the window it is attached to is resized, or when its orientation is changed.
 *
 * <p>Another added feature is the ability to restore a split ratio of 0.5f (same size for both panels) when
 * the divider component is double-clicked.
 *
 * @author Maxence Bernard
 */
public class ProportionalSplitPane extends JSplitPane implements ComponentListener, MouseListener {

    /** Last known absolute divider location */
    private int lastDividerLocation = -1;

    /** Current proportional divider location, initially 0.5f (same size for both panels) */
    private float splitRatio = 0.5f;

    /** Window this split pane is attached to */
    private Window window;


    public ProportionalSplitPane(Window window) {
        super();
        init(window, null, null);
    }

    public ProportionalSplitPane(Window window, int orientation) {
        super(orientation);
        init(window, null, null);
    }

    public ProportionalSplitPane(Window window, int orientation, boolean continuousLayout) {
        super(orientation, continuousLayout);
        init(window, null, null);
    }

    public ProportionalSplitPane(Window window, int orientation, JComponent leftComponent, JComponent rightComponent) {
        super(orientation, leftComponent, rightComponent);
        init(window, leftComponent, rightComponent);
    }

    public ProportionalSplitPane(Window window, int orientation, boolean continuousLayout, JComponent leftComponent, JComponent rightComponent) {
        super(orientation, continuousLayout, leftComponent, rightComponent);
        init(window, leftComponent, rightComponent);
    }


    private void init(Window window, JComponent leftComponent, JComponent rightComponent) {
        this.window = window;
        window.addComponentListener(this);

        BasicSplitPaneDivider divider = getDividerComponent();
        divider.addComponentListener(this);
        divider.addMouseListener(this);

        // Set null minimum size for both components so that divider can be moved all the way left/up and right/down
        Dimension nullDimension = new Dimension(0,0);
        if(leftComponent!=null)
            leftComponent.setMinimumSize(nullDimension);
        if(rightComponent!=null)
            rightComponent.setMinimumSize(nullDimension);
    }


    /**
     * Updates the divider component's location to keep the current proportional divider location. 
     */
    public void updateDividerLocation() {
//if(Debug.ON) Debug.trace("1: lastDividerLocation="+lastDividerLocation+" dimension="+(getOrientation()==HORIZONTAL_SPLIT?getWidth():getHeight())+" isVisible="+isVisible());

        if(!window.isVisible())
            return;

        setDividerLocation((int)(splitRatio*(getOrientation()==HORIZONTAL_SPLIT?getWidth():getHeight())));
        // Remember last divider's location (see componentMoved())
        lastDividerLocation = getDividerLocation();
//if(Debug.ON) Debug.trace("2: lastDividerLocation="+lastDividerLocation);
    }


    /**
     * Sets the constant, proportional divider's location. The given float but be comprised between 0 and 1, 0 meaning
     * completely left (or top), 1 right completely (or bottom).   
     *
     * @param splitRatio the proportional divider's location, comprised between 0 and 1.
     */
    public void setSplitRatio(float splitRatio) {
        this.splitRatio = splitRatio;
        lastDividerLocation = -1;
        updateDividerLocation();
    }


    /**
     * Returns the split pane divider component.
     */
    public BasicSplitPaneDivider getDividerComponent() {
        return ((BasicSplitPaneUI)getUI()).getDivider();
    }


    /**
     * Disables all the JSPlitPane accessibility shortcuts that are registered by default:
     * <ul>
     * <li>Navigate in - Tab
     * <li>Navigate out - Ctrl+Tab, Ctrl+Shift+Tab
     * <li>Navigate between - Tab, F6
     * <li>Give focus spliter bar - F8
     * <li>Change size - Arrow keys, home, and end (moves the pane splitter appropriately)
     * </ul>
     */
    public void disableAccessibilityShortcuts() {
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        InputMap parentInputMap = inputMap.getParent();
        KeyStroke ks[] = inputMap.allKeys();
        for(int i=0; i<ks.length; i++)
            parentInputMap.remove(ks[i]);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        // Maintain the divider's proportional location
        updateDividerLocation();
    }


    //////////////////////////////////////
    // ComponentListener implementation //
    //////////////////////////////////////

    /**
     * Sets the divider location when the ContentPane has been resized so that it stays at the
     * same proportional (not absolute) location.
     */
    public void componentResized(ComponentEvent e) {
        Object source = e.getSource();

        if(source==window) {
//            if(Debug.ON) Debug.trace("called on MainFrame ratio="+splitRatio);

            // Note: the window/split pane may not be visible when this method is called for the first time
            updateDividerLocation();
        }
    }

    public void componentMoved(ComponentEvent e) {
        Object source = e.getSource();

        // Called when divider (or window) has been moved
        if(source== getDividerComponent()) {
//            if(Debug.ON) Debug.trace("called on splitPane divider, lastDividerLocation="+lastDividerLocation+" splitPane.getDividerLocation()="+getDividerLocation());

            // Ignore this event if the divider's location hasn't changed, or if the initial divider's location
            // hasn't been set yet
            if(lastDividerLocation==-1 || lastDividerLocation==getDividerLocation()) {
//                if(Debug.ON) Debug.trace("same divider location, ignoring event");
                return;
            }

//            if(Debug.ON) Debug.trace("old ratio="+splitRatio);

            // Divider has been moved, calculate new split ratio
            lastDividerLocation = getDividerLocation();
            splitRatio = lastDividerLocation/(float)(getOrientation()==HORIZONTAL_SPLIT?getWidth():getHeight());

//            if(Debug.ON) Debug.trace("new ratio="+splitRatio);
        }
    }

    public void componentShown(ComponentEvent e) {
        // Called when the window is made visible
        if(e.getSource()==window) {
            // Set initial divider's location
            updateDividerLocation();
        }
    }

    public void componentHidden(ComponentEvent e) {
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent mouseEvent) {
        if(PlatformManager.isLeftMouseButton(mouseEvent) && mouseEvent.getClickCount()==2)
            setSplitRatio(0.5f);
    }

    public void mousePressed(MouseEvent mouseEvent) {
    }

    public void mouseReleased(MouseEvent mouseEvent) {
    }

    public void mouseEntered(MouseEvent mouseEvent) {
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }
}

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

import com.mucommander.ui.action.MuteProxyAction;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * PopupButton is a compound component that combines a JButton with a JPopupMenu.
 *
 * <p>When the mouse is held down on the button, a popup menu is displayed below the button. When the button is clicked,
 * if a specific action was specified at creation time or using {@link #setAction(Action)}, this action is performed.
 * If not, a popup menu is displayed below the button.
 *
 * <p>This class is abstract. Derived classes must implement {@link #getPopupMenu()} to return a JPopupMenu instance
 * to be displayed.
 *
 * @author Maxence Bernard
 */
public abstract class PopupButton extends NonFocusableButton {

    /** Custom action performed when the button is clicked. If null, popup menu will be displayed when mouse is clicked */
    private Action buttonClickedAction;

    /* Timestamp when popup menu was closed */
    private long popupMenuClosedTime;

    /** Non-null while popup menu is visible */
    private JPopupMenu popupMenu;

    /** Location of the popup menu, relative to the button */
    private int popupMenuLocation = BOTTOM;

    /** Controls the number of milliseconds to hold down the mouse button on the button to display the popup menu */
    private final static int POPUP_DELAY = 300;


    /**
     * Creates a new PopupButton with no custom action for when this button is clicked.
     * When this button is clicked, the popup menu as returned by {@link #getPopupMenu()} will be displayed.
     */
    public PopupButton() {
        this(null);
    }

    /**
     * Creates a new PopupButton with a custom action to performed when this button is clicked.
     *
     * @param buttonClickedAction custom action to performed when this button is clicked
     */
    public PopupButton(Action buttonClickedAction) {
        setAction(buttonClickedAction);

        // Listen to mouse events on this button
        addMouseListener(new PopupMenuHandler());
    }


    /**
     * Sets the action to be performed when this button is clicked. If <code>null</code> is passed, a popup menu will
     * displayed when this button is clicked.
     */
    public void setAction(Action buttonClickedAction) {
        if(buttonClickedAction==null) {
            super.setAction(null);
        }
        else {
            // Pass a MuteProxyAction to JButton that does nothing when the action is performed.
            // We need this to keep the use the Action's properties but handle action events ourself.
            super.setAction(new MuteProxyAction(buttonClickedAction));
        }

        this.buttonClickedAction = buttonClickedAction;
    }

    public int getPopupMenuLocation() {
        return popupMenuLocation;
    }

    public void setPopupMenuLocation(int location) {
        this.popupMenuLocation = location;
    }

    /**
     * Returns true if a popup menu is currently being displayed.
     */
    public boolean isPopupMenuVisible() {
        return popupMenu!=null;
    }

    /**
     * Displays the popup menu as returned by {@link #getPopupMenu()}.
     */
    public synchronized void popupMenu() {
        this.popupMenu = getPopupMenu();

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                popupMenuClosed();
            }

            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                popupMenuClosed();
            }

            private void popupMenuClosed() {
                // Set popup menu reference to null once it has been made invisible so that it can be garbage collected,
                // and remember the time at which the popup was closed, so that a mouse press on the button closes
                // the popup menu and does not bring it back up.
                popupMenuClosedTime = System.currentTimeMillis();
                popupMenu = null;
                setSelected(false);
            }
        });

        // Popup up the menu underneath under this button. This has to be executed by the event thread, otherwise some
        // weird repaint issue will arise under Windows at least (Note: this method can be executed by a thread other
        // than the event thread).
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension popupMenuSize = popupMenu.getPreferredSize();

                popupMenu.show(PopupButton.this,
                        popupMenuLocation==RIGHT?getWidth():popupMenuLocation==LEFT?-(int)popupMenuSize.getWidth():0,
                        popupMenuLocation==BOTTOM?getHeight():popupMenuLocation==TOP?-(int)popupMenuSize.getHeight():0
                );

            }
        });

        // Leave the button selected (shows that button has focus) while the popup menu is visible
        setSelected(true);

        // Note: focus MUST NOT be requested on the popup menu because:
        // a/ it's not necessary, focus is automatically transfered to the popup menu
        // b/ it creates a weird bug under Windows which prevents enter key from selecting any menu item
    }


    /**
     * This method is invoked when the popup menu is about to be displayed. This method must return the JPopupMenu
     * instance to display.
     */
    public abstract JPopupMenu getPopupMenu();


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * This inner class controls this button's behavior and actions when the mouse button is clicked or held down.
     */
    private class PopupMenuHandler implements MouseListener, Runnable {

        /** Contains the time at which a mouse button was pressed, 0 when a mouse button is not currently being pressed */
        private long pressedTime;

        /** Returns true to indicate that a mouse event should currently be ignored because the popup menu
         * is visible, or was closed recently (less than POPUP_DELAY ms ago) */
        private boolean shouldIgnoreMouseEvent() {
            return isPopupMenuVisible() || System.currentTimeMillis()- popupMenuClosedTime<POPUP_DELAY;
        }

        //////////////////////////////////
        // MouseListener implementation //
        //////////////////////////////////

        public synchronized void mousePressed(MouseEvent mouseEvent) {
            if(!isEnabled() || shouldIgnoreMouseEvent())    // Ignore event if button is disabled
                return;

            pressedTime = System.currentTimeMillis();

            // Spawn a thread to check if mouse is still pressed in POPUP_DELAY ms. If that is the case, popup menu
            // will be displayed.
            new Thread(this).start();
        }

        public synchronized void mouseClicked(MouseEvent mouseEvent) {
            if(!isEnabled() || shouldIgnoreMouseEvent())    // Ignore event if button is disabled
                return;

            // Indicate to Thread spawn by mousePressed that mouse is not pressed anymore
            pressedTime = 0;

            if(buttonClickedAction !=null)    // Perform the action if there is one
                buttonClickedAction.actionPerformed(new ActionEvent(PopupButton.this, ActionEvent.ACTION_PERFORMED, "clicked"));
            else                // No action, popup menu
                popupMenu();
        }

        public synchronized void mouseReleased(MouseEvent mouseEvent) {
            // Indicate to Thread spawn by mousePressed that mouse is not pressed anymore
            pressedTime = 0;
        }

        public synchronized void mouseExited(MouseEvent mouseEvent) {
            // Indicate to Thread spawn by mousePressed that mouse is not pressed anymore
            pressedTime = 0;
        }

        public void mouseEntered(MouseEvent mouseEvent) {
        }


        /////////////////////////////
        // Runnable implementation //
        /////////////////////////////

        public void run() {
                try { Thread.sleep(POPUP_DELAY); }
                catch(InterruptedException e) {}

                synchronized(this) {
                    // Popup menu if a popup menu is not already being displayed and if mouse is still pressed
                    if(!isPopupMenuVisible() && pressedTime!=0 && System.currentTimeMillis()-pressedTime>=POPUP_DELAY) {
                        popupMenu();
                    }
                }
        }
    }
}

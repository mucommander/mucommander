/*
 * This file is part of muCommander, http://www.mucommander.com
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


package com.mucommander.commons.util.ui.button;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.mucommander.commons.util.ui.helper.MnemonicHelper;


/**
 * ButtonChoicePanel lays out an array of buttons on a grid and provides an easy way
 * for the user to navigate and select buttons :
 * <ul>
 *  <li>At any given time, the current active button (the first one initially) can be selected by pressing ENTER
 *  <li>LEFT key goes back to the previous button, to the last button if current button is the first one
 *  <li>RIGHT key goes forward one button, to the first button if current button is the last one
 *  <li>UP key goes up one row, to the last row if current button is on the first row
 *  <li>DOWN key goes down one row, to the first row if current button is on the last row
 *  <li>Buttons can directly be selected by pressing the Mnemonic associated with the button (if any)
 * </ul>
 * This does not interfere with regular focus management where TAB (resp. Shift+TAB) goes to the next (resp. previous)
 * focusable component.
 * 
 * @author Maxence Bernard
 */
public class ButtonChoicePanel extends JPanel implements KeyListener, FocusListener {

    /** Provided JButton instances */
    private final List<JButton> buttons;

    /** RootPane associated with this ButtonChoicePanel */
    private JRootPane rootPane;

    /** Number of columns of the buttons grid */
    private int nbCols;
    /** Number of row of the buttons grid */
    private int nbRows;

    /** Current button, i.e. the one that currently has focus */
    private int currentButton;

    /**
     * Creates a new ButtonChoicePanel and lays out the given buttons on a grid
     * according to the provided number of colums.
     *
     * <p>Initial focus will be given to the first button.</p>
     *
     * @param buttons  the JButton instances to layout
     * @param nbCols   number of columns for the buttons grid, if <=0 all buttons will be put on a single row
     * @param rootPane associated with this ButtonChoicePanel
     */
    public ButtonChoicePanel(List<JButton> buttons, int nbCols, JRootPane rootPane) {
        this.buttons = buttons;
        int nbButtons = buttons.size();
        this.nbCols = nbCols<=0?nbButtons:nbCols;
        this.rootPane = rootPane;
        this.nbRows = nbCols<=0?1:nbButtons/nbCols+(nbButtons%nbCols==0?0:1);

        // If the provided number of columns is <= 0, lay out all buttons on a single row
        // and use FlowLayout to do that
        if (nbCols<=0) {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
        }
        // Use GridLayout to lay out buttons on 2-dimensional grid
        else {
            setLayout(new GridLayout(0, nbCols));
        }

        for (JButton button : buttons) {
            // Listener to key events to transfer focus 
            button.addKeyListener(this);

            // Allow buttons to be made 'default buttons' when enter is pressed
            button.setDefaultCapable(true);

            // Listen to focus events to make the focused button the default button
            button.addFocusListener(this);

            add(button);
        }

        // Set mnemonics to buttons
        updateMnemonics();

        // First button is made 'default button'
        if (!buttons.isEmpty()) {
            rootPane.setDefaultButton(buttons.get(0));
        }
        this.currentButton = 0;
    }


    /**
     * Updates the buttons mnemonics. This method should be called when at least one of the button's label has changed.
     */
    public void updateMnemonics() {
        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        char mnemonic;

        for (JButton button : buttons) {
            mnemonic = mnemonicHelper.getMnemonic(button);
            if(mnemonic!=0)
                button.setMnemonic(mnemonic);
        }
    }


    /////////////////////////
    // KeyListener methods //
    /////////////////////////

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        int oldCurrentButton = currentButton;
        int nbButtons = buttons.size();

        // LEFT key goes back one button, to the last button if current button is the first one
        if (keyCode==KeyEvent.VK_LEFT) {
            this.currentButton = currentButton==0?nbButtons-1:currentButton-1;
        }
        // RIGHT key goes forward one button, to the first button if current button is the last one
        else if (keyCode==KeyEvent.VK_RIGHT) {
            this.currentButton = currentButton==nbButtons-1?0:currentButton+1;
        }
        // UP key goes up one row, to the last row if current button is on the first row
        else if (keyCode==KeyEvent.VK_UP) {
            if (currentButton<nbCols) {		// If current button is on the first row
                this.currentButton = (nbRows-1)*nbCols+currentButton%nbCols;
                if(this.currentButton>nbButtons-1)
                    this.currentButton -= nbCols;
            }
            else
                this.currentButton -= nbCols;
        }
        // DOWN key goes down one row, to the first row if current button is on the last row
        else if (keyCode==KeyEvent.VK_DOWN) {
            if(nbButtons-currentButton>0 && nbButtons-currentButton<=nbCols)		// If current button is on the last row
                this.currentButton = currentButton%nbCols;
            else
                this.currentButton += nbCols;
        }
        // Click button when a key that corresponds to one of the buttons' mnemonic has been pressed
        else if(!e.isAltDown()) {
            for (JButton button : buttons) {
                if (keyCode==button.getMnemonic()) {
                    button.doClick();
                    return;
                }
            }
        }

        // Make the new button the default button and request focus on this button
        if (oldCurrentButton != currentButton) {
            rootPane.setDefaultButton(buttons.get(currentButton));
            buttons.get(currentButton).requestFocus();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }


    //////////////////////////////////
    // FocusListener implementation //
    //////////////////////////////////

    public void focusGained(FocusEvent focusEvent) {
        // Makes the newly focused button the default button
        rootPane.setDefaultButton((JButton)focusEvent.getComponent());
    }

    public void focusLost(FocusEvent focusEvent) {
    }
}

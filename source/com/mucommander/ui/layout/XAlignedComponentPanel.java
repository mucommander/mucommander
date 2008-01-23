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


package com.mucommander.ui.layout;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;


/**
 * XAlignedComponentPanel is a panel which makes life easier when having
 * to display several rows of label + text field.
 * <p>On each row, labels are right-aligned and text fields left-aligned,
 * looking something like this:<br>
 * <pre>
 *       Label1 [Component1]<br>
 * LongerLabel2 [Component2]<br>
 *       Label3 [Component3]<br>
 * </pre>
 *
 * <p>Vertical space between labels and components, and horizontal space between rows can both be specified.
 *
 * @author Maxence Bernard
 */
public class XAlignedComponentPanel extends JPanel {

    /** Gridbag layout constraints */
    private GridBagConstraints c;

    /** Number of pixels between labels and components */
    private int xSpace;

    /**
     * First component in this panel, which will be given the focus when focus is requested on this panel using
     * {@link #requestFocus()}.
     */
    private JComponent firstComponent;


    public final static int DEFAULT_XPACE = 5;


    /**
     * Creates an initially empty panel, using the default vertical space as defined by {@link #DEFAULT_XPACE} to
     * separate labels and components for all rows added later.
     */
    public XAlignedComponentPanel() {
        this(DEFAULT_XPACE);
    }


    /**
     * Creates an initially empty panel, using the given vertical space to separate labels and components for
     * all rows added later.
     *
     * @param xSpace number of pixels to be inserted between labels and components.
     */
    public XAlignedComponentPanel(int xSpace) {
        // Set grid bag layout
        setLayout(new GridBagLayout());

        setAlignmentX(LEFT_ALIGNMENT);

        // Number of pixels between labels and components
        this.xSpace = xSpace;
		
        // Init gridbag constraints.
        this.c = new GridBagConstraints();
        this.c.anchor = GridBagConstraints.EAST;
    }

    public void setLabelLeftAligned(boolean aligned) {
        this.c.anchor = aligned ? GridBagConstraints.WEST : GridBagConstraints.EAST;
    }

    /**
     * Adds a new row with the given label and component, the component taking all the horizontal space left
     * by the widest label in this XAlignedComponentPanel.
     *
     * @param label text that describes the component
     * @param component JComponent instance, will take all remaining width space
     * @param ySpaceAfter number of pixels to be inserted after this row
     */
    public void addRow(String label, JComponent component, int ySpaceAfter) {
        addRow(new JLabel(label), component, ySpaceAfter);
    }


    /**
     * Adds a new row with the given label and component, the component taking all the horizontal space left
     * by the widest label in this XAlignedComponentPanel.
     *
     * @param label the label component that describes the component
     * @param component JComponent instance that will take all remaining width space
     * @param ySpaceAfter number of pixels to be inserted after this row
     */
    public void addRow(JComponent label, JComponent component, int ySpaceAfter) {
        if(firstComponent ==null)
            firstComponent = component;
		
        // Prepare grid bag constraints for label
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.insets = new Insets(0, 0, ySpaceAfter, xSpace);
		
        add(label, c);

        // Prepare grid bag constraints for component
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, ySpaceAfter, 0);

        add(component, c);
    }


    /**
     * Adds a new row with the specified component left-aligned and taking all available width space.
     *
     * @param component JComponent instance that will take all available width space
     * @param ySpaceAfter number of pixels to be inserted after this row
     */
    public void addRow(JComponent component, int ySpaceAfter) {
        // Prepare grid bag constraints		
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, ySpaceAfter, 0);
	
        add(component, c);
    }
	
	
	
    /**
     * Overrides JPanel#requestFocus() method to request focus on the first component
     * and select its contents if it is an instance of JTextComponent.
     */
    public void requestFocus() {
        if(firstComponent ==null)
            super.requestFocus();
        else {
            if(firstComponent instanceof JTextComponent) {
                JTextComponent textComponent = (JTextComponent) firstComponent;
                String text = textComponent.getText();
                if(!text.equals("")) {
                    textComponent.setSelectionStart(0);
                    textComponent.setSelectionEnd(text.length());
                }
            }			
            firstComponent.requestFocus();
        }
    }
}

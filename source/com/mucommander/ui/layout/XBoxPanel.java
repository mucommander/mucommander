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

import javax.swing.*;
import java.awt.*;


/**
 * Convenience class to make panels with a horizontal BoxLayout easier to use.
 *
 * @author Maxence Bernard
 */
public class XBoxPanel extends JPanel {

    /** Custom insets, can be null if custom insets haven't been specified with {@link #setInsets(Insets)} */
    private Insets insets;
	
	
    /**
     * Creates a new JPanel with a vertical BoxLayout (BoxLayout.X_AXIS).
     */
    public XBoxPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }
	

    /**
     * Creates a new JPanel with a vertical BoxLayout (BoxLayout.X_AXIS) and
     * adds some initial space to the panel.
     */
    public XBoxPanel(int nbPixels) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createRigidArea(new Dimension(nbPixels, 0)));
    }

	
    /**
     * Aligns the given component on the left and adds it to this panel.
     */
    public Component add(Component comp) {
        if(comp instanceof JComponent)
            ((JComponent)comp).setAlignmentX(LEFT_ALIGNMENT);

        return super.add(comp);
    }

    /**
     * Adds a vertical separation of the given size to this panel.
     */
    public void addSpace(int nbPixels) {
        add(Box.createRigidArea(new Dimension(nbPixels, 0)));
    }
	
	
    /**
     * Sets this panel's insets.
     */
    public void setInsets(Insets insets) {
        this.insets = insets;
    }
	
    /**
     * Returns this panel's insets.
     */
    public Insets getInsets() {
        return insets==null?super.getInsets():insets;
    }
}

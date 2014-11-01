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

package com.mucommander.ui.button;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;

import com.mucommander.commons.runtime.OsFamily;

/**
 * NonFocusableButton is a JButton which is non focusable, i.e. that cannot hold keyboard focus.
 *
 * @author Maxence Bernard
 */
public class NonFocusableButton extends JButton {

    public NonFocusableButton() {
        setLookAndFeelProperties();
    }

    public NonFocusableButton(Action a) {
        super(a);
        setLookAndFeelProperties();
    }

    public NonFocusableButton(Icon icon) {
        super(icon);
        setLookAndFeelProperties();
    }

    public NonFocusableButton(String text) {
        super(text);
        setLookAndFeelProperties();
    }

    public NonFocusableButton(String text, Icon icon) {
        super(text, icon);
        setLookAndFeelProperties();
    }


    private void setLookAndFeelProperties() {
        // Fill the content area under the Windows L&F only, required for the borders to be painted.
        // Note: filing the content area under Metal L&F looks like absolute crap.
        setContentAreaFilled(OsFamily.WINDOWS.isCurrent() && "Windows".equals(UIManager.getLookAndFeel().getName()));
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void updateUI() {
        super.updateUI();

        // Update L&F properties 
        setLookAndFeelProperties();
    }
}

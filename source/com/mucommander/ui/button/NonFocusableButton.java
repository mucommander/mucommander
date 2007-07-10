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

import javax.swing.*;

/**
 * NonFocusableButton is a JButton which is non focusable, i.e. that cannot hold keyboard focus.
 *
 * @author Maxence Bernard
 */
public class NonFocusableButton extends JButton {

    public NonFocusableButton() {
    }

    public NonFocusableButton(Action a) {
        super(a);
    }

    public NonFocusableButton(Icon icon) {
        super(icon);
    }

    public NonFocusableButton(String text) {
        super(text);
    }

    public NonFocusableButton(String text, Icon icon) {
        super(text, icon);
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    // For JDK 1.4 and up
    public boolean isFocusable() {
        return false;
    }
}

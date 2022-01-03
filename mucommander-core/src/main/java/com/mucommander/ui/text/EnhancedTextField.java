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
package com.mucommander.ui.text;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


/**
 * @author Gerolf Scherr
 */
public class EnhancedTextField extends JTextField {

    public EnhancedTextField(int columns, boolean selectAllOnFocus) {
        super(columns);
        init(selectAllOnFocus);
    }

    public EnhancedTextField(String s, boolean selectAllOnFocus) {
        super(s);
        init(selectAllOnFocus);
    }

    private void init(boolean selectAllOnFocus) {
        if (selectAllOnFocus) addFocusListener(SelectAllOnFocusListener.get());
    }

}


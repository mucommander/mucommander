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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;


/**
 * @author Gerolf Scherr, Arik Hadas
 */
public class SelectAllOnFocusTextField extends JTextField {

    public SelectAllOnFocusTextField(int columns) {
        super(columns);
        addFocusListener(new SelectAllOnFocusListener(this));
    }

    public SelectAllOnFocusTextField(String s) {
        super(s);
        addFocusListener(new SelectAllOnFocusListener(this));
    }

    private static class SelectAllOnFocusListener extends FocusAdapter {
        private JTextField textField;

        SelectAllOnFocusListener(JTextField textField) {
            this.textField = textField;
        }

        @Override
        public void focusGained(FocusEvent e) {
            textField.selectAll();
        }
    }
}


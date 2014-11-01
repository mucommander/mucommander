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

package com.mucommander.ui.text;

import com.mucommander.ui.autocomplete.BasicAutocompleterTextComponent;
import com.mucommander.ui.autocomplete.CompleterFactory;
import com.mucommander.ui.autocomplete.TextFieldCompletion;

import javax.swing.*;
import javax.swing.text.Document;

/**
 * <code>FilePathField</code> is a text field that is made to receive a file path. It provides auto-completion
 * capabilities, suggesting files/folders to the user as the path is being entered.
 *
 * @author Maxence Bernard
 */
public class FilePathField extends JTextField {

    public FilePathField() {
        super();
        enableAutoCompletion();
    }

    public FilePathField(String text) {
        super(text);
        enableAutoCompletion();
    }

    public FilePathField(int columns) {
        super(columns);
        enableAutoCompletion();
    }

    public FilePathField(String text, int columns) {
        super(text, columns);
        enableAutoCompletion();
    }

    public FilePathField(Document doc, String text, int columns) {
        super(doc, text, columns);
        enableAutoCompletion();
    }


    /**
     * Adds auto-completion capabilities to this text field.
     */
    private void enableAutoCompletion() {
        new TextFieldCompletion(new BasicAutocompleterTextComponent(this), CompleterFactory.getPathCompleter());
    }
}

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

package com.mucommander.ui.viewer.text;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.viewer.EditorFrame;
import com.mucommander.ui.viewer.FileEditor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A simple text editor. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard
 */
class TextEditor extends FileEditor implements DocumentListener {

    private TextEditorImpl textEditorImpl;

    public TextEditor() {
        textEditorImpl = new TextEditorImpl(true);

        setLayout(new BorderLayout());
        add(textEditorImpl.getTextArea(), BorderLayout.NORTH);
    }


    ///////////////////////////////
    // FileEditor implementation //
    ///////////////////////////////

    protected void saveAs(AbstractFile destFile) throws IOException {
        OutputStream out = destFile.getOutputStream(false);
        out.write(textEditorImpl.getTextArea().getText().getBytes(textEditorImpl.getFileEncoding()));
        out.close();

        setSaveNeeded(false);
    }

    public void edit(AbstractFile file) throws IOException {
        textEditorImpl.startEditing(file, this);

        EditorFrame frame = getFrame();
        if(frame!=null)
            textEditorImpl.populateMenus(frame);
    }


    /////////////////////////////////////
    // DocumentListener implementation //
    /////////////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }
	
    public void insertUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    public void removeUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public Insets getInsets() {
        return new Insets(4, 3, 4, 3);
    }

    public void requestFocus() {
        textEditorImpl.getTextArea().requestFocus();
    }
}

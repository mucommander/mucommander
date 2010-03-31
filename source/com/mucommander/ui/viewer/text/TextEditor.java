/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mucommander.AppLogger;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileOperation;
import com.mucommander.ui.viewer.EditorFrame;
import com.mucommander.ui.viewer.FileEditor;


/**
 * A simple text editor. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
class TextEditor extends FileEditor implements DocumentListener {

    private TextEditorImpl textEditorImpl;

    public TextEditor() {
        textEditorImpl = new TextEditorImpl(true);
    }


    ///////////////////////////////
    // FileEditor implementation //
    ///////////////////////////////

    @Override
    protected void saveAs(AbstractFile destFile) throws IOException {
        OutputStream out;

        out = null;

        try {
            out = destFile.getOutputStream();
            textEditorImpl.write(out);

            setSaveNeeded(false);

            // Change the parent folder's date to now, so that changes are picked up by folder auto-refresh (see ticket #258)
            if(destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
                try {
                    destFile.getParent().changeDate(System.currentTimeMillis());
                }
                catch (IOException e) {
                    AppLogger.fine("failed to change the date of "+destFile, e);
                    // Fail silently
                }
            }
        }
        finally {
            if(out != null) {
                try {out.close();}
                catch(IOException e) {
                    // Ignored
                }
            }
        }
    }

    @Override
    public void edit(AbstractFile file) throws IOException {
        textEditorImpl.startEditing(file, this);

        EditorFrame frame = getFrame();
        if(frame!=null)
            textEditorImpl.populateMenus(frame, getMenuBar());
    }
    
    @Override
	public JComponent getViewedComponent() {
		return textEditorImpl.getTextArea();
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
}

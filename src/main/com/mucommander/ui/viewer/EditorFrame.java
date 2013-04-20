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

package com.mucommander.ui.viewer;

import java.awt.Dimension;
import java.awt.Image;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;


/**
 * A specialized <code>JFrame</code> that displays a {@link FileEditor} for a given file and provides some common
 * editing functionalities. The {@link FileEditor} instance is provided by {@link EditorRegistrar}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class EditorFrame extends FileFrame {

    private FileEditor editor;
	
    private final static Dimension MIN_DIMENSION = new Dimension(500, 360);

    /**
     * Creates a new EditorFrame to start viewing the given file.
     *
     * <p>This constructor has package access only, EditorFrame can to be created by
     * {@link EditorRegistrar#createEditorFrame(MainFrame,AbstractFile,Image)}.
     */
    EditorFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        super(mainFrame, file, icon);
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public Dimension getMinimumSize() {
        return MIN_DIMENSION;
    }

    @Override
    public void dispose() {
    	if(editor==null || editor.askSave())   /// Returns true if the file does not have any unsaved change or if the user refused to save the changes
    		super.dispose();
    }
    
    @Override
	protected FilePresenter createFilePresenter(AbstractFile file) throws UserCancelledException {
		return editor = EditorRegistrar.createFileEditor(file, EditorFrame.this);
	}

    @Override
    protected String getGenericErrorDialogTitle() {
    	return Translator.get("file_editor.edit_error_title");
    }

    @Override
    protected String getGenericErrorDialogMessage() {
    	return Translator.get("file_editor.edit_error");
    }
}

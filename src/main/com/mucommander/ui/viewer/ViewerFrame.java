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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;

import java.awt.*;


/**
 * A specialized <code>JFrame</code> that displays a {@link FileViewer} for a given file.
 * The {@link FileViewer} instance is provided by {@link ViewerRegistrar}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ViewerFrame extends FileFrame {
	
    private final static Dimension MIN_DIMENSION = new Dimension(200, 150);
	
    /**
     * Creates a new ViewerFrame to start viewing the given file.
     *
     * <p>This constructor has package access only, ViewerFrame need to be created can
     * {@link ViewerRegistrar#createViewerFrame(MainFrame,AbstractFile,Image)}.
     */
    ViewerFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        super(mainFrame, file, icon);
        
        setMinimumSize(MIN_DIMENSION);
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected void showGenericErrorDialog() {
        InformationDialog.showErrorDialog(getMainFrame(), Translator.get("file_viewer.view_error_title"), Translator.get("file_viewer.view_error"));
    }

	@Override
	protected FilePresenter createFilePresenter(AbstractFile file) throws UserCancelledException {
		return ViewerRegistrar.createFileViewer(file, ViewerFrame.this);
	}
}

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
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFrame;

import java.awt.*;
import java.io.IOException;


/**
 * A simple text viewer. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard
 */
class TextViewer extends FileViewer {

    private TextEditorImpl textEditorImpl;

    public TextViewer() {
        textEditorImpl = new TextEditorImpl(false);

        setLayout(new BorderLayout());
        add(textEditorImpl.getTextArea(), BorderLayout.NORTH);
    }


    ///////////////////////////////
    // FileViewer implementation //
    ///////////////////////////////

    public void view(AbstractFile file) throws IOException {
        textEditorImpl.startEditing(file, null);

        ViewerFrame frame = getFrame();
        if(frame!=null)
            textEditorImpl.populateMenus(frame);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public Insets getInsets() {
        return new Insets(4, 3, 4, 3);
    }

    public long getMaxRecommendedSize() {
        return 1048576;
    }
}

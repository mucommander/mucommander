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

package com.mucommander.ui.text;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.icon.FileIcons;

import javax.swing.*;

/**
 * A simple JLabel that displays information about a file:
 * <ul>
 *  <li>the label's text is set to the file's name or canonical path (specified in the constructor)</li>
 *  <li>the label's icon is set to the file's icon, as returned by {@link FileIcons#getFileIcon(com.mucommander.file.AbstractFile)}</li>
 *  <li>the label's tooltip is set to the file's canonical path, only if the label's text is the file's name</li>
 * </ul>
 *
 * @author Maxence Bernard
 */
public class FileLabel extends JLabel {

    /**
     * Creates a new FileLabel, showing the file's name or full canonical path depending on the value of
     * <code>showFullPath</code>.
     *
     * @param file the file to show
     * @param showFullPath if true, the file's canonical path will be displayed, if false its filename.
     */
    public FileLabel(AbstractFile file, boolean showFullPath) {
        String path = file.getCanonicalPath();

        if(showFullPath) {
            setText(path);
        }
        else {
            setText(file.getName());
            setToolTipText(path);
        }

        setIcon(FileIcons.getFileIcon(file));
    }
}

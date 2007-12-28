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

package com.mucommander.ui.viewer.image;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFactory;

/**
 * <code>ViewerFactory</code> implementation for creating image viewers.
 *
 * @author Nicolas Rinaudo
 */
public class ImageFactory implements ViewerFactory {
    /** Used to filter out file extensions that the image viewer cannot open. */
    private ExtensionFilenameFilter filter;

    public ImageFactory() {
        filter = new ExtensionFilenameFilter(new String[] {".png", ".gif", ".jpg", ".jpeg"});
        filter.setCaseSensitive(false);
    }

    public boolean canViewFile(AbstractFile file) {
        return filter.accept(file);
    }
    public FileViewer createFileViewer() {return new ImageViewer();}
}

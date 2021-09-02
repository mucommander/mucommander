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
package com.mucommander.viewer.binary;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.viewer.FileEditor;
import com.mucommander.viewer.FileEditorService;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.WarnUserException;

/**
 * <code>FileViewerService</code> implementation for creating binary viewers.
 *
 * @author Miroslav Hajda
 */
public class BinaryFileViewerService implements FileViewerService, FileEditorService {

    @Override
    public String getName() {
        return "Binary";
    }

    @Override
    public int getOrderPriority() {
        return 0;
    }

    @Override
    public boolean canViewFile(AbstractFile file) {
        return !file.isDirectory();
    }

    @Override
    public FileViewer createFileViewer() {
        return new BinaryViewer();
    }

    @Override
    public boolean canEditFile(AbstractFile file) throws WarnUserException {
        return !file.isDirectory();
    }

    @Override
    public FileEditor createFileEditor() {
        return new BinaryEditor();
    }
}

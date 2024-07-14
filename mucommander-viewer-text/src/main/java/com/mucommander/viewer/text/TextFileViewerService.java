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
package com.mucommander.viewer.text;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.BinaryDetector;
import com.mucommander.viewer.CanOpen;
import com.mucommander.viewer.FileEditorService;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.viewer.FileEditor;

import java.io.IOException;
import java.io.InputStream;

import com.mucommander.viewer.FileViewer;

/**
 * <code>FileViewerService</code> and <code>FileEditorService</code>
 * implementation for creating text viewers and editors.
 *
 * @author Nicolas Rinaudo
 */
public class TextFileViewerService implements FileViewerService, FileEditorService {

    /**
     * Max file size the editor can open.
     */
    private static final int MAX_FILE_SIZE_FOR_EDIT = 1024*1024;

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public int getOrderPriority() {
        return 10;
    }

    @Override
    public CanOpen canOpenFile(AbstractFile file) {
        // Do not allow directories
        if (file.isDirectory()) {
            return CanOpen.NO;
        }

        // Warn the user if the file looks like a binary file
        InputStream in = null;
        try {
            in = file.getInputStream();
            if (BinaryDetector.guessBinary(in)) {
                return CanOpen.NO;
            }
        } catch (IOException e) {
            // Not much to do
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                }
            }
        }

        // Requires user confirmation that the file is larger than a certain size,
        // and as the whole file is loaded into memory (into JTextArea) it may be very slow or OOM
        if (file.getSize() > MAX_FILE_SIZE_FOR_EDIT) {
            return CanOpen.YES_USER_CONSENT;
        }

        return CanOpen.YES;
    }

    @Override
    public String getConfirmationMsg() {
        return "file_viewer.large_file_warning";
    }

    @Override
    public FileViewer createFileViewer(boolean fromSearchWithContent) {
        return new TextViewer(fromSearchWithContent);
    }

    @Override
    public FileEditor createFileEditor(boolean fromSearchWithContent) {
        return new TextEditor(fromSearchWithContent);
    }
}

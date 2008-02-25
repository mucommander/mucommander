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
import com.mucommander.io.BinaryDetector;
import com.mucommander.text.Translator;
import com.mucommander.ui.viewer.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>ViewerFactory</code> and <code>EditorFactory</code> implementation for creating text viewers and editors.
 *
 * @author Nicolas Rinaudo
 */
public class TextFactory implements ViewerFactory, EditorFactory {

    public boolean canViewFile(AbstractFile file) throws WarnUserException {
        return doGenericChecks(file);
    }

    public boolean canEditFile(AbstractFile file) throws WarnUserException {
        return doGenericChecks(file);
    }

    public FileViewer createFileViewer() {
        return new TextViewer();
    }

    public FileEditor createFileEditor() {
        return new TextEditor();
    }


    private boolean doGenericChecks(AbstractFile file) throws WarnUserException {
        // Do not allow directories
        if(file.isDirectory())
            return false;

        // Warn the user if the file is large that a certain size as the whole file is loaded into memory
        // (in a JTextArea)
        if(file.getSize()>1048576)
            throw new WarnUserException(Translator.get("file_viewer.large_file_warning"));

        // Warn the user if the file looks like a binary file
        InputStream in = null;
        try {
            in = file.getInputStream();
            if(BinaryDetector.guessBinary(in))
                throw new WarnUserException(Translator.get("text_viewer.binary_file_warning"));
        }
        catch(IOException e) {
            // Not much too do
        }
        finally {
            if(in!=null) {
                try { in.close(); } catch(IOException e2) {}
            }
        }

        return true;
    }
}

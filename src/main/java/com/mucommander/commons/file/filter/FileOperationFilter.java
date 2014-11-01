/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;

/**
 * <code>OperationFileFilter</code> matches files which support a specified {@link FileOperation file operation}.
 *
 * <p>Only one file operation can be matched at a time. To match several file operations, combine them using a
 * {@link com.mucommander.commons.file.filter.ChainedFileFilter}.</p>
 *
 * @see FileOperation
 * @author Maxence Bernard
 */
public class FileOperationFilter extends AbstractFileFilter {

    /** The file operation to match */
    private FileOperation op;


    public FileOperationFilter(FileOperation op) {
        this(op, false);
    }

    public FileOperationFilter(FileOperation op, boolean inverted) {
        super(inverted);
        setFileOperation(op);
    }

    /**
     * Returns the file operation this filter matches.
     *
     * @return the file operation this filter matches.
     */
    public FileOperation getFileOperation() {
        return op;
    }

    /**
     * Sets the file operation this filter matches, replacing the previous operation.
     *
     * @param op the file operation this filter matches.
     */
    public void setFileOperation(FileOperation op) {
        this.op = op;
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return file.isFileOperationSupported(op);
    }
}

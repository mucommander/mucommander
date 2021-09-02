/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.viewer;

import com.mucommander.commons.file.AbstractFile;

/**
 * Interface for file editor service.
 *
 * @author Miroslav Hajda
 */
public interface FileEditorService {

    /**
     * Returns name for editor.
     *
     * @return name title
     */
    String getName();

    /**
     * Returns order priority.
     *
     * Reference editors use:<br>
     * 10 - text<br>
     * 0 - binary
     *
     * @return order priority
     */
    int getOrderPriority();

    /**
     * Returns <code>true</code> if this factory can create a file editor for
     * the specified file.
     * <p>
     * The FileEditor may base its decision strictly upon the file's name and
     * its extension or may wish to read some of the file and compare it to a
     * magic number.
     * </p>
     *
     * @param file file for which a editor must be created.
     * @return <code>true</code> if this factory can create a file editor for
     * the specified file.
     * @throws WarnUserException if the specified file can be edited after the
     * warning message contained in the exception is displayed to the end user.
     */
    boolean canEditFile(AbstractFile file) throws WarnUserException;

    /**
     * Returns a new instance of {@link FileEditor}.
     *
     * @return a new instance of {@link FileEditor}.
     */
    FileEditor createFileEditor();
}

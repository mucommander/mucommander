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

/**
 * A common interface for instanciating {@link FileEditor} implementations, and finding out if a editor is capable
 * of editing a particular file.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public interface EditorFactory {
    /**
     * Returns <code>true</code> if this factory can create a file editor for the specified file.
     * <p>
     * The FileEditor may base its decision stricly upon the file's name and its extension or may wish to read some of
     * the file and compare it to a magic number.
     * </p>
     * @param  file file for which a editor must be created.
     * @throws WarnUserException if the specified file can be edited after the warning message contained in the
     * exception is displayed to the end user.
     * @return      <code>true</code> if this factory can create a file editor for the specified file.
     */
    public boolean canEditFile(AbstractFile file) throws WarnUserException;

    /**
     * Returns a new instance of {@link FileEditor}.
     * @return a new instance of {@link FileEditor}.
     */
    public FileEditor createFileEditor();
}

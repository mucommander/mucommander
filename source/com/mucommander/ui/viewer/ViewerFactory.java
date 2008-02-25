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

package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

/**
 * A common interface for instanciating {@link FileViewer} implementations, and finding out if a viewer is capable
 * of viewing a particular file.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public interface ViewerFactory {
    /**
     * Returns <code>true</code> if this factory can create a file viewer for the specified file.
     * <p>
     * The FileEditor may base its decision stricly upon the file's name and its extension or may wish to read some of
     * the file and compare it to a magic number.
     * </p>
     * @param  file file for which a viewer must be created.
     * @throws WarnUserException if the specified file can be viewed after the warning message contained in the
     * exception is displayed to the end user.
     * @return      <code>true</code> if this factory can create a file viewer for the specified file.
     */
    public boolean canViewFile(AbstractFile file) throws WarnUserException;

    /**
     * Returns a new instance of {@link FileViewer}.
     * @return a new instance of {@link FileViewer}.
     */
    public FileViewer createFileViewer();
}

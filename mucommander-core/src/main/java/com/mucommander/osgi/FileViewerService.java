/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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
package com.mucommander.osgi;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.viewer.FileViewerWrapper;
import com.mucommander.viewer.WarnUserException;

/**
 * Interface for file viewer service.
 *
 * @author Miroslav Hajda
 */
public interface FileViewerService {

    /**
     * Returns tab title for viewer.
     *
     * @return tab title
     */
    String getTabTitle();

    /**
     * Returns order priority.
     *
     * Reference viewers use:<br>
     * 20 - image<br>
     * 10 - text<br>
     * 0 - binary
     *
     * @return order priority
     */
    int getOrderPriority();

    /**
     * Returns <code>true</code> if this factory can create a file viewer for
     * the specified file.
     * <p>
     * The FileEditor may base its decision strictly upon the file's name and
     * its extension or may wish to read some of the file and compare it to a
     * magic number.
     * </p>
     *
     * @param file file for which a viewer must be created.
     * @return <code>true</code> if this factory can create a file viewer for
     * the specified file.
     */
    boolean canViewFile(AbstractFile file) throws WarnUserException;

    /**
     * Returns a new instance of {@link FileViewer2}.
     *
     * @return a new instance of {@link FileViewer2}.
     */
    FileViewerWrapper createFileViewer();
}

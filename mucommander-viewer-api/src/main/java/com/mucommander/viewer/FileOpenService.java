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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for file editor & viewer services.
 */
@ParametersAreNonnullByDefault
public interface FileOpenService {

    /**
     * Returns name for editor/viewer.
     *
     * @return name title
     */
    @Nonnull
    String getName();

    /**
     * Returns order priority.
     *
     * Reference editors/viewers use:<br>
     * 10 - text<br>
     * 0 - binary
     *
     * @return order priority
     */
    int getOrderPriority();

    /**
     * Returns <code>CanOpen</code> if this factory can create a file editor for the specified file.
     * <p>
     * The FileEditor/FileViewer may base its decision strictly upon the file's name and its extension or may wish to read some of
     * the file and compare it to a magic number.
     * </p>
     *
     * @param file
     *            file for which an editor or viewer should be created.
     * @return <code>CanOpen</code> if this factory can create a file editor for the specified file.
     */
    CanOpen canOpenFile(AbstractFile file);

    /**
     * Returns a text message (a key in dictionary) to be displayed if #canOpenFile returns
     * CanOpen.YES_WITH_CONFIRMATION. May return null if CanOpen.YES_WITH_CONFIRMATION is never returned.
     * @return the message (dictionary key), can be null
     */
    default String getConfirmationMsg() { return null; };
}

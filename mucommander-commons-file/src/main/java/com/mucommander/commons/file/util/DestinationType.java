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

package com.mucommander.commons.file.util;

/**
 * @author Arik Hadas
 */
public enum DestinationType {
    /** Designates a folder, either a directory or archive, that exists on the filesystem. */
    EXISTING_FOLDER,
    /** Designates a regular file that exists on the filesystem. The file may be a browsable archive but that was
     * refered to as a regular file, i.e. without a trailing separator character in the path. */
    EXISTING_FILE,
    /** Designates a new file that doesn't exist on the filesystem. The file's parent however does always exist. */
    NEW_FILE;
}

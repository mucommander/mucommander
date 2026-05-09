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


package dev.barebones.commander.commons.file.protocol;

import dev.barebones.commander.commons.file.FileURL;

/**
 * This interface contains a set of known protocol names, that can be found in {@link FileURL}. 
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
public interface FileProtocols {

    /** Protocol for local or locally mounted files. */
    String FILE = "file";

    /** Protocol for files served by an NFS server. */
    String NFS = "nfs";

    /** Protocol for files served by an Amazon S3 (or S3-compatible) server. */
    String S3 = "s3";

    /** Protocol for files served by an SFTP server. */
    String SFTP = "sftp";

    /** Protocol for files served by a SMB/CIFS server. The backend was
     *  removed but the constant survives so legacy bookmarks pointing at
     *  smb:// URLs still parse without throwing. */
    String SMB = "smb";

}

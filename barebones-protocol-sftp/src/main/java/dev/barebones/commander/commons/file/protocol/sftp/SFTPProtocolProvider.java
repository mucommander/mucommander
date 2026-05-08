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


package dev.barebones.commander.commons.file.protocol.sftp;

import java.io.IOException;
import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;

/**
 * This class is the provider for the FTP filesystem implemented by {@link dev.barebones.commander.commons.file.protocol.ftp.FTPFile}.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see dev.barebones.commander.commons.file.protocol.sftp.SFTPFile
 */
public class SFTPProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        return instantiationParams.isEmpty()
            ?new SFTPFile(url)
            :new SFTPFile(url, (SFTPFile.SFTPFileAttributes)instantiationParams.get("attributes"));
    }
}

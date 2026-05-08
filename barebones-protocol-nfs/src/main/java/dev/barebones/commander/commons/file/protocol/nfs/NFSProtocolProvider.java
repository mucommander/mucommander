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


package dev.barebones.commander.commons.file.protocol.nfs;

import java.io.IOException;
import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;

/**
 * This class is the provider for the NFS filesystem implemented by {@link dev.barebones.commander.commons.file.protocol.nfs.NFSFile}.
 *
 * @author Nicolas Rinaudo
 * @see dev.barebones.commander.commons.file.protocol.nfs.NFSFile
 */
public class NFSProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        return new NFSFile(url);
    }
}

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


package dev.barebones.commander.commons.file.protocol.local;

import java.io.IOException;
import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;
import dev.barebones.commander.commons.runtime.OsFamily;

/**
 * This class is the provider for the local filesystem implemented by {@link dev.barebones.commander.commons.file.protocol.local.LocalFile}
 * and network path given in UNC format which is implemented by {@link dev.barebones.commander.commons.file.protocol.local.UNCFile}
 *
 * @author Maxence Bernard, Arik Hadas
 * @see dev.barebones.commander.commons.file.protocol.local.LocalFile
 * @see dev.barebones.commander.commons.file.protocol.local.UNCFile
 */
public class LocalProtocolProvider implements ProtocolProvider {

	/** Are we running Windows ? */
    private final static boolean IS_WINDOWS =  OsFamily.WINDOWS.isCurrent();
	
    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        return isUncFile(url)
                ?(instantiationParams.isEmpty()?new UNCFile(url):new UNCFile(url ,(java.io.File)instantiationParams.get("createdFile")))
                :(instantiationParams.isEmpty()?new LocalFile(url):new LocalFile(url, (java.io.File)instantiationParams.get("createdFile")));
    }
	
	/**
     * Returns <code>true</code> if the specified {@link FileURL} denotes a Windows UNC file.
     *
     * @param fileURL the {@link FileURL} to test
     * @return <code>true</code> if the specified {@link FileURL} denotes a Windows UNC file.
     */
    private static boolean isUncFile(FileURL fileURL) {
        return IS_WINDOWS && !FileURL.LOCALHOST.equals(fileURL.getHost());
    }
}

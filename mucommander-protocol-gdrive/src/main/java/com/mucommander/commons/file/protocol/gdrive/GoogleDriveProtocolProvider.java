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

package com.mucommander.commons.file.protocol.gdrive;

import java.io.IOException;
import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.ProtocolProvider;

/**
 * @author Arik Hadas
 */
public class GoogleDriveProtocolProvider implements ProtocolProvider {

	/////////////////////////////////////
	// ProtocolProvider Implementation //
	/////////////////////////////////////

	public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
	    String path = url.getPath();
	    switch(path) {
	    case "/":
	        return new GoogleDriveRoot(url);
	    default:
	        return new GoogleDriveFile(url);
	    }
	}
}

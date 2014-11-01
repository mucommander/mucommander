/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.file.impl.vsphere;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;

import java.io.IOException;

/**
 * This class is the provider for the VSphere filesystem implemented by
 * {@link com.mucommander.commons.file.impl.vsphere.VSphereFile}.
 * 
 * @author Yuval Kohavi <yuval.kohavi@intigua.com>
 * @see com.mucommander.commons.file.impl.vsphere.VSphereFile
 */
public class VSphereProtocolProvider implements ProtocolProvider {

	// ///////////////////////////////////
	// ProtocolProvider Implementation //
	// ///////////////////////////////////

	public AbstractFile getFile(FileURL url, Object... instantiationParams)
			throws IOException {
		return new VSphereFile(url);
	}
}

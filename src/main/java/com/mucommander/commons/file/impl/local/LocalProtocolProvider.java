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


package com.mucommander.commons.file.impl.local;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import com.mucommander.commons.runtime.OsFamily;

import java.io.IOException;

/**
 * This class is the provider for the local filesystem implemented by {@link com.mucommander.commons.file.impl.local.LocalFile}
 * and network path given in UNC format which is implemented by {@link com.mucommander.commons.file.impl.local.UNCFile}
 *
 * @author Maxence Bernard, Arik Hadas
 * @see com.mucommander.commons.file.impl.local.LocalFile
 * @see com.mucommander.commons.file.impl.local.UNCFile
 */
public class LocalProtocolProvider implements ProtocolProvider {

	/** Are we running Windows ? */
    private final static boolean IS_WINDOWS =  OsFamily.WINDOWS.isCurrent();
	
    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return isUncFile(url)?
        	 (instantiationParams.length==0?new UNCFile(url):new UNCFile(url ,(java.io.File)instantiationParams[0]))
        	:(instantiationParams.length==0?new LocalFile(url):new LocalFile(url, (java.io.File)instantiationParams[0]));
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

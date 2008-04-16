/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.sftp;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;
import com.mucommander.runtime.JavaVersions;

import java.io.IOException;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class SFTPProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url) throws IOException {return new SFTPFile(url);}

    /**
     * Returns <code>true</code> if SFTP support is available under the current runtime.
     * SFTP support currently requires Java 1.5 or higher.
     *
     * @return <code>true</code> if SFTP support is available under the current runtime
     */
    public static boolean isAvailable() {
        return JavaVersions.JAVA_1_5.isCurrentOrHigher();
    }
}

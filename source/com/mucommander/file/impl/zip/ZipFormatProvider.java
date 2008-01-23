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

package com.mucommander.file.impl.zip;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveFormatProvider;

import java.io.IOException;

/**
 * File provider used to create instances of {@link ZipArchiveFile}.
 * @author Nicolas Rinaudo
 */
public class ZipFormatProvider implements ArchiveFormatProvider {
    /**
     * Creates a new instance of {@link ZipFormatProvider}.
     * @param  file URL to map as an {@link ZipFormatProvider}.
     * @return      a new instance of {@link ZipFormatProvider} that matches the specified URL.
     */
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {return new ZipArchiveFile(file);}
}

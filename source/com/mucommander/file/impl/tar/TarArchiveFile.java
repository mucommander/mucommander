/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.file.impl.tar;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


/**
 * TarArchiveFile provides read-only access to archives in the Tar/Tgz format.
 *
 * <p>Tar support is provided by the <code>Apache Ant</code> library distributed in the Apache Software License.
 *
 * @author Maxence Bernard
 */
public class TarArchiveFile extends AbstractROArchiveFile {

    /**
     * Creates a TarArchiveFile around the given file.
     */
    public TarArchiveFile(AbstractFile file) {
        super(file);
    }


    /**
     * Returns a TarInputStream which can be used to read TAR entries.
     */
    private TarInputStream createTarStream() throws IOException {
        InputStream inputStream = file.getInputStream();

        String name = getName().toLowerCase();
            // Gzip-compressed file
        if(name.endsWith("tgz") || name.endsWith("tar.gz"))
                // Note: this will fail for gz/tgz entries inside a tar file (IOException: Not in GZIP format),
                // why is a complete mystery: the gz/tgz entry can be extracted and then properly browsed
            inputStream = new GZIPInputStream(inputStream);

        // Bzip2-compressed file
        else if(name.endsWith("tbz2") || name.endsWith("tar.bz2")) {
            try { inputStream = new CBZip2InputStream(inputStream); }
            catch(Exception e) {
                // CBZip2InputStream is known to throw NullPointerException if file is not properly Bzip2-encoded
                // so we need to catch those and throw them as IOException
                if(com.mucommander.Debug.ON)
                    com.mucommander.Debug.trace("Exception caught while creating CBZip2InputStream: "+e+", throwing IOException");

                throw new IOException();
            }
        }

        return new TarInputStream(inputStream);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////
	
    public Vector getEntries() throws IOException {
        // Note: JavaTar's FastTarStream can unfortunately not be used
        // because it fails on many tar files that TarInputStream can read
        // without any problem.
        TarInputStream tin = createTarStream();

        // Load TAR entries
        Vector entries = new Vector();
        org.apache.tools.tar.TarEntry entry;
        while ((entry=tin.getNextEntry())!=null) {
            entries.add(new TarEntry(entry));
        }
        tin.close();

        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        TarInputStream tin = createTarStream();
        org.apache.tools.tar.TarEntry tempEntry;
        String entryPath = entry.getPath();
        while ((tempEntry=tin.getNextEntry())!=null) {
            if (tempEntry.getName().equals(entryPath))
                return tin;
        }

        return null;
    }
}

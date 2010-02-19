/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.file.impl.rar;

import com.mucommander.file.*;
import com.mucommander.file.impl.rar.provider.RarFile;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


/**
 * RarArchiveFile provides read-only access to archives in the Rar format.
 *
 * @see com.mucommander.file.impl.rar.RarFormatProvider
 * @author Arik Hadas
 */
public class RarArchiveFile extends AbstractROArchiveFile {

	/** The RarFile object that actually reads the entries in the Rar file */
	private RarFile rarFile;
	
	/** The date at which the current RarFile object was created */
	private long lastRarFileDate;	
	
    
	public RarArchiveFile(AbstractFile file) throws IOException {		
		super(file);
	}
	
	/**
     * Checks if the underlying Rar file is up-to-date, i.e. exists and has not changed without this archive file
     * being aware of it. If one of those 2 conditions are not met, (re)load the RipFile instance (parse the entries)
     * and declare the Rar file as up-to-date.
     *
     * @throws IOException if an error occurred while reloading
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    private void checkRarFile() throws IOException, UnsupportedFileOperationException {
        long currentDate = file.getDate();
        
        if (rarFile==null || currentDate != lastRarFileDate) {
        	rarFile = new RarFile(file);
            declareRarFileUpToDate(currentDate);
        }
    }
    
    /**
     * Declare the underlying Rar file as up-to-date. Calling this method after the Rar file has been
     * modified prevents {@link #checkRarFile()} from being reloaded.
     */
    private void declareRarFileUpToDate(long currentFileDate) {
        lastRarFileDate = currentFileDate;
    }
    
    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader}
     *
     * @param header the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given FileHeader
     */
    private ArchiveEntry createArchiveEntry(FileHeader header) {
    	return new ArchiveEntry(
    			header.getFileNameString().replace('\\', '/'),
    			header.isDirectory(),
    			header.getMTime().getTime(),
    			header.getFullUnpackSize(),
                true
    	);
    }

    
    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////
    
    @Override
    public synchronized ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
        checkRarFile();

        Vector<ArchiveEntry> entries = new Vector<ArchiveEntry>();
        for (Object o : rarFile.getEntries())
            entries.add(createArchiveEntry((FileHeader)o));

        return new WrapperArchiveEntryIterator(entries.iterator());
    }

    @Override
    public synchronized InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
		checkRarFile();
		
		return rarFile.getEntryInputStream(entry.getPath().replace('/', '\\'));
	}
}

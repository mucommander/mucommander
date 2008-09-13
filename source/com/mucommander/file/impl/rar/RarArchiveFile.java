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

package com.mucommander.file.impl.rar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import rar.ExternalFile;
import rar.RarEntry;
import rar.RarFile;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;

/**
 * RarArchiveFile provides read-only access to archives in the Rar format.
 *
 * @see com.mucommander.file.impl.rar.RarFormatProvider
 * @author Arik Hadas
 */
public class RarArchiveFile extends AbstractROArchiveFile {
	private RarFile m_rar;

	public RarArchiveFile(AbstractFile file) throws IOException {		
		super(file);

		final AbstractFile f = file;
		m_rar = new RarFile(new ExternalFile(){

			public InputStream getInputStream() throws IOException {
				return f.getInputStream();
			}
			
		});
	}
	
	public Vector getEntries() throws IOException {
		Vector result = new Vector();
		Iterator iter = m_rar.getEntries().iterator();
		while (iter.hasNext()) {
			RarEntry file = (RarEntry) iter.next();
			//System.out.println("adding: " + file.getPath());
			result.add(convertRarEntryToArchiveEntry(file));
		}
		return result;
	}
	
	private ArchiveEntry convertRarEntryToArchiveEntry(RarEntry rarEntry) {
		return new ArchiveEntry(rarEntry.getPath().replace('\\', '/'), rarEntry.isDirectory(), rarEntry.getDate().getTime(), rarEntry.getSize());
	}
	
	public InputStream getEntryInputStream(ArchiveEntry entry)
			throws IOException {
		//System.out.println("arik path = " + entry.getPath());
		//System.out.println("arik name = " + entry.getName());
		return m_rar.extract(entry.getPath().replace('/', '\\'));	
	}
}

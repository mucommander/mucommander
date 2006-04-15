package com.mucommander.file;

import java.io.*;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class Bzip2ArchiveFile extends AbstractArchiveFile {

	private SingleEntry entries[];

	/**
	 * Creates a BzipArchiveFile on top of the given file.
	 */
	public Bzip2ArchiveFile(AbstractFile file) {
		super(file);
	}


	////////////////////////////////////////
	// AbstractArchiveFile implementation //
	////////////////////////////////////////
	
	protected ArchiveEntry[] getEntries() throws IOException {
		if(this.entries==null) {
			String extension = getExtension();
			String name = getName();
			
			if(extension!=null) {
				extension = extension.toLowerCase();
				
				// Remove the 'bz2' or 'tbz2' extension from the entry's name
				if(extension.equals("tbz2"))
					name = name.substring(0, name.length()-4)+"tar";
				else if(extension.equals("bz2"))
					name = name.substring(0, name.length()-4);
			}

			this.entries = new SingleEntry[]{new SingleEntry("/"+name, getDate(), -1)};
		}
	
		return this.entries;
	}


	InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
		return new CBZip2InputStream(getInputStream());
	}
}
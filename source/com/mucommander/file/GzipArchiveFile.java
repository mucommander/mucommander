package com.mucommander.file;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractArchiveFile {

	private SingleEntry entries[];

	/**
	 * Creates a GzipArchiveFile on top of the given file.
	 */
	public GzipArchiveFile(AbstractFile file) {
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
				
				// Remove the 'gz' or 'tgz' extension from the entry's name
				if(extension.equals("tgz"))
					name = name.substring(0, name.length()-3)+"tar";
				else if(extension.equals("gz"))
					name = name.substring(0, name.length()-3);
			}

			this.entries = new SingleEntry[]{new SingleEntry("/"+name, getDate(), -1)};
		}
		
		return this.entries;
	}


	InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
		return new GZIPInputStream(getInputStream());
	}
}
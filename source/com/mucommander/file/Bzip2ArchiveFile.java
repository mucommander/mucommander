package com.mucommander.file;

import java.io.*;
import org.apache.tools.bzip2.CBZip2InputStream;
import java.util.Vector;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class Bzip2ArchiveFile extends AbstractArchiveFile {

    /**
     * Creates a BzipArchiveFile on top of the given file.
     */
    public Bzip2ArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////
	
    protected Vector getEntries() throws IOException {
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

        Vector entries = new Vector();
        entries.add(new SimpleEntry("/"+name, getDate(), -1, false));
        return entries;
    }


    InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        return new CBZip2InputStream(getInputStream());
    }
}

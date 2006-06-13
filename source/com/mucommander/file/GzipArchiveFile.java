package com.mucommander.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractArchiveFile {

    /**
     * Creates a GzipArchiveFile on top of the given file.
     */
    public GzipArchiveFile(AbstractFile file) {
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
			
            // Remove the 'gz' or 'tgz' extension from the entry's name
            if(extension.equals("tgz"))
                name = name.substring(0, name.length()-3)+"tar";
            else if(extension.equals("gz"))
                name = name.substring(0, name.length()-3);
        }

        Vector entries = new Vector();
        entries.add(new SimpleEntry("/"+name, getDate(), -1, false));
        return entries;
    }


    InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        return new GZIPInputStream(getInputStream());
    }
}

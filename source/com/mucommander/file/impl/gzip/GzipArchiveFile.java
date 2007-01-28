package com.mucommander.file.impl.gzip;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.SimpleArchiveEntry;

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
	
    public Vector getEntries() throws IOException {
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
        entries.add(new SimpleArchiveEntry("/"+name, getDate(), -1, false));
        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        return new GZIPInputStream(getInputStream());
    }
}

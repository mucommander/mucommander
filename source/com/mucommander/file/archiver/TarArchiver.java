
package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import org.apache.tools.tar.TarOutputStream;
import org.apache.tools.tar.TarEntry;

import java.io.OutputStream;
import java.io.IOException;


/**
 * Archiver implementation using the Tar archive format.
 *
 * @author Maxence Bernard
 */
class TarArchiver extends Archiver {

    private TarOutputStream tos;
    private boolean firstEntry = true;

    protected TarArchiver(OutputStream outputStream) {
        this.tos = new TarOutputStream(outputStream);
        // Specify how to handle files whose name is > 100 chars (default is to fail!)
        this.tos.setLongFileMode(TarOutputStream.LONGFILE_GNU);
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
        // Start by closing current entry
        if(!firstEntry)
            tos.closeEntry();

        boolean isDirectory = file.isDirectory();
		
        // Create the entry
        TarEntry entry = new TarEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size (required by TarOutputStream) and date
        if(!isDirectory)		// Do not set size if file is directory!
            entry.setSize(file.getSize());
        entry.setModTime(file.getDate());

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating entry, name="+entry.getName()+" isDirectory="+entry.isDirectory()+" size="+entry.getSize()+" modTime="+entry.getModTime());
		
        // Add the entry
        tos.putNextEntry(entry);

        if(firstEntry)
            firstEntry = false;
	
        // Return the OutputStream that allows to write to the entry, only if it isn't a directory 
        return isDirectory?null:tos;
    }


    public void close() throws IOException {
        // Close current entry
        if(!firstEntry)
            tos.closeEntry();
		
        tos.close();
    }
}


package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import java.io.IOException;
import java.io.OutputStream;


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
        long size = file.getSize();
        if(!isDirectory && size>=0)		// Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        entry.setModTime(file.getDate());

        int perms = entry.getMode();
        perms = AbstractFile.setPermissionBit(perms, AbstractFile.READ_MASK, file.canRead());
        perms = AbstractFile.setPermissionBit(perms, AbstractFile.WRITE_MASK, file.canWrite());
        perms = AbstractFile.setPermissionBit(perms, AbstractFile.EXECUTE_MASK, file.canExecute());
        entry.setMode(perms);
        
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

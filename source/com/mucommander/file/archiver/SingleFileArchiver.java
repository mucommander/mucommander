
package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Generic single file Archiver.
 *
 * @author Maxence Bernard
 */
class SingleFileArchiver extends Archiver {

    private OutputStream outputStream;
    private boolean firstEntry = true;


    protected SingleFileArchiver(OutputStream outputStream) {
        this.outputStream = outputStream;
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    /**
     * This method is a no-op, and does nothing but throw an IOException if it is called more than once,
     * which should never be the case as this Archiver is only meant to store one file. 
     */
    public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
        if(firstEntry)
            firstEntry = false;
        else
            throw new IOException();

        return outputStream;
    }
	
	
    public void close() throws IOException {
        outputStream.close();
    }
}

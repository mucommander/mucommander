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

package com.mucommander.file.impl.rar.provider;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileLogger;
import com.mucommander.file.UnsupportedFileOperationException;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.Archive;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception.RarException;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception.RarExceptionType;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader;
import com.mucommander.io.FailSafePipedInputStream;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 
 * @author Arik Hadas
 */
public class RarFile {
	
	/** The underlying archive file */
    private AbstractFile file;
        
    /** Interface to junrar library */
    private Archive archive;
    
    /** Executor that performs the extraction process from junrar package */
    private static final Executor threadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "RAR extractor thread");
            t.setDaemon(true);
            return t;
        }
    });
    
    public RarFile(AbstractFile file) throws IOException, UnsupportedFileOperationException {
    	this.file = file;
    	RarDebug.trace("RAR: creating rar archive for \"" + file.getAbsolutePath() +"\"");
    	InputStream fileIn = file.getInputStream();
        try {
            archive = new Archive(fileIn);
        }
        finally {
            fileIn.close();
        }
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public Collection getEntries() {
    	return archive.getFileHeaders();
    }
    
    public InputStream getEntryInputStream(String path) throws IOException {
    	final FileHeader header = archive.getFileHeader(path);

    	// If the file that is going to be extracted is divided and continued in another archive 
        // part - don't extract it and throw corresponding exception to raise an error. 
        if (header.isSplitAfter())
    		throw new RarException(RarExceptionType.mvNotImplemented);
    	
		final FailSafePipedInputStream in = new FailSafePipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);

		threadPool.execute(new Runnable() {
			public void run() {
		        BufferedOutputStream bufferStream = null;
                InputStream fileIn1 = null;
                InputStream fileIn2 = null;
                try {
		            bufferStream = new BufferedOutputStream(out);
		            boolean isSolid = header.isSolid();
		            fileIn1 = file.getInputStream();
                    fileIn2 = isSolid ? file.getInputStream() : null;

                    archive.extractEntry(isSolid, header, fileIn1, bufferStream, fileIn2);
		            // flush remaining output
		            bufferStream.flush();
		        } catch (Exception ex) {
		        	RarDebug.trace("RAR: got error while extracting entry \"" + header.getFileNameString() +"\": " + ex.getMessage());

                    // Have the PipedInputStream throw this exception when returning from a blocking method (e.g. read() or close())
                    // or the next time one is called.
                    // If the Exception is an IOException instance, use it directly. If it isn't, create a new
                    // IOException with the original message.
                    in.setExternalFailure(ex instanceof IOException?(IOException)ex:new IOException(ex.getMessage()));

                    // Its expected for the reader to close the stream...
		            // wrap and throw (the executor creates a new thread on error)
		            if(ex.getCause() != null && !ex.getCause().getLocalizedMessage().equals("Pipe closed"))
		                throw new RuntimeException(ex);
		        } finally {
		            // Close this stream otherwise the reader can await eternally
		            close(bufferStream);

                    // Close the file inputstream(s)
                    close(fileIn1);
                    close(fileIn2);
                }
		    }
		});
		
		return in;
    }

    /**    
     * Convenient way to close a Closeable object.
     *  
     * @param closeable object.
     */
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ex) {
            	FileLogger.fine("couldn't close a closable object of type \"" + closeable.getClass().getName() + "\"");
            }
        }
    }


}

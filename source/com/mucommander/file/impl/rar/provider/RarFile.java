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

package com.mucommander.file.impl.rar.provider;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.Archive;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception.RarException;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader;

/**
 * 
 * @author Arik Hadas
 */
public class RarFile {
	
	/** The underlying archive file */
    private AbstractFile file;
        
    /** The interface to junrar library */
    private Archive archive;
    
    private static final Executor threadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "rar extractor");
            t.setDaemon(true);
            return t;
        }
    });
    
    public RarFile(AbstractFile file) throws IOException {
    	this.file = file;
    	
		try {
			archive = new Archive(file.getInputStream());
		} catch (RarException e) {
			throw new IOException();
		}
		
		parseFile();
    }
    
    private void parseFile() throws IOException {
    	
    }
    
    public Collection getEntries() throws IOException {    	
		return archive.getFileHeaders();
    }
    
    public InputStream getEntryInputStream(String path) throws IOException {
    	final FileHeader header = archive.getFileHeader(path); //(FileHeader) nameMap.get(path);
    	
		final PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out;

		try {
		    out = new PipedOutputStream(in);
		} catch (IOException ex) {
		    throw new IOException("ERROR: Couldn't connect the pipes");
		}

		//Each inner runnable holds a reference to this so the rarArchiveFile is only
		//finallzed when all of them are gc - close the ArchiveFile then
		threadPool.execute(new Runnable() {
			public void run() {
		        BufferedOutputStream bufferStream = null;
		        try {
		            bufferStream = new BufferedOutputStream(out);
		            boolean isSolid = header.isSolid();
		            archive.extractEntry(isSolid, header, file.getInputStream(), bufferStream, isSolid ? file.getInputStream() : null);
		            //flush remaining output
		            bufferStream.flush();
		            
		        } catch (Exception ex) {
		        	System.out.println("exception: " + ex.getMessage() + " for " + header.getFileNameString());
		        	ex.printStackTrace();		        	
		        	close(in);
					
		            //Its expected for the reader to close the stream...
		            //wrap and throw (the executor creates a new thread on error)
		            if(ex.getCause() != null && !ex.getCause().getLocalizedMessage().equals("Pipe closed"))
		                throw new RuntimeException(ex);
		        } finally {
		            //Close this stream otherwise the reader can await eternally
		            close(bufferStream);
		        }
		    }
		});
		
		return in;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ex) {
            	System.out.println("error closing closable");
            }
        }
    }
}

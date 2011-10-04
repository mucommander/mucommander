/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.impl.sevenzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractROArchiveFile;
import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.ArchiveEntryIterator;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.WrapperArchiveEntryIterator;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.IInArchive;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZipEntry;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip.Handler;
import com.mucommander.commons.util.CircularByteBuffer;


/**
 * SevenZipArchiveFile provides read access to archives in the 7zip format.
 *
 * @author Arik Hadas, Maxence Bernard
 */
public class SevenZipArchiveFile extends AbstractROArchiveFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipArchiveFile.class);

    private IInArchive sevenZipFile;
	
	public SevenZipArchiveFile(AbstractFile file) throws IOException {		
		super(file);
	}
	
	private IInArchive openSevenZipFile() throws IOException {
		if (sevenZipFile == null) {
			MuRandomAccessFile in = new MuRandomAccessFile(file);
//			MyRandomAccessFile in = new MyRandomAccessFile(file.getAbsolutePath(), "rw");
			sevenZipFile = new Handler();
			if (sevenZipFile.Open(in) != 0)
				throw new IOException("Error while opening 7zip archive " + file.getAbsolutePath());
		}
        return sevenZipFile;
    }
    
    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link SevenZipEntry}
     *
     * @param entry the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given SevenZipEntry
     */
    private ArchiveEntry createArchiveEntry(SevenZipEntry entry) {
		return new ArchiveEntry(entry.getName(), entry.isDirectory(), entry.getTime(), entry.getSize(), true);
	}

    
    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////

    @Override
    public InputStream getEntryInputStream(final ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
		final IInArchive sevenZipFile = openSevenZipFile();
		
/*		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);

//		threadPool.execute(new Runnable() {
//			public void run() {
//		        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
		        try {
		        	int arrays []  = new int[1];
		            for(int i = 0 ; i < sevenZipFile.size() ; i++) {
//		                	System.out.println("check " + sevenZipFile.getEntry(i).getName());
		                if (entry.getPath().equals(sevenZipFile.getEntry(i).getName())) {
		                	System.out.println("entry.getPath = " + entry.getPath() + ", sevenZipFile.getEntry(i).getName() " + sevenZipFile.getEntry(i).getName());
		                    arrays[0] = i;
		                    break;
		                }
		            }
		        	
					MuArchiveExtractCallback extractCallbackSpec = new MuArchiveExtractCallback(os);//, entry.getPath());
			        extractCallbackSpec.Init(sevenZipFile);
			        try {
			        	sevenZipFile.Extract(arrays, 1, IInArchive.NExtract_NAskMode_kExtract , extractCallbackSpec);
			        }
			        catch (Exception e) {
			        	e.printStackTrace();
//			        	return;
			        }
//			        sevenZipFile.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract , extractCallbackSpec);
			        try
			        {
			        Thread.sleep(1000); // do nothing for 1000 miliseconds (1 second)
			        }
			        catch(InterruptedException e)
			        {
			        e.printStackTrace();
			        }
			        
			        System.out.println("stopped");
					
//			        bufferedOut.flush();
				}
                catch (Exception e) {
                    LOGGER.info("Error while retrieving 7zip entry {}", e);
                    System.out.println("Error while retrieving 7zip entry {}");
				}
                finally {
//                    try { bufferedOut.close(); }
//                    catch(IOException e) {
//                        // Not much we can do about it
//                    	e.printStackTrace();
//                    }

                    try { in.close(); }
                    catch(IOException e) {
                        // Not much we can do about it
                    	e.printStackTrace();
                    }

                    try { sevenZipFile.close(); }
                    catch(IOException e) {
                        // Not much we can do about it
                    	e.printStackTrace();
                    }
                }
//			}
//		});
		
		return new ByteArrayInputStream(os.toByteArray()); */
		
		final int arrays []  = new int[1];
        for(int i = 0 ; i < sevenZipFile.size() ; i++) {
//            	System.out.println("check " + sevenZipFile.getEntry(i).getName());
            if (entry.getPath().equals(sevenZipFile.getEntry(i).getName())) {
            	System.out.println("entry.getPath = " + entry.getPath() + ", sevenZipFile.getEntry(i).getName() " + sevenZipFile.getEntry(i).getName());
                arrays[0] = i;
                break;
            }
        }
    	
		final CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
		  new Thread(
		    new Runnable(){
		      public void run(){
		        
		        MuArchiveExtractCallback extractCallbackSpec = new MuArchiveExtractCallback(cbb.getOutputStream(), entry.getPath());
		        extractCallbackSpec.Init(sevenZipFile);
		        try {
		        	sevenZipFile.Extract(arrays, 1, IInArchive.NExtract_NAskMode_kExtract , extractCallbackSpec);
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
//		        	return;
		        }
		      }
		    }
		  ).start();
		  
		  return cbb.getInputStream();
	}

	@Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
		final IInArchive sevenZipFile = openSevenZipFile();

        try {
            int nbEntries = sevenZipFile.size();
            Vector<ArchiveEntry> entries = new Vector<ArchiveEntry>();
            for(int i = 0; i <nbEntries ; i++)
                entries.add(createArchiveEntry(sevenZipFile.getEntry(i)));

            return new WrapperArchiveEntryIterator(entries.iterator());
        }
        finally {
            /*try { sevenZipFile.close(); }
            catch(IOException e) {
                // Not much we can do about it
            }*/
        }
	}
}

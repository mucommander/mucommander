package com.mucommander.file.impl.sevenzip;

import SevenZip.Archive.IInArchive;
import SevenZip.Archive.SevenZip.Handler;
import SevenZip.Archive.SevenZipEntry;
import com.mucommander.file.*;
import com.mucommander.io.FailSafePipedInputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * SevenZipArchiveFile provides read access to archives in the 7zip format.
 *
 * @author Arik Hadas, Maxence Bernard
 */
public class SevenZipArchiveFile extends AbstractROArchiveFile {

	private static final Executor threadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "SevenZip extractor thread");
            t.setDaemon(true);
            return t;
        }
    });
	
	public SevenZipArchiveFile(AbstractFile file) throws IOException {		
		super(file);
	}
	
    private IInArchive openSevenZipFile() throws IOException {
        MuRandomAccessFile in = new MuRandomAccessFile(file);
        IInArchive sevenZipFile = new Handler();
        if (sevenZipFile.Open(in) != 0)
            throw new IOException("Error while opening 7zip archive " + file.getAbsolutePath());

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
		
		final FailSafePipedInputStream in = new FailSafePipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);

		threadPool.execute(new Runnable() {
			public void run() {
		        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
		        try {
					MuArchiveExtractCallback extractCallbackSpec = new MuArchiveExtractCallback(bufferedOut, entry.getPath());
			        extractCallbackSpec.Init(sevenZipFile);
			        sevenZipFile.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract , extractCallbackSpec);
			        
					bufferedOut.flush();
				}
                catch (Exception e) {
                    FileLogger.fine("Error while retrieving 7zip entry "+entry.getName(), e);
				}
                finally {
                    try { bufferedOut.close(); }
                    catch(IOException e) {
                        // Not much we can do about it
                    }

                    try { in.close(); }
                    catch(IOException e) {
                        // Not much we can do about it
                    }

                    try { sevenZipFile.close(); }
                    catch(IOException e) {
                        // Not much we can do about it
                    }
                }
			}
		});
		
		return in; 
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
            try { sevenZipFile.close(); }
            catch(IOException e) {
                // Not much we can do about it
            }
        }
	}
}

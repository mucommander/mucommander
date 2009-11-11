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

public class SevenZipArchiveFile extends AbstractROArchiveFile {
	
	/** An interface to the seven-zip package */
	private IInArchive sevenZipFile;
	
	/** The date at which the current IInArchive object was created */
	private long lastSevenZipFileDate;
	
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
	
	/**
     * Checks if the underlying 7zip file is up-to-date, i.e. exists and has not changed without this archive file
     * being aware of it. If one of those 2 conditions are not met, (re)load the RipFile instance (parse the entries)
     * and declare the 7zip file as up-to-date.
     *
     * @throws IOException if an error occurred while reloading
     */
    private void checkSevenZipFile() throws IOException {
        long currentDate = file.getDate();
        
        if (sevenZipFile==null || currentDate != lastSevenZipFileDate) {
        	MuRandomAccessFile istream = new MuRandomAccessFile(file);
        	sevenZipFile = new Handler();
        	if (sevenZipFile.Open( istream ) != 0)
        		throw new IOException("ERROR: could not open 7zip archive: " + file.getAbsolutePath());
        	
            declareSevenZipFileUpToDate(currentDate);
        }
    }
    
    /**
     * Declare the underlying 7zip file as up-to-date. Calling this method after the 7zip file has been
     * modified prevents {@link #checkSevenZipFile()} from being reloaded.
     */
    private void declareSevenZipFileUpToDate(long currentFileDate) {
        lastSevenZipFileDate = currentFileDate;
    }
    
    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.SevenZipEntry}
     *
     * @param entry the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given SevenZipEntry
     */
    private ArchiveEntry createArchiveEntry(SevenZipEntry entry) {
		return new ArchiveEntry(
				entry.getName(), entry.isDirectory(), entry.getTime(), entry.getSize(), true
				);
	}
    
    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////

	@Override
    public InputStream getEntryInputStream(final ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException {
		checkSevenZipFile();
		
		final FailSafePipedInputStream in = new FailSafePipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);

		threadPool.execute(new Runnable() {
			public void run() {
		        BufferedOutputStream bufferStream = null;

		        bufferStream = new BufferedOutputStream(out);
		        try {
					MuArchiveExtractCallback extractCallbackSpec = new MuArchiveExtractCallback(bufferStream, entry.getPath());
			        extractCallbackSpec.Init(sevenZipFile);
			        sevenZipFile.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract , extractCallbackSpec);
			        
					bufferStream.flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
                    FileLogger.finest(null, e);
				}
		        
			}
		});
		
		return in; 
	}

	@Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
		checkSevenZipFile();

    	Vector<SevenZip.Archive.SevenZipEntry> result = new Vector<SevenZip.Archive.SevenZipEntry>();
    	
    	for(int i = 0; i < sevenZipFile.size() ; i++)
            result.add(sevenZipFile.getEntry(i));
    	
    	Vector<ArchiveEntry> entries = new Vector<ArchiveEntry>();

        for (Object aResult : result)
            entries.add(createArchiveEntry((SevenZipEntry)aResult));
        
        return new WrapperArchiveEntryIterator(entries.iterator());
	}
}

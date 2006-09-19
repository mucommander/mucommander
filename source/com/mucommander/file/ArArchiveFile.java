package com.mucommander.file;

import com.mucommander.io.ByteLimitInputStream;

import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Maxence Bernard
 */
public class ArArchiveFile extends AbstractArchiveFile {

    /**
     * Creates a ArArchiveFile around the given file.
     */
    public ArArchiveFile(AbstractFile file) {
        super(file);
    }


    /**
     * Skips the global header: "!<arch>" string followed by LF char (8 characters in total).
     */
    private static void skipGlobalHeader(InputStream in) throws IOException {

        skipFully(in, 8);
    }


    /**
     * Reads the next file header and returns an ArchiveEntry representing the entry.
     */
    private static ArchiveEntry getNextEntry(InputStream in) throws IOException {
        byte fileHeader[] = new byte[60];

        try {
            // Fully read the 60 file header bytes. If it cannot be read, it most likely means we've reached
            // the end of the archive.
            readFully(in, fileHeader);
        }
        catch(IOException e) {
            return null;
        }

        try {
            // Read the 16 filename characters and trim string to remove any trailing space characters
            String name = new String(fileHeader, 0, 16).trim();

            // Read the 16 file date characters, trim string to remove any trailing space characters
            long date = Long.parseLong(new String(fileHeader, 16, 12).trim()) * 1000;

            // No use for file's Owner ID, Group ID and mode at the moment, skip them

            // Read the 10 file size characters, trim string to remove any trailing space characters
            // and parse size as a long
            long size = Long.parseLong(new String(fileHeader, 48, 10).trim());

            // BSD variant : BSD ar store extended filenames by placing the string "#1/" followed by the file name length
            // in the file name field, and appending the real filename to the file header.
            if(name.startsWith("#1/")) {
                // Read extended name
                int extendedNameLength = Integer.parseInt(name.substring(3, name.length()));
                name = new String(readFully(in, new byte[extendedNameLength])).trim();
                size -= extendedNameLength;
            }

            return new SimpleEntry(name, date, size, false);
        }
        // Re-throw IOException
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught IOException: "+e);

            throw e;
        }
        // Catch any other exceptions (NumberFormatException for instance) and throw an IOException instead
        catch(Exception e2) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught Exception: "+e2);

            throw new IOException();
        }
    }


    /**
     * Skips the current entry's file data, using the given entry's size.
     */
    private static void skipEntryData(InputStream in, ArchiveEntry entry) throws IOException {
        long size = entry.getSize();

        // Skip file's data, plus 1 padding character if size is not even
        skipFully(in, size + (size%2));
    }


    /**
     * Fully reads the given byte array from the given InputStream.
     *
     * @throws IOException if the byte array could not be read
     */
    private static byte[] readFully(InputStream in, byte b[]) throws IOException {
        int off = 0;
        int len = b.length;
        do {
            int nbRead = in.read(b, off, len-off);
            if(nbRead==-1)
                throw new IOException();

            off += nbRead;
        }
        while(off<len);

        return b;
    }


    /**
     * Fully skips the given number of bytes in the given InputStream.
     *
     * @throws IOException if the bytes could not be skipped
     */
    private static void skipFully(InputStream in, long n) throws IOException {
        do {
            long nbSkipped = in.skip(n);
            if(nbSkipped<0)
                throw new IOException();

            n -= nbSkipped;
        } while(n>0);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    Vector getEntries() throws IOException {
        Vector entries = new Vector();
        InputStream in = getInputStream();
        skipGlobalHeader(in);

        try {
            ArchiveEntry entry;
            while((entry = getNextEntry(in))!=null) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Adding entry "+entry.getName());

                skipEntryData(in, entry);
                entries.add(entry);
            }
        }
        finally {
            // Close input stream
            in.close();
        }

        return entries;
    }


    InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        InputStream in = getInputStream();
        skipGlobalHeader(in);

        ArchiveEntry currentEntry;
        while((currentEntry = getNextEntry(in))!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("currentEntry="+currentEntry.getName()+" entry="+entry.getName());

            if(currentEntry.getName().equals(entry.getName())) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found entry "+entry.getName());
               return new ByteLimitInputStream(in, entry.getSize());
            }

            skipEntryData(in, currentEntry);
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("throwing IOException");

        // Entry not found, should not normally happen
        throw new IOException();
    }
}

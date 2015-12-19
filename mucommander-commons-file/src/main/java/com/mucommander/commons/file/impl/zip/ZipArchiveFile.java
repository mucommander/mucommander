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


package com.mucommander.commons.file.impl.zip;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.impl.zip.provider.ZipConstants;
import com.mucommander.commons.file.impl.zip.provider.ZipEntry;
import com.mucommander.commons.file.impl.zip.provider.ZipFile;
import com.mucommander.commons.io.FilteredOutputStream;

import java.io.*;
import java.util.Iterator;
import java.util.zip.ZipInputStream;


/**
 * ZipArchiveFile provides read and write access (under certain conditions) to archives in the Zip format.
 * <p>
 * Two different packages that implement the actual Zip compression format are used: the homemade
 * <code>com.mucommander.commons.file.impl.zip.provider</code> package and Java's <code>java.util.zip</code>.
 * <code>com.mucommander.commons.file.impl.zip.provider</code> provides additional functionality and improved performance over
 * <code>java.util.zip</code> but requires the underlying file to supply a <code>RandomAccessInputStream</code> for read
 * access and a <code>RandomAccessOutputStream</code> for write access. If the underlying file can't provide at least a
 * <code>RandomAccessInputStream</code>, the lesser <code>java.util.zip</code> package is used.
 * </p>
 *
 * @see com.mucommander.commons.file.impl.zip.ZipFormatProvider
 * @see com.mucommander.commons.file.impl.zip.provider.ZipFile
 * @author Maxence Bernard
 */
public class ZipArchiveFile extends AbstractRWArchiveFile {

    /** The ZipFile object that actually reads and modifies the entries in the Zip file */
    private ZipFile zipFile;

    /** The date at which the current ZipFile object was created */
    private long lastZipFileDate;

    /** Contents of an empty Zip file, 22 bytes long */
    private final static byte EMPTY_ZIP_BYTES[] = {
        0x50, 0x4B, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };


    /**
     * Creates a new ZipArchiveFile on top of the given file.
     *
     * @param file the underlying archive file
     */
    public ZipArchiveFile(AbstractFile file) {
        super(file);
    }

    /**
     * Checks if the underlying Zip file is up-to-date, i.e. exists and has not changed without this archive file
     * being aware of it. If one of those 2 conditions are not met, (re)load the ZipFile instance (parse the entries)
     * and declare the Zip file as up-to-date.
     *
     * @throws IOException if an error occurred while reloading
     */
    private void checkZipFile() throws IOException, UnsupportedFileOperationException {
        long currentDate = file.getDate();

        if(zipFile==null || currentDate!=lastZipFileDate) {
            zipFile = new ZipFile(file);
            declareZipFileUpToDate();
        }
    }

    /**
     * Declare the underlying Zip file as up-to-date. Calling this method after the Zip file has been modified prevents
     * {@link #checkZipFile()} from being reloaded.
     */
    private void declareZipFileUpToDate() {
        lastZipFileDate = file.getDate();
    }

    /**
     * Creates and returns a {@link com.mucommander.commons.file.impl.zip.provider.ZipEntry} instance using the attributes
     * of the given {@link ArchiveEntry}.
     *
     * @param entry the object that serves to initialize the attributes of the returned ZipEntry
     * @return a ZipEntry whose attributes are fetched from the given ZipEntry
     */
    private ZipEntry createZipEntry(ArchiveEntry entry) {
        boolean isDirectory = entry.isDirectory();
        String path = entry.getPath();
        if(isDirectory && !path.endsWith("/"))
            path += "/";

        com.mucommander.commons.file.impl.zip.provider.ZipEntry zipEntry = new com.mucommander.commons.file.impl.zip.provider.ZipEntry(path);
        zipEntry.setMethod(ZipConstants.DEFLATED);
        zipEntry.setTime(System.currentTimeMillis());
        zipEntry.setUnixMode(SimpleFilePermissions.padPermissions(entry.getPermissions(), isDirectory
                    ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                    : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue());

        return zipEntry;
    }

    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.commons.file.impl.zip.provider.ZipEntry}.
     * It is worth noting that the returned entry has the {@link ArchiveEntry#exists exists} flag set to <code>true</code>.
     *
     * @param zipEntry the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given ZipEntry
     */
    static ArchiveEntry createArchiveEntry(ZipEntry zipEntry) {
        ArchiveEntry entry = new ArchiveEntry(zipEntry.getName(), zipEntry.isDirectory(), zipEntry.getTime(), zipEntry.getSize(), true);

        if(zipEntry.hasUnixMode())
            entry.setPermissions(new SimpleFilePermissions(zipEntry.getUnixMode()));

        entry.setEntryObject(zipEntry);

        return entry;
    }

    /**
     * Adds the given {@link ArchiveEntry} to the entries tree and declares the Zip file and entries tree up-to-date.
     *
     * @param entry the entry to add to the entries tree
     * @throws IOException if an error occurred while adding the entry to the tree
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    private void finishAddEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
        // Declare the zip file and entries tree up-to-date
        declareZipFileUpToDate();
        declareEntriesTreeUpToDate();

        // Add the new entry to the entries tree
        addToEntriesTree(entry);
    }


    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////

    @Override
    public synchronized ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
        // If the underlying AbstractFile has random read access, use our own ZipFile implementation to read entries
        if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
            checkZipFile();

            final Iterator<ZipEntry> iterator = zipFile.getEntries();

            return new ArchiveEntryIterator() {
                public ArchiveEntry nextEntry() throws IOException {
                    ZipEntry entry;

                    if(!iterator.hasNext() || (entry = iterator.next())==null)
                        return null;

                    return createArchiveEntry(entry);
                }

                public void close() throws IOException {
                }
            };
        }
        // If the underlying AbstractFile doesn't have random read access, use java.util.zip.ZipInputStream to
        // read the entries. This is much slower than the former method as the file cannot be sought through and needs
        // to be traversed.
        else {
            return new JavaUtilZipEntryIterator(new ZipInputStream(file.getInputStream()));
        }
    }


    @Override
    public synchronized InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        // If the underlying AbstractFile has random read access, use our own ZipFile implementation to read the entry
        if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
            checkZipFile();

            ZipEntry zipEntry = (com.mucommander.commons.file.impl.zip.provider.ZipEntry)entry.getEntryObject();
            if(zipEntry==null)  // Should not normally happen
                throw new IOException();

            return zipFile.getInputStream(zipEntry);
        }
        // If the underlying AbstractFile doesn't have random read access, use java.util.InputStream to
        // read the entry. This is much slower than the former method as the file cannot be seeked and needs
        // to be traversed to locate the entry we're interested in.
        else {
            // Optimization: first check if the specified iterator is positionned at the beginning of the entry.
            // This will typically be the case if an iterator is being used to read all the archive's entries
            // (unpack operation). In that case, we save the cost of looking for the entry in the archive.
            if(entryIterator!=null && (entryIterator instanceof JavaUtilZipEntryIterator)) {
                ArchiveEntry currentEntry = ((JavaUtilZipEntryIterator)entryIterator).getCurrentEntry();
                if(currentEntry.getPath().equals(entry.getPath())) {
                    // The entry/zip stream is wrapped in a FilterInputStream where #close is implemented as a no-op:
                    // we don't want the ZipInputStream to be closed when the caller closes the entry's stream.
                    return new FilterInputStream(((JavaUtilZipEntryIterator)entryIterator).getZipInputStream()) {
                        @Override
                        public void close() throws IOException {
                            // No-op
                        }
                    };
                }

                // This is not the one, look for the entry from the beginning of the archive
            }

            // Iterate through the archive until we've found the entry
            java.util.zip.ZipInputStream zin = new java.util.zip.ZipInputStream(file.getInputStream());
            java.util.zip.ZipEntry zipEntry;
            String entryPath = entry.getPath();
            // Iterate until we find the entry we're looking for
            while ((zipEntry=zin.getNextEntry())!=null)
                if (zipEntry.getName().equals(entryPath)) // That's the one, return it
                    return zin;

            throw new IOException("Unknown Zip entry: "+entry.getName());
        }
    }

    //////////////////////////////////////////
    // AbstractRWArchiveFile implementation //
    //////////////////////////////////////////

    @Override
    public synchronized OutputStream addEntry(final ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
        checkZipFile();

        final ZipEntry zipEntry = createZipEntry(entry);

        if(zipEntry.isDirectory()) {
            // Add the new directory entry to the zip file (physically)
            zipFile.addEntry(zipEntry);

            // Set the ZipEntry object into the ArchiveEntry
            entry.setEntryObject(zipEntry);

            // Declare the zip file and entries tree up-to-date and add the new entry to the entries tree
            finishAddEntry(entry);

            return null;
        }
        else {
            // Set the ZipEntry object into the ArchiveEntry
            entry.setEntryObject(zipEntry);

            return new FilteredOutputStream(zipFile.addEntry(zipEntry)) {
                @Override
                public void close() throws IOException {
                    super.close();

                    // Declare the zip file and entries tree up-to-date and add the new entry to the entries tree
                        finishAddEntry(entry);
                    }
            };
        }
    }

    @Override
    public synchronized void deleteEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
        ZipEntry zipEntry = (com.mucommander.commons.file.impl.zip.provider.ZipEntry)entry.getEntryObject();

        // Most of the time, the ZipEntry will not be null. However, it can be null in some rare cases, when directory
        // entries have been created in the entries tree but don't exist in the Zip file.
        // That is the case when a file entry exists in the Zip file but has no directory entry for the parent.
        if(zipEntry!=null) {
            // Entry exists physically in the zip file

            checkZipFile();

            // Delete the entry from the zip file (physically)
            zipFile.deleteEntry(zipEntry);

            // Remove the ZipEntry object from the AchiveEntry
            entry.setEntryObject(null);

            // Declare the zip file and entries tree up-to-date
            declareZipFileUpToDate();
            declareEntriesTreeUpToDate();
        }
        // Else entry doesn't physically exist in the zip file, only in the entries tree

        // Remove the entry from the entries tree
        removeFromEntriesTree(entry);
    }

    @Override
    public void updateEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
        ZipEntry zipEntry = (com.mucommander.commons.file.impl.zip.provider.ZipEntry)entry.getEntryObject();

        // Most of the time, the ZipEntry will not be null. However, it can be null in some rare cases, when directory
        // entries have been created in the entries tree but don't exist in the Zip file.
        // That is the case when a file entry exists in the Zip file but has no directory entry for the parent.
        if(zipEntry!=null) {
            // Entry exists physically in the zip file

            checkZipFile();

            zipEntry.setTime(entry.getDate());
            zipEntry.setUnixMode(entry.getPermissions().getIntValue());
            
            // Physically update the entry's attributes in the Zip file
            zipFile.updateEntry(zipEntry);

            // Declare the zip file and entries tree up-to-date
            declareZipFileUpToDate();
            declareEntriesTreeUpToDate();
        }
    }

    @Override
    public synchronized void optimizeArchive() throws IOException, UnsupportedFileOperationException {
        checkZipFile();

        // Defragment the zip file
        zipFile.defragment();

        // Declare the zip file and entries tree up-to-date
        declareZipFileUpToDate();
        declareEntriesTreeUpToDate();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns <code>true</code> only if the proxied archive file has random read and write access, as reported
     * by {@link AbstractFile#isFileOperationSupported(FileOperation)}. If that is not the case, this archive has 
     * read-only access and behaves just like a {@link com.mucommander.commons.file.AbstractROArchiveFile}.
     *
     * @return true only if the proxied archive file has random read and write access
     */
    @Override
    public boolean isWritable() {
        return file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)
            && file.isFileOperationSupported(FileOperation.RANDOM_WRITE_FILE);
    }

    /**
     * Creates an empty, valid Zip file. The resulting file is 22 bytes long.
     */
    @Override
    public void mkfile() throws IOException, UnsupportedFileOperationException {
        if(exists())
            throw new IOException();

        copyStream(new ByteArrayInputStream(EMPTY_ZIP_BYTES), false, EMPTY_ZIP_BYTES.length);
    }
}

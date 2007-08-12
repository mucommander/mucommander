/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.file.impl.zip;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractRWArchiveFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.impl.zip.provider.ZipConstants;
import com.mucommander.file.impl.zip.provider.ZipFile;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipInputStream;
// do not import java.util.zip.ZipEntry or com.mucommander.file.impl.zip.provider.ZipEntry as it would create class name conflicts!

/**
 * ZipArchiveFile provides read-only access to archives in the Zip format.
 *
 * <p>Zip support is provided by the <code>java.util.zip</code> API.
 *
 * @author Maxence Bernard
 */
public class ZipArchiveFile extends AbstractRWArchiveFile {

    /** The ZipFile object that actually reads and modifies the entries in the Zip file */
    private ZipFile zipFile;

    /** The date at which the current ZipFile object was created */
    private long lastZipFileDate;

    /**
     * Creates a new ZipArchiveFile.
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
    private void checkZipFile() throws IOException {
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


    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////
	
    public Vector getEntries() throws IOException {
        Vector entries = new Vector();

        // If the underlying AbstractFile has random read access, use our own ZipFile implementation to read entries
        if (file.hasRandomAccessInputStream()) {
            checkZipFile();

            Enumeration entriesEnum = zipFile.getEntries();
            while(entriesEnum.hasMoreElements())
                entries.add(new ZipEntry((java.util.zip.ZipEntry)entriesEnum.nextElement()));
        }
        // If the underlying AbstractFile doesn't have random read access, use java.util.InputStream to
        // read the entries. This is much slower than the former method as the file cannot be seeked and needs
        // to be traversed.
        else {
            ZipInputStream zin = null;
            try {
                zin = new ZipInputStream(file.getInputStream());
                java.util.zip.ZipEntry entry;
                while ((entry=zin.getNextEntry())!=null)
                    entries.add(new ZipEntry(entry));
            }
            catch(Exception e) {
                // java.util.zip.ZipInputStream can throw an IllegalArgumentException when the filename/comment encoding
                // is not UTF-8 as expected (ZipInputStream always expects UTF-8). The more general Exception is caught
                // (just to be safe) and an IOException thrown.
                throw new IOException();
            }
            finally {
                if(zin!=null)
                    zin.close();
            }
        }

        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        // If the underlying AbstractFile has random read access, use our own ZipFile implementation to read the entry
        if (file.hasRandomAccessInputStream()) {
            checkZipFile();

            return zipFile.getInputStream((com.mucommander.file.impl.zip.provider.ZipEntry)entry.getEntryObject());
        }
        // If the underlying AbstractFile doesn't have random read access, use java.util.InputStream to
        // read the entry. This is much slower than the former method as the file cannot be seeked and needs
        // to be traversed.
        else {
            ZipInputStream zin = new ZipInputStream(file.getInputStream());
            java.util.zip.ZipEntry tempEntry;
            String entryPath = entry.getPath();
            // Iterate until we find the entry we're looking for
            while ((tempEntry=zin.getNextEntry())!=null)
                if (tempEntry.getName().equals(entryPath)) // That's the one, return it
                    return zin;
            return null;
        }
    }

    private ZipEntry getZipEntry(ArchiveEntry entry) {
        String path = entry.getPath();
        if(entry.isDirectory() && !path.endsWith("/"))
            path += "/";

        com.mucommander.file.impl.zip.provider.ZipEntry entryObject = new com.mucommander.file.impl.zip.provider.ZipEntry(path);
        entryObject.setMethod(ZipConstants.DEFLATED);
        entryObject.setTime(System.currentTimeMillis());

        return new ZipEntry(entryObject);
    }

    private void finishAddEntry(ZipEntry zipEntry) throws IOException {
        // Declare the zip file and entries tree up-to-date
        declareZipFileUpToDate();
        declareEntriesTreeUpToDate();

        // Add the new entry to the entries tree
        addToEntriesTree(zipEntry);
    }


    //////////////////////////////////////////
    // AbstractRWArchiveFile implementation //
    //////////////////////////////////////////

    public OutputStream addEntry(ArchiveEntry entry) throws IOException {
        checkZipFile();

        final ZipEntry zipEntry = getZipEntry(entry);

        if(zipEntry.isDirectory()) {
            // Add the new directory entry to the zip file (physically)
            zipFile.addEntry((com.mucommander.file.impl.zip.provider.ZipEntry)zipEntry.getEntryObject());

            // Declare the zip file and entries tree up-to-date and add the new entry to the entries tree
            finishAddEntry(zipEntry);

            return null;
        }
        else {
            return new FilterOutputStream(zipFile.addEntry((com.mucommander.file.impl.zip.provider.ZipEntry)zipEntry.getEntryObject())) {
                public void close() throws IOException {
                    super.close();

                    // Declare the zip file and entries tree up-to-date and add the new entry to the entries tree
                    finishAddEntry(zipEntry);
                }
            };
        }
    }

    public void deleteEntry(ArchiveEntry entry) throws IOException {
        // Most of the time, the given ArchiveEntry will be a Zip entry. However, in some rare cases, it can be a
        // SimpleArchiveEntry for directory entries that have been created in the entries tree but don't exist in the
        // Zip file. That is the case when a file entry exists in the Zip file but has no directory entry for the parent.
        if(entry instanceof ZipEntry) {
            // Entry exists physically in the zip file

            checkZipFile();

            // Delete the entry from the zip file (physically)
            zipFile.deleteEntry((com.mucommander.file.impl.zip.provider.ZipEntry)entry.getEntryObject());

            // Declare the zip file and entries tree up-to-date
            declareZipFileUpToDate();
            declareEntriesTreeUpToDate();
        }
        // Else entry doesn't physically exist in the zip file, only in the entries tree

        // Remove the entry from the entries tree
        removeFromEntriesTree(entry);
    }

//    public OutputStream addFileEntry(ArchiveEntry entry) throws IOException {
//        checkZipFile();
//
//        final ZipEntry zipEntry = getZipEntry(entry);
//
//        return new FilterOutputStream(zipFile.addEntry((com.mucommander.file.impl.zip.provider.ZipEntry)zipEntry.getEntryObject())) {
//            public void close() throws IOException {
//                super.close();
//
//                // Declare the zip file and entries tree up-to-date
//                declareZipFileUpToDate();
//                declareEntriesTreeUpToDate();
//
//                // Add the new directory entry to the entries tree
//                addToEntriesTree(zipEntry);
//            }
//        };
//    }
//
//    public void addDirectoryEntry(ArchiveEntry entry) throws IOException {
//        checkZipFile();
//
//        ZipEntry zipEntry = getZipEntry(entry);
//
//        // Add the new directory entry to the zip file (physically)
//        zipFile.addEntry((com.mucommander.file.impl.zip.provider.ZipEntry)zipEntry.getEntryObject());
//
//        // Declare the zip file and entries tree up-to-date and add the new entry to the entries tree
//        declareZipFileUpToDate();
//        declareEntriesTreeUpToDate();
//
//        // Add the new directory entry to the entries tree
//        addToEntriesTree(zipEntry);
//    }

    public void optimizeArchive() throws IOException {
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
     * by {@link #hasRandomAccessInputStream()} and {@link #hasRandomAccessOutputStream()} respectively. If that is
     * not the case, this archive has read-only access and behaves just like a
     * {@link com.mucommander.file.AbstractROArchiveFile}.
     *
     * @return true only if the proxied archive file has random read and write access
     */
    public boolean isWritableArchive() {
        return file.hasRandomAccessInputStream() && file.hasRandomAccessOutputStream();
    }
}

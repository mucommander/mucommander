/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mucommander.file.impl.zip.provider;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.*;

import java.io.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * This class is a replacement for <code>java.util.ZipFile</code> with some extra functionalities:
 * <ul>
 *  <li>Ability to add or remove entries 'on-the-fly', i.e. without rewriting the whole archive.
 *  <li>Advanced encoding support for filenames and comments. UTF-8 is used for parsing entries that explicitely declare
 * using UTF-8 (as per Zip specs). For entries that do not use UTF-8, the encoding is auto-detected (best effort).
 * Alternatively, the encoding used for parsing entries can be specified if it is known in advance. For new entries
 * added with {@link #addEntry(ZipEntry)}, UTF-8 is always used and declared as such in the Zip headers.
 *  <li>Loads the internal/external file attributes and extra fields instead of ignoring them
 * </ul>
 *
 * <p>This class doesn't extend <code>java.util.zip.ZipFile</code> as it would have to reimplement all methods anyway.
 * Like <code>java.util.ZipFile</code>, it supports compressed (DEFLATED) and uncompressed (STORED) entries.</p>
 *
 * <p>Random read access is required to instanciate a <code>ZipFile</code> and retrieve its entries. Furthermore, random
 * write access is required for methods that modify the Zip file.</p>
 *
 * <p>The method signatures mimic the ones of <code>java.util.zip.ZipFile</code> with a few exceptions:
 * <ul>
 *   <li>There is no <code>getName</code> method.</li>
 *   <li>There is no <code>close</code> method: underlying input and output streams are opened and closed automatically
 *    as they are needed.</li>
 *   <li><code>entries</code> has been renamed to {@link #getEntries()}.</li>
 *   <li><code>size</code> has been renamed to {@link #getNbEntries()}.</li>
 * </ul>
 * </p>
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Apache Ant, Maxence Bernard
 */
public class ZipFile implements ZipConstants {

    /** The underlying archive file */
    private AbstractFile file;

    /** The currently opened RandomAccessInputStream to the zip file (may be null) */
    private RandomAccessInputStream rais;

    /** The currently opened RandomAccessInputStream to the zip file (may be null) */
    private RandomAccessOutputStream raos;

    /** Contains ZipEntry instances corresponding to the archive's entries, in the order they were found in the archive. */
    private Vector entries = new Vector();

    /** Maps entry paths to corresponding ZipEntry instances */
    private Hashtable nameMap = new Hashtable();

    /** Global zip file comment */
    private String comment;


    /**
     * The default encoding to use for parsing filenames and comments. This value is only used for Zip entries that do
     * not have the UTF-8 flag set. If not specified (null), then automatic encoding detection is used (default).
     */
    private String defaultEncoding = null;


    /**
     * Opens the given Zip file and parses information about the entries it contains.
     *
     * <p>The given {@link AbstractFile} must have random read access. If not, an <code>IOException</code> will be
     * thrown.</p>
     *
     * @param f the archive file
     * @throws IOException if a RandomAccessInputStream could not retrieved, or if an error occurred while reading the
     * Zip file.
     * @throws ZipException if this file is not a valid Zip file
     */
    public ZipFile(AbstractFile f) throws IOException, ZipException {
        this.file = f;

        try {
            openRead();
            parseCentralDirectory();
        }
        finally {
            closeRead();
        }
    }


    /**
     * Opens the zip file for random read access.
     *
     * @throws IOException if an error occured while opening the zip file for random read access.
     */
    private void openRead() throws IOException {
        if(rais!=null) {
            if(Debug.ON) Debug.trace("Warning: an existing RandomAccessInputStream was found, closing it now");
            rais.close();
        }

        rais = file.getRandomAccessInputStream();
    }

    /**
     * Closes the current RandomAccessInputStream to the zip file.
     *
     * @throws IOException if an error occurred
     */
    private void closeRead() throws IOException {
        if(rais!=null) {
            try {
                rais.close();
            }
            finally {
                rais = null;
            }
        }
    }

    /**
     * Opens the zip file for random write access.
     *
     * @throws IOException if an error occured while opening the zip file for random read access.
     */
    private void openWrite() throws IOException{
        if(raos!=null) {
            if(Debug.ON) Debug.trace("Warning: an existing RandomAccessOutputStream was found, closing it now");
            raos.close();
        }

        // Create a buffered output stream to improve write performance, as headers are written by small chunks
        raos = new BufferedRandomOutputStream(file.getRandomAccessOutputStream(), WRITE_BUFFER_SIZE);
    }

    /**
     * Closes the current RandomAccessOutputStream to the zip file.
     *
     * @throws IOException if an error occurred
     */
    private void closeWrite() throws IOException {
        if(raos!=null) {
            try {
                raos.close();
            }
            finally {
                raos = null;
            }
        }
    }

    /**
     * Returns the default encoding to use for parsing filenames and comments. This value is not used for Zip entries
     * that explicitely declare using UTF-8 (in the general purpose bit flag).
     *
     * <p>By default, this method returns <code>null</code> to indicate that automatic encoding detection is used. 
     * Although it is not 100% accurate, encoding detection is the preferred approach, unless the encoding is known
     * in advance which is rather uncommon.</p>
     *
     * <p>Note that this value only affects entries <i>parsing</i>. Written entries are systematically encoded in
     * <code>UTF-8</code> and declared as such in the general purpose bit flag so that proper zip unpackers know what
     * encoding to expect.</p>
     *
     * @return the default encoding to use for parsing filenames and comments
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding to use for parsing filenames and comments. This value is not used for Zip entries
     * that explicitely declare using UTF-8 (in the general purpose bit flag).
     *
     * <p>By default, the encoding is <code>null</code> to indicate that automatic encoding detection is used.
     * Although it is not 100% accurate, encoding detection is the preferred approach, unless the encoding is known
     * in advance which is rather uncommon.</p>
     *
     * <p>Note that this value only affects entries <i>parsing</i>. Written entries are systematically encoded in
     * <code>UTF-8</code> and declared as such in the general purpose bit flag so that proper zip unpackers know what
     * encoding to expect.</p>
     *
     * @param defaultEncoding the default encoding to use for parsing filenames and comments
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Returns all entries as an enumeration of {@link ZipEntry} instances.
     *
     * @return Returns all entries as an enumeration of ZipEntry instances.
     */
    public Enumeration getEntries() {
        return entries.elements();
    }

    /**
     * Returns the number of entries contained by this Zip file.
     *
     * @return the number of entries contained by this Zip file
     */
    public int getNbEntries() {
        return entries.size();
    }

    /**
     * Returns a named entry or <code>null</code> if no entry by that name exists.
     *
     * @param name name of the entry.
     * @return the ZipEntry corresponding to the given name or <code>null</code> if not present.
     */
    public ZipEntry getEntry(String name) {
        return (ZipEntry) nameMap.get(name);
    }

    /**
     * Returns an InputStream for reading the contents of the given entry.
     *
     * @param ze the entry to get the stream for.
     * @return a stream to read the entry from.
     * @throws IOException if unable to create an input stream from the zipenty
     * @throws ZipException if the zipentry has an unsupported compression method
     */
    public InputStream getInputStream(ZipEntry ze) throws IOException, ZipException {

        ZipEntryInfo entryInfo = ze.getEntryInfo();
        if (entryInfo == null)
            throw new ZipException("Unknown entry: "+ze.getName());

        openRead();
        RandomAccessInputStream entryIn = this.rais;

        // If data offset is -1 (not calculated yet), calculate it now
        if (entryInfo.dataOffset == -1)
            calculateDataOffset(entryInfo);

        this.rais = null;
        
        long start = entryInfo.dataOffset;
        BoundedInputStream bis =
            new BoundedInputStream(entryIn, start, ze.getCompressedSize());
        switch (ze.getMethod()) {
            case ZipEntry.STORED:
                return bis;
            case ZipEntry.DEFLATED:
                bis.addDummy();
                return new InflaterInputStream(bis, new Inflater(true));
            default:
                throw new ZipException("Found unsupported compression method "
                                       + ze.getMethod());
        }
    }


    /**
     * Deletes the given entry from this zip file. For performance reasons, this method removes the central file
     * header and zero out the local file header and data so that the entry cannot be retrieved, but it does
     * not reclaim the freed space and produces fragmentation. In other words, the resulting zip file will not be
     * smaller after the entry has been deleted and will contain an area of unused space. The {@link #defragment} method
     * can be called to reclaim the free space.
     *
     * <p>There is one case where this method reclaims the free space: when the specified entry is the last one in the
     * zip file. In this case, the resulting zip file will be smaller.</p>
     *
     * <p>Note that 'fragmented' zip files are perfectly valid zip files, any zip parser should be able to cope with
     * such files.<p>
     *
     * <p>The underlying {@link AbstractFile} must have random write access. If not, an <code>IOException</code> will be
     * thrown.</p>
     *
     * @param ze the ZipEntry to delete
     * @throws IOException if the underlying AbstractFile does not have random write access or if an I/O error occurred
     * @throws ZipException if the specified ZipEntry cannot be found in this zip file
     */
    public void deleteEntry(ZipEntry ze) throws IOException, ZipException {
        openRead();
        openWrite();

        try {
            ZipEntryInfo entryInfo = ze.getEntryInfo();
            if (entryInfo == null) {
                // Fail silently if the entry is a directory as specific directory entries do not always exist
                // in zip files.
                if(ze.isDirectory())
                    return;

                throw new ZipException("Unknown entry: "+ze.getName());
            }

            // Strip out central file header of deleted entry

            int entryIndex = entries.indexOf(ze);
            int nbEntries = entries.size();

            long cdStartOffset;
            long cdEndOffset;

            if(nbEntries==1) {
                // Special case if the deleted entry is the only one, the zip file will become empty.
                // Note: empty zip files must have the central directory start at offset 0.
                cdStartOffset = 0;
                cdEndOffset = 0;

                raos.seek(0);
            }
            else {
                cdStartOffset = ((ZipEntry)entries.elementAt(0)).getEntryInfo().centralHeaderOffset;
                long shift;

                if(entryIndex==nbEntries-1) {
                    // Lucky case! If the entry to delete is the last one, we can easily/quickly reclaim the space used
                    // by this entry by moving the central directory (minus the last header corresponding to the deleted
                    // entry) to where the entry's local header started.

                    // The entry before the deleted one, will become the last one
                    ZipEntryInfo lastEntryInfo = ((ZipEntry)entries.elementAt(nbEntries-2)).getEntryInfo();

                    // Destination offset
                    long newCdStartOffset = entryInfo.headerOffset;

                    cdEndOffset = lastEntryInfo.centralHeaderOffset + lastEntryInfo.centralHeaderLen;
                    long cdLength = cdEndOffset-cdStartOffset;

                    // Copy the central directory
                    copyChunk(rais, raos, cdStartOffset, newCdStartOffset, cdLength);

                    // Update central directory header offsets
                    shift = cdStartOffset-newCdStartOffset;
                    for(int i=0; i<nbEntries-1; i++)
                        ((ZipEntry)entries.elementAt(i)).getEntryInfo().centralHeaderOffset -= shift;

                    cdStartOffset = newCdStartOffset;
                    cdEndOffset = newCdStartOffset + cdLength;
                }
                else {
                    // Most frequent case: the entry to delete is neither the only one nor the last one.
                    // In this case, we don't reclaim the space (would be too slow) but simply zero out
                    // the local file header and data (so that the data entry can't be retrieved)

                    // If data offset is -1 (not calculated yet), calculate it now
                    if (entryInfo.dataOffset == -1)
                        calculateDataOffset(entryInfo);

                    // Zero out all bytes of the local file header+data for the deleted entry
                    // Note: the data descriptor (if any) is not erased, this would require some extra check and it is
                    // not really necessary, as the information it contains is not sensitive
                    raos.seek(entryInfo.headerOffset);
                    writeZeros(raos, entryInfo.dataOffset-entryInfo.headerOffset + ze.getCompressedSize());

                    // Update the central directory :
                    // - do not touch the file headers that are located before the deleted entry
                    // - move the file headers that are located after the deleted entry to where the deleted entry's
                    // header was (this will remove the deleted entry's file header)
                    
                    ZipEntryInfo lastEntryInfo = ((ZipEntry)entries.elementAt(nbEntries-1)).getEntryInfo();

                    long startOffset = ((ZipEntry)entries.elementAt(entryIndex+1)).getEntryInfo().centralHeaderOffset;
                    cdEndOffset = lastEntryInfo.centralHeaderOffset + lastEntryInfo.centralHeaderLen;

                    copyChunk(rais, raos, startOffset, entryInfo.centralHeaderOffset, cdEndOffset-startOffset);

                    // Update central directory header offsets for files located after the deleted entry as their
                    // offset has changed
                    shift = entryInfo.centralHeaderLen;
                    for(int i=entryIndex+1; i<nbEntries; i++)
                        ((ZipEntry)entries.elementAt(i)).getEntryInfo().centralHeaderOffset -= shift;

                    cdEndOffset -= shift;
                }
            }

            // Write the central directory end section
            ZipOutputStream.writeCentralDirectoryEnd(raos, nbEntries-1, cdEndOffset - cdStartOffset, cdStartOffset, comment, UTF_8);

            // Truncate the zip file to reclaim the trailing unused space
            raos.setLength(raos.getOffset());

            // All good, remove the deleted entry from the lists
            entries.removeElementAt(entryIndex);
            nameMap.remove(ze.getName());
        }
        finally {
            try { closeRead(); }
            catch(IOException e) {}

            try { closeWrite(); }
            catch(IOException e) {}
        }
    }


    /**
     * Appends the given entry to the end of this zip file and returns an <code>OutputStream</code> that allows to write
     * the contents of the entry. The returned <code>OutputStream</code> must always be closed for the zip file to be
     * properly modified. Not doing will leave this zip file in a inconsistent, corrupted state.
     *
     * <p>The underlying {@link AbstractFile} must have random write access. If not, an <code>IOException</code> will be
     * thrown.</p>
     *
     * @param entry the entry to add to this zip file
     * @return an OutputStream to write the contents of the entry
     * @throws IOException if the underlying AbstractFile does not have random write access or if an I/O error occurred
     */
    public OutputStream addEntry(final ZipEntry entry) throws IOException {
        try {
            // Open the zip file for random read and write access
            openRead();
            openWrite();

            // Write the new entry's local file header right before the central directory start
            positionAtCentralDirectory();
            long centralDirectoryStart = rais.getOffset();
            raos.seek(centralDirectoryStart);

            final ZipEntryInfo entryInfo = new ZipEntryInfo();
            entryInfo.encoding = UTF_8;   // Always use UTF-8 for new entries
            entryInfo.headerOffset = centralDirectoryStart;
            entryInfo.dataOffset = entryInfo.headerOffset +
                                     ZipOutputStream.writeLocalFileHeader(entry, raos, entryInfo.encoding, false);

            // Add the new entry to the internal lists
            entry.setEntryInfo(entryInfo);
            entries.add(entry);
            nameMap.put(entry.getName(), entry);

            // Create the ZipEntryOutputStream to write the entry's contents
            ZipEntryOutputStream zeos = new ZipEntryOutputStream(raos) {
                // Post-data file info and central directory get written when the stream is closed
                public void close() throws IOException {
                    // Write data info in the local file header
                    ZipOutputStream.finalizeEntryData(entry, this, raos, false);

                    // Write the central directory that was squashed by the new entry (at least partially)
                    ZipEntry tempZe;
                    ZipEntryInfo tempEntryInfo;
                    int nbEntries = entries.size();
                    long cdLength = 0;                  // Length of central directory
                    long cdOffset = raos.getOffset();   // Offset of central directory
                    for(int i=0; i<nbEntries; i++) {
                        tempZe = (ZipEntry)entries.elementAt(i);
                        tempEntryInfo = tempZe.getEntryInfo();

                        // Update offset to central header
                        tempEntryInfo.centralHeaderOffset = raos.getOffset();

                        cdLength += ZipOutputStream.writeCentralFileHeader(
                                        tempZe,
                                        raos,
                                        tempEntryInfo.encoding,     // Preserve existing encoding so that LFH and CFH match
                                        tempEntryInfo.headerOffset,
                                        tempEntryInfo.hasDataDescriptor);

                        // Update length of central header
                        tempEntryInfo.centralHeaderLen = raos.getOffset() - tempEntryInfo.centralHeaderOffset;
                    }

                    ZipOutputStream.writeCentralDirectoryEnd(raos, nbEntries, cdLength, cdOffset, comment, UTF_8);

                    // In some rare cases, the resulting zip file may be smaller.
                    // Truncate the file to ensure that it ends at the central directory end position.
                    raos.setLength(raos.getOffset());

                    super.close();
                    closeWrite();
                }
            };

            // Directory entries cannot contain data, close the stream now and return null
            if(entry.isDirectory()) {
                zeos.close();
                return null;
            }

            return zeos;
        }
        finally {
            closeRead();
            // Note: RandomAccessOutputStream is closed by ZipEntryOutputStream#close()
        }
    }


    /**
     * Removes free space fragments from this zip file, thus reducing the size of the zip file. If this zip file does
     * not contain any free space fragments, the zip file is not modified.
     *
     * <p>Fragmentation occurs when deleting entries with {@link #deleteEntry(ZipEntry)}. When deleting several entries,
     * this method should be called once after all entries have deleted.</p>
     *
     * <p>The underlying {@link AbstractFile} must have random write access. If not, an <code>IOException</code> will be
     * thrown.</p>
     *
     * @throws IOException if the underlying AbstractFile does not have random write access or if an I/O error occurred
     */
    public void defragment() throws IOException {
        int nbEntries = entries.size();
        if(nbEntries==0)
            return;

        try {
            openRead();
            openWrite();

            ZipEntry currentEntry, previousEntry;
            ZipEntryInfo currentEntryInfo, previousEntryInfo;
            long shift = 0;

            // Special case for the first entry

            currentEntry = (ZipEntry)entries.elementAt(0);
            currentEntryInfo = currentEntry.getEntryInfo();

            // If data offset is -1 (not calculated yet), calculate it now
            if (currentEntryInfo.dataOffset == -1)
                calculateDataOffset(currentEntryInfo);

            if(currentEntryInfo.headerOffset>0) {
                copyChunk(rais, raos, currentEntryInfo.headerOffset, 0, (currentEntryInfo.dataOffset- currentEntryInfo.headerOffset)+currentEntry.getCompressedSize());
                shift = currentEntryInfo.headerOffset;

                currentEntryInfo.headerOffset = 0;
                currentEntryInfo.dataOffset -= shift;
            }

            previousEntry = currentEntry;
            previousEntryInfo = currentEntryInfo;

            // Process all other entries

            for(int i=1; i<nbEntries; i++) {
                currentEntry = (ZipEntry)entries.elementAt(i);
                currentEntryInfo = currentEntry.getEntryInfo();

                // If data offset is -1 (not calculated yet), calculate it now
                if (currentEntryInfo.dataOffset == -1)
                    calculateDataOffset(currentEntryInfo);

                // Calculate the offset to the end of the previous entry based on its data offset and compressed size
                // and taking into account a potential data descriptor
                long previousCompressedSize = previousEntry.getCompressedSize();
                long previousEntryEnd = previousEntryInfo.dataOffset+previousCompressedSize;
                if(previousEntryInfo.hasDataDescriptor)
                    previousEntryEnd += 16;

                // Tests if there is some unused space between the 2 entries
                if(previousEntryEnd < currentEntryInfo.headerOffset) {
                    copyChunk(rais, raos, currentEntryInfo.headerOffset, previousEntryInfo.dataOffset+previousCompressedSize, (currentEntryInfo.dataOffset- currentEntryInfo.headerOffset)+currentEntry.getCompressedSize());
                    shift = currentEntryInfo.headerOffset - (previousEntryInfo.dataOffset+previousCompressedSize);

                    currentEntryInfo.headerOffset -= shift;
                    currentEntryInfo.dataOffset -= shift;
                }

                previousEntry = currentEntry;
                previousEntryInfo = currentEntryInfo;
            }

            // Rewrite central directory with updated offsets
            if(shift!=0) {
                long cdLength = 0;                  // Length of central directory
                long cdOffset = raos.getOffset();   // Offset of central directory
                ZipEntry ze;
                ZipEntryInfo entryInfo;

                for(int i=0; i<nbEntries; i++) {
                    ze = (ZipEntry)entries.elementAt(i);
                    entryInfo = ze.getEntryInfo();

                    // Update offset to central directory file header
                    entryInfo.centralHeaderOffset = raos.getOffset();

                    // Preserve existing encoding when rewriting CFH so that it matches LFH
                    cdLength += ZipOutputStream.writeCentralFileHeader(ze, raos, entryInfo.encoding, entryInfo.headerOffset, entryInfo.hasDataDescriptor);

                    // Update length of central directory file header
                    entryInfo.centralHeaderLen = raos.getOffset() - entryInfo.centralHeaderOffset;
                }

                ZipOutputStream.writeCentralDirectoryEnd(raos, nbEntries, cdLength, cdOffset, comment, UTF_8);

                // Truncate the zip file to reclaim the trailing unused space
                raos.setLength(raos.getOffset());
            }
        }
        finally {
            try { closeRead(); }
            catch(IOException e) {}

            try { closeWrite(); }
            catch(IOException e) {}
        }
    }


    /**
     * Copies a chunk of data from the given {@link RandomAccessInputStream} to the specified
     * {@link RandomAccessOutputStream}.
     *
     * @param rais the source stream
     * @param raos the destination stream
     * @param srcOffset start of data in the source stream
     * @param destOffset start of data in the destination stream
     * @param length number of bytes to copy
     * @throws IOException if an error occurred while copying data
     */
    private static void copyChunk(RandomAccessInputStream rais, RandomAccessOutputStream raos, long srcOffset, long destOffset, long length) throws IOException {
//if(Debug.ON) Debug.trace("srcOffset="+srcOffset+" destOffset="+destOffset+" length="+length);

        rais.seek(srcOffset);
        raos.seek(destOffset);

        // Use BufferPool to avoid excessive memory allocation and garbage collection
        byte buffer[] = BufferPool.getBuffer(WRITE_BUFFER_SIZE);

        try {
            long remaining = length;
            int nbBytes;
            while(remaining>0) {
                nbBytes = (int)(remaining< WRITE_BUFFER_SIZE ?remaining: WRITE_BUFFER_SIZE);
                rais.readFully(buffer, 0, nbBytes);
                raos.write(buffer, 0, nbBytes);
                remaining -= nbBytes;
            }
        }
        finally {
            BufferPool.releaseBuffer(buffer);
        }
    }


    /**
     * Writes the specified number of zero bytes to the given <code>OutputStream</code>. 
     *
     * @param out the OutputStream to write zeros to
     * @param len number of zero bytes to write
     * @throws IOException if an error occurred while writing zeros
     */
    private static void writeZeros(OutputStream out, long len) throws IOException {
//if(Debug.ON) Debug.trace("writing "+len+" zeros");

        // Use BufferPool to avoid excessive memory allocation and garbage collection
        int bufferLen = WRITE_BUFFER_SIZE;
        byte buffer[] = BufferPool.getBuffer(bufferLen);

        try {
            long remaining = len;
            int nbWrite;
            while(remaining>0) {
                nbWrite = (int)(remaining>bufferLen?bufferLen:remaining);
                out.write(buffer, 0, nbWrite);
                remaining -= nbWrite;
            }
        }
        finally {
            BufferPool.releaseBuffer(buffer);
        }
    }



    /**
     * Calulcates the data offset of the entry which starts at the given ZipEntryInfo.headerOffset and stores the result
     * in ZipEntryInfo.dataOffset. After calling this method, the RandomAccessInputStream will be positionned at the
     * beginning of the filename field.
     *
     * @param entryInfo the ZipEntryInfo object in which to store the data offset
     * @throws IOException if an unexcepted I/O error occurred
     */
    private void calculateDataOffset(ZipEntryInfo entryInfo) throws IOException {
        // Skip the following fields:
        //  local file header signature     4 bytes
        //  version needed to extract       2 bytes
        //  general purpose bit flag        2 bytes
        //  compression method              2 bytes
        //  last mod file time              2 bytes
        //  last mod file date              2 bytes
        //  crc-32                          4 bytes
        //  compressed size                 4 bytes
        //  uncompressed size               4 bytes
        // Total nb of bytes to skip:      26

        long dataOffset = entryInfo.headerOffset + 26;
        rais.seek(dataOffset);

        // Advance the offset of the filename field's length (plus the filename length field: 2 bytes)
        byte[] b = new byte[2];
        rais.readFully(b);
        dataOffset += 2 + ZipShort.getValue(b);

        // Advance the offset of the extra field's length (plus the extra field length field: 2 bytes)
        rais.readFully(b);
        dataOffset += 2 + ZipShort.getValue(b);

        entryInfo.dataOffset = dataOffset;
    }


    /** Combined length of all constant-size fields of the Central File Header */
    private static final int CFH_LEN =
        /* version made by                 */ 2
        /* version needed to extract       */ + 2
        /* general purpose bit flag        */ + 2
        /* compression method              */ + 2
        /* last mod file time              */ + 2
        /* last mod file date              */ + 2
        /* crc-32                          */ + 4
        /* compressed size                 */ + 4
        /* uncompressed size               */ + 4
        /* filename length                 */ + 2
        /* extra field length              */ + 2
        /* file comment length             */ + 2
        /* disk number start               */ + 2
        /* internal file attributes        */ + 2
        /* external file attributes        */ + 4
        /* relative offset of local header */ + 4;

    /**
     * Reads the central directory of the given archive and populates
     * the internal tables with ZipEntry instances.
     *
     * <p>The ZipEntrys will know all data that can be obtained from
     * the central directory alone, but not the data that requires the
     * local file header or additional data to be read.</p>
     *
     * @throws IOException if an I/O error occurred
     * @throws ZipException if this file is not a valid Zip file
     */
    private void parseCentralDirectory() throws IOException, ZipException {

        positionAtCentralDirectory();

        byte[] cfh = new byte[CFH_LEN];

        byte[] signatureBytes = new byte[4];
        rais.readFully(signatureBytes);
        long sig = ZipLong.getValue(signatureBytes);
        final long cfhSig = ZipLong.getValue(CFH_SIG);

        ByteArrayOutputStream encodingAccumulator = new ByteArrayOutputStream();

        while (sig == cfhSig) {
            ZipEntryInfo entryInfo = new ZipEntryInfo();

            // Set Central directory file header offset
            entryInfo.centralHeaderOffset = rais.getOffset() - 4;     // 4 for the header signature

            rais.readFully(cfh);
            int off = 0;
            ZipEntry ze = new ZipEntry();

            int versionMadeBy = ZipShort.getValue(cfh, off);
            off += 2;
            ze.setPlatform((versionMadeBy >> 8) & 0x0F);

            off += 2; // skip version info

            int gp = ZipShort.getValue(cfh, off);   // General purpose bit flag
            boolean isUTF8 = (gp&0x800)!=0;         // Tests if bit 11 is set, signaling UTF-8 is used for filename and comment

            if(isUTF8)
                entryInfo.encoding = UTF_8;
            // Else encoding will be guessed and set later

            entryInfo.hasDataDescriptor = (gp&8)!=0;

            off += 2;
//if(Debug.ON) Debug.trace("hasDataDescriptor="+entryInfo.hasDataDescriptor);

            int method = ZipShort.getValue(cfh, off);
            // Note: ZipEntry#setMethod(int) will throw a java.lang.InternalError ("invalid compression method") if the
            // method is different from DEFLATED or STORED (happens with IMPLODED for example).
            // Thus we check the method ourselves to fail gracefully.
            if(method!=DEFLATED && method!=STORED)
                throw new ZipException("Unsupported compression method");

            ze.setMethod(method);
            off += 2;

            // FIXME this is actually not very cpu cycles friendly as we are converting from
            // dos to java while the underlying Sun implementation will convert
            // from java to dos time for internal storage...
            long time = dosToJavaTime(ZipLong.getValue(cfh, off));
            ze.setTime(time);
            off += 4;

            ze.setCrc(ZipLong.getValue(cfh, off));
            off += 4;

            ze.setCompressedSize(ZipLong.getValue(cfh, off));
            off += 4;

            ze.setSize(ZipLong.getValue(cfh, off));
            off += 4;

            int fileNameLen = ZipShort.getValue(cfh, off);
            off += 2;

            int extraLen = ZipShort.getValue(cfh, off);
            off += 2;

            int commentLen = ZipShort.getValue(cfh, off);
            off += 2;

            off += 2; // disk number

            ze.setInternalAttributes(ZipShort.getValue(cfh, off));
            off += 2;

            ze.setExternalAttributes(ZipLong.getValue(cfh, off));
            off += 4;

            // Read filename bytes
            byte[] filename = new byte[fileNameLen];
            rais.readFully(filename);

            if(isUTF8) {
                // We know the filename is encoded in UTF-8, set it now
                ze.setName(getString(filename, UTF_8));
//if(Debug.ON) Debug.trace("using UTF-8, filename="+ze.getName());
            }
            else {
                // Keep the filename bytes, String will be encoded after
                entryInfo.filename = filename;
                // Accumulate those unidentified bytes for encoding detection
                feedEncodingAccumulator(encodingAccumulator, filename);
            }

            // Offset to local file header
            entryInfo.headerOffset = ZipLong.getValue(cfh, off);
            // data offset will be filled later

            // Read and set extra bytes
            byte extra[] = new byte[extraLen];
            rais.readFully(extra);
            ze.setExtra(extra);

            // Read comment bytes
            byte[] comment = new byte[commentLen];
            rais.readFully(comment);

            if(isUTF8) {
                // We know the comment is encoded in UTF-8, set it now
                ze.setComment(getString(comment, UTF_8));
//if(Debug.ON) Debug.trace("using UTF-8, comment="+ze.getComment());
            }
            else {
                // Keep the comment bytes, String will be encoded after
                entryInfo.comment = comment;
                // Accumulate those unidentified bytes for encoding detection
                feedEncodingAccumulator(encodingAccumulator, comment);
            }

            entryInfo.centralHeaderLen = 46 + fileNameLen + extraLen + commentLen;

            // Add the new entry to the internal lists
            ze.setEntryInfo(entryInfo);
            entries.add(ze);
            nameMap.put(ze.getName(), ze);

            // Swallow signature
            rais.readFully(signatureBytes);
            sig = ZipLong.getValue(signatureBytes);
        }

        if(encodingAccumulator.size()>0) {
            int nbEntries = entries.size();
            String guessedEncoding = EncodingDetector.detectEncoding(encodingAccumulator.toByteArray());
//            if(Debug.ON) Debug.trace("guessedEncoding="+guessedEncoding);

            ZipEntry entry;
            ZipEntryInfo entryInfo;
            for(int i=0; i<nbEntries; i++) {
                entry = (ZipEntry)entries.elementAt(i);
                entryInfo = entry.getEntryInfo();

                if(entryInfo.encoding != null)
                    continue;

                entryInfo.encoding = guessedEncoding;

                entry.setName(getString(entryInfo.filename, guessedEncoding));
                entryInfo.filename = null;
//if(Debug.ON) Debug.trace("guessedEncoding="+guessedEncoding+" filename="+entry.getName());

                entry.setComment(getString(entryInfo.comment, guessedEncoding));
                entryInfo.comment = null;
//if(Debug.ON) Debug.trace("guessedEncoding="+guessedEncoding+" comment="+entry.getComment());
            }
        }
    }

    /**
     * Feeds the given bytes to the encoding accumulator (used for encoding detection). The bytes will be ignored if
     * the accumulator has enough data already.
     *
     * @param encodingAccumulator the ByteArrayOutputStream that holds filename and comment bytes
     * @param bytes the bytes to feed to the encoding accumulator
     * @throws IOException if an I/O occurs (should never happen)
     */
    private static void feedEncodingAccumulator(ByteArrayOutputStream encodingAccumulator, byte bytes[]) throws IOException {
        if(encodingAccumulator.size() < EncodingDetector.MAX_RECOMMENDED_BYTE_SIZE)
            encodingAccumulator.write(bytes);
        // Else accumulator has enough bytes, ignore the given bytes
    }

    /** Minimum possible size for the End Of Central Directory record (no comment) */
    private static final int MIN_EOCD_SIZE =
        /* end of central dir signature    */ 4
        /* number of this disk             */ + 2
        /* number of the disk with the     */
        /* start of the central directory  */ + 2
        /* total number of entries in      */
        /* the central dir on this disk    */ + 2
        /* total number of entries in      */
        /* the central dir                 */ + 2
        /* size of the central directory   */ + 4
        /* offset of start of central      */
        /* directory with respect to       */
        /* the starting disk number        */ + 4
        /* zipfile comment length          */ + 2
        /* zipfile comment                 */ + 0;

    /** Maximum possible size for the End Of Central Directory record (max comment size: 65535) */
    private static final int MAX_EOCD_SIZE =
        /* end of central dir signature    */ 4
        /* number of this disk             */ + 2
        /* number of the disk with the     */
        /* start of the central directory  */ + 2
        /* total number of entries in      */
        /* the central dir on this disk    */ + 2
        /* total number of entries in      */
        /* the central dir                 */ + 2
        /* size of the central directory   */ + 4
        /* offset of start of central      */
        /* directory with respect to       */
        /* the starting disk number        */ + 4
        /* zipfile comment length          */ + 2
        /* zipfile comment                 */ + 65535;

    private static final int CFD_LOCATOR_OFFSET =
        /* end of central dir signature    */ 4
        /* number of this disk             */ + 2
        /* number of the disk with the     */
        /* start of the central directory  */ + 2
        /* total number of entries in      */
        /* the central dir on this disk    */ + 2
        /* total number of entries in      */
        /* the central dir                 */ + 2
        /* size of the central directory   */ + 4;

    /**
     * Searches for the end of central dir record, parses
     * it and positions the stream at the first central directory
     * record.
     *
     * @throws IOException if an I/O error occurs
     * @throws ZipException if the end of central directory signature could not be found. This can be interpreted as the
     * underlying file not being a Zip file
     */
    private void positionAtCentralDirectory() throws IOException, ZipException {
        boolean found = false;
        long length = rais.getLength();
        long off = length - MIN_EOCD_SIZE;

        // Minimum offset at which the EOCD signature can be located, based of the maximum comment length (65535)
        long minSigOffset = length-MAX_EOCD_SIZE;

        if (off >= 0) {
            int curr;
            rais.seek(off);

            while (off>=0 && off>=minSigOffset && (curr=rais.read())!=-1) {
                if (curr == EOCD_SIG[0]) {
                    curr = rais.read();
                    if (curr == EOCD_SIG[1]) {
                        curr = rais.read();
                        if (curr == EOCD_SIG[2]) {
                            curr = rais.read();
                            if (curr == EOCD_SIG[3]) {
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if(--off>0)     // Avoids negative seeks in case the sig is not found (would throw an IOException)
                    rais.seek(off);
            }
        }

        if (!found) {
            throw new ZipException("Invalid Zip stream (EOCD signature not found)");
        }

        // Parse the offset to the central directory start
        rais.seek(off + CFD_LOCATOR_OFFSET);
        byte[] cfdOffset = new byte[4];
        rais.readFully(cfdOffset);

        long cdStart = ZipLong.getValue(cfdOffset);

        // Fetch the global zip file comment
        byte[] commentLen = new byte[2];
        rais.readFully(commentLen);
        byte commentBytes[] = new byte[ZipShort.getValue(commentLen)];
        rais.readFully(commentBytes);
        // Try to guess the comment's encoding, there is no other way of knowing the encoding as
        // the Zip format doesn't provide any information other than the bytes themselves (not even a UTF-8 byte)
        comment = getString(commentBytes, EncodingDetector.detectEncoding(commentBytes));

        // Seek to the start of the central directory
        rais.seek(cdStart);
    }


    /*
     * Converts DOS time (Epoch=1980) to Java time (Epoch=1970).
     */
    private static long dosToJavaTime(long dosTime) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, (int) ((dosTime >> 25) & 0x7f) + 1980);
        cal.set(Calendar.MONTH, (int) ((dosTime >> 21) & 0x0f) - 1);
        cal.set(Calendar.DATE, (int) (dosTime >> 16) & 0x1f);
        cal.set(Calendar.HOUR_OF_DAY, (int) (dosTime >> 11) & 0x1f);
        cal.set(Calendar.MINUTE, (int) (dosTime >> 5) & 0x3f);
        cal.set(Calendar.SECOND, (int) (dosTime << 1) & 0x3e);

        return cal.getTime().getTime();
    }


    /**
     * Creates and returns a String created using the given bytes and encoding.
     * If the specified encoding isn't supported, the platform's default encoding will be used.
     *
     * @param bytes the byte array to transform
     * @param encoding the encoding to use to instanciate the String
     * @return String instance that was created with the given encoding
     */
    private static String getString(byte[] bytes, String encoding) {
        if(bytes.length==0)
            return "";

        if(encoding!=null) {
            try {
                return new String(bytes, encoding);
            }
            catch(UnsupportedEncodingException e) {
                if(Debug.ON) Debug.trace("Error: unsupported encoding: "+encoding+" , falling back to default encoding");
            }
        }

        // Fall back to platform's default encoding
        return new String(bytes);
    }


    ///////////////////
    // Inner classes //
    ///////////////////
    
    /**
     * InputStream that delegates requests to the underlying
     * RandomAccessFile, making sure that only bytes from a certain
     * range can be read.
     */
    private static class BoundedInputStream extends InputStream {

        private final RandomAccessInputStream rais;

        private long remaining;
        private long loc;
        private boolean addDummyByte = false;

        BoundedInputStream(RandomAccessInputStream rais, long start, long remaining) {
            this.rais = rais;
            this.remaining = remaining;
            loc = start;
        }

        public int read() throws IOException {
            if (remaining-- <= 0) {
                if (addDummyByte) {
                    addDummyByte = false;
                    return 0;
                }
                return -1;
            }
            synchronized (rais) {
                rais.seek(loc++);
                return rais.read();
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                if (addDummyByte) {
                    addDummyByte = false;
                    b[off] = 0;
                    return 1;
                }
                return -1;
            }

            if (len <= 0) {
                return 0;
            }

            if (len > remaining) {
                len = (int) remaining;
            }
            int ret;
            synchronized (rais) {
                rais.seek(loc);
                ret = rais.read(b, off, len);
            }
            if (ret > 0) {
                loc += ret;
                remaining -= ret;
            }
            return ret;
        }

        public void close() throws IOException {
            rais.close();
        }

        /**
         * Inflater needs an extra dummy byte for nowrap - see Inflater's javadocs.
         */
        void addDummy() {
            addDummyByte = true;
        }
    }

}

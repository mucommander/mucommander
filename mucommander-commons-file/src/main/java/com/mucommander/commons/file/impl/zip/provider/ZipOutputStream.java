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

package com.mucommander.commons.file.impl.zip.provider;

import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

/**
 * Reimplementation of {@link java.util.zip.ZipOutputStream java.util.zip.ZipOutputStream} that handles the extended
 * functionality of this package, especially internal/external file attributes and extra fields with different layouts
 * for local file data and central directory entries.
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Apache Ant, Maxence Bernard
 */
public class ZipOutputStream extends OutputStream implements ZipConstants {

    /** Current entry */
    private ZipEntry entry;

    /** Current ZipEntryOutputStream corresponding to the entry being written */
    private ZipEntryOutputStream zeos;

    /** Additional info about current entry */
    private ZipEntryInfo entryInfo;

    /** The global zip file comment */
    private String comment = "";

    /** Compression level for zip entries */
    private int level = DEFAULT_DEFLATER_COMPRESSION;

    /** Compression method zip entries */
    private int method = DEFLATED;

    /** Deflater instance that is used to compress DEFLATED entries */
    protected Deflater deflater = new Deflater(level, true);

    /** Buffer used by Deflater to deflate data */
    protected byte[] deflaterBuf;

    /** List of zip entries written so far */
    private Vector<ZipEntry> entries = new Vector<ZipEntry>();

    /** Count the bytes written to out */
    private long written = 0;

    /** The encoding to use for filenames and the file comment, UTF-8 by default */
    private String encoding = UTF_8;

    /** Holds byte buffer instance used to convert short and longs, avoids creating lots of small arrays */
    private ZipBuffer zipBuffer = new ZipBuffer();

    /** 0 (zero) as ZipShort */
    private static final byte[] SHORT_0 = ZipShort.getBytes(0);

    /** 0 (zero) as ZipLong */
    private static final byte[] LONG_0 = ZipLong.getBytes(0);

    /** Three ZipLong zeros */
    private static final byte[] LONG_TRIPLE_0 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /** 8 as ZipShort */
    private static final byte[] SHORT_8 = ZipShort.getBytes(8);

    /** 10 as ZipShort */
    private static final byte[] SHORT_10 = ZipShort.getBytes(10);

    /** 20 as ZipShort */
    private static final byte[] SHORT_20 = ZipShort.getBytes(20);

    /** 2048 as ZipShort */
    private static final byte[] SHORT_2048 = ZipShort.getBytes(2048);

    /** 2056 as ZipShort */
    private static final byte[] SHORT_2056 = ZipShort.getBytes(2056);


    /**
     * The underlying stream this ZipOutputStream writes zip-compressed data to.
     */
    protected OutputStream out;

    /**
     * Is the underlying stream a RandomAccessOutputStream? Avoids excessive instanceof comparisons.
     */
    private boolean hasRandomAccess;


    /**
     * Creates a new <code>ZipOutputStream</code> that writes Zip-compressed data to the given <code>OutputStream</code>.
     * If a {@link RandomAccessOutputStream} is supplied, the Zip entries will be written without data descriptor,
     * which will yield a slightly smaller file.
     *
     * @param out the underlying OutputStream stream where compressed data is written to
     */
    public ZipOutputStream(OutputStream out) {
        this.out = out;
        this.hasRandomAccess = out instanceof RandomAccessOutputStream;

        // Use BufferPool to avoid excessive memory allocation and garbage collection.
        deflaterBuf = BufferPool.getByteArray(DEFAULT_DEFLATER_BUFFER_SIZE);
    }


    /**
     * This method indicates whether this archive is writing to a {@link RandomAccessOutputStream}.
     *
     * @return true if seekable
     */
    public boolean isSeekable() {
        return hasRandomAccess;
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * <p>For a list of possible values see <a
     * href="http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html">http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html</a>.
     * Defaults to the platform's default character encoding.</p>
     *
     * @param encoding the encoding value
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * @return null if using the platform's default character encoding.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns <code>true</code> if the given encoding is either "UTF-8", "UTF8" (case-insensitive) or
     * <code>null</code>.
     *
     * @param encoding the encoding String to test
     * @return true if the given encoding is either "UTF-8", "UTF8" or null
     */
    private static boolean isUTF8(String encoding) {
        return encoding==null || encoding.equalsIgnoreCase("UTF-8") || encoding.equalsIgnoreCase("UTF8");
    }

    /**
     * Finishs writing the contents and closes this as well as the
     * underlying stream.
     *
     * @throws IOException on error
     */
    public void finish() throws IOException {
        closeEntry();
        long cdOffset = written;
        int nbEntries = entries.size();
        ZipEntry ze;
        for (int i=0; i <nbEntries; i++) {
            ze =  entries.elementAt(i);
            written += writeCentralFileHeader(ze, out, encoding, ze.getEntryInfo().headerOffset, !hasRandomAccess, zipBuffer);
        }
        long cdLength = written - cdOffset;
        writeCentralDirectoryEnd(out, nbEntries, cdLength, cdOffset, comment, encoding, zipBuffer);
        entries.removeAllElements();
    }

    /**
     * Writes all necessary data for this entry. This method must be called after an entry opened by
     * {@link #putNextEntry(ZipEntry)} has finished being written.
     *
     * @throws IOException on error
     */
    public void closeEntry() throws IOException {
        if (entry == null)
            return;

        finalizeEntryData(entry, zeos, out, !hasRandomAccess, zipBuffer);
        written += entry.getCompressedSize();

        if(!hasRandomAccess)
            written += writeDataDescriptor(entry, out, zipBuffer);

        entry = null;
        entryInfo = null;

        zeos.close();
        zeos = null;
    }

    /**
     * Writes the size and CRC information of an entry. This method is to be called right after a file entry's data
     * has been written.
     *
     * <p>The size and CRC information is written to the given <code>OutputStream</code>, either as a data descriptor or
     * in the entry's local file header, and is set in the given {@link ZipEntry} instance.
     *
     * @param entry the entry
     * @param zeos the Zip entry's output stream
     * @param out the
     * @param useDataDescriptor if true, a data descriptor will be written to out. If false, size and CRC information
     * will be written in the local file header (requires out to be a RandomAccessOutputStream).
     * @param zipBuffer a ZipBuffer instance used to convert integer values to Zip variants
     * @throws IOException if an I/O error occurred
     */
    protected static void finalizeEntryData(ZipEntry entry, ZipEntryOutputStream zeos, OutputStream out, boolean useDataDescriptor, ZipBuffer zipBuffer) throws IOException {
        long crc = zeos.getCrc();

        if (entry.getMethod() == DEFLATED) {
            ((DeflatedOutputStream)zeos).finishDeflate();

            entry.setSize(adjustToLong(zeos.getTotalIn()));
            long compressedSize = adjustToLong(zeos.getTotalOut());
            entry.setCompressedSize(compressedSize);
            entry.setCrc(crc);
        }
        else {      // Method is STORED
            long size = zeos.getTotalOut();

            entry.setSize(size);
            entry.setCompressedSize(size);
            entry.setCrc(crc);
        }

        // If random access output, write the local file header containing
        // the correct CRC and compressed/uncompressed sizes
        if (!useDataDescriptor) {
            RandomAccessOutputStream raos = (RandomAccessOutputStream)out;

            long save = raos.getOffset();

            raos.seek(entry.getEntryInfo().headerOffset + 14);
            raos.write(ZipLong.getBytes(entry.getCrc(), zipBuffer.longBuffer));
            raos.write(ZipLong.getBytes(entry.getCompressedSize(), zipBuffer.longBuffer));
            raos.write(ZipLong.getBytes(entry.getSize(), zipBuffer.longBuffer));
            raos.seek(save);
        }
    }

    /**
     * Start writing the given entry. The entry is written by calling the <code>write()</code> of this class.
     * When the entry has finished being written, {@link #closeEntry()} must be called.
     *
     * @param ze the entry to write
     * @throws IOException on error
     */
    public void putNextEntry(ZipEntry ze) throws IOException {
        closeEntry();

        entry = ze;
        entryInfo = new ZipEntryInfo();
        entry.setEntryInfo(entryInfo);
        entries.addElement(entry);

        int entryMethod = entry.getMethod();
        if (entryMethod == -1) {
            // method not specified in the entry, use the one set in this ZipOutputStream
            entryMethod = method;
            entry.setMethod(method);
        }

        if (entry.getTime() == -1) {
            // date not specified in the entry, set it to now
            entry.setTime(System.currentTimeMillis());
        }

        if(entryMethod == DEFLATED) {
            deflater.reset();
            deflater.setLevel(level);

            zeos = new DeflatedOutputStream(out, deflater, deflaterBuf);
        }
        else {
            zeos = new StoredOutputStream(out);
        }

        entryInfo.headerOffset = written;
        written += writeLocalFileHeader(entry, out, encoding, !hasRandomAccess, zipBuffer);
        entryInfo.dataOffset = written;
    }

    /**
     * Sets the file comment.
     *
     * @param comment the comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the compression level for subsequent entries.
     *
     * <p>Default is Deflater.DEFAULT_COMPRESSION.</p>
     *
     * @param level the compression level.
     * @throws IllegalArgumentException if an invalid compression level is specified.
     */
    public void setLevel(int level) {
        if (level < Deflater.DEFAULT_COMPRESSION
            || level > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException(
                "Invalid compression level: " + level);
        }
        this.level = level;
    }

    /**
     * Sets the default compression method for subsequent entries.
     *
     * <p>Default is DEFLATED.</p>
     *
     * @param method an <code>int</code> from java.util.zip.ZipEntry
     */
    public void setMethod(int method) {
        this.method = method;
    }

    /**
     * Writes the local file header entry.
     *
     * @param ze the entry to write
     * @param out the OutputStream to write the header to
     * @param encoding the encoding to use for writing the entry's filename. If UTF-8 is used, the general purpose bit
     * flag will be set accordingly.
     * @param useDataDescriptor indicates whether a data descriptor will follow the file entry's data. The general
     * purpose bit flag will be set accordingly.
     * @param zipBuffer a ZipBuffer instance used to convert integer values to Zip variants
     * @return the size (number of bytes) of the written local file header
     * @throws IOException if an I/O error occurred
     */
    protected static long writeLocalFileHeader(ZipEntry ze, OutputStream out, String encoding, boolean useDataDescriptor, ZipBuffer zipBuffer) throws IOException {
        out.write(LFH_SIG);
        // written += 4;

        int zipMethod = ze.getMethod();

        // version needed to extract
        // general purpose bit flag
        writeVersionAndGPBF(out, encoding, useDataDescriptor);
        // nbWritten += 4;

        // compression method
        out.write(ZipShort.getBytes(zipMethod, zipBuffer.shortBuffer));
        // written += 2;

        // last mod. time and date
        out.write(ZipLong.getBytes(ze.getDosTime(), zipBuffer.longBuffer));
        //  written += 4;

        // CRC
        // compressed length
        // uncompressed length

        // this information is not known at this stage so it will be set after the data has been written,
        // either in the data descriptor (if used), or here by seeking (requires random access)
        out.write(LONG_TRIPLE_0);   // 12 zero bytes
        // written += 12;

        // file name length
        byte[] name = getBytes(ze.getName(), encoding);
        out.write(ZipShort.getBytes(name.length, zipBuffer.shortBuffer));
        // written += 2;

        // extra field length
        byte[] extra = ze.getLocalFileDataExtra();
        out.write(ZipShort.getBytes(extra.length, zipBuffer.shortBuffer));
        // written += 2;

        // Number of bytes written by this method so far
        long written = 30;

        // file name
        out.write(name);
        written += name.length;

        // extra field
        out.write(extra);
        written += extra.length;

        return written;
    }

    /**
     * Writes the data descriptor, using the CRC, compressed and uncompressed size attributes contained in the
     * given ZipEntry.
     * The length of the field is returned, it is always 16 bytes.
     *
     * @param ze the entry for which to write the data descriptor
     * @param out the OutputStream where to write the data descriptor to
     * @param zipBuffer a ZipBuffer instance used to convert integer values to Zip variants
     * @return the number of bytes that were written, i.e. the size of the data descriptor (16 bytes)
     * @throws IOException if an I/O error occurred
     */
    protected static long writeDataDescriptor(ZipEntry ze, OutputStream out, ZipBuffer zipBuffer) throws IOException {
        out.write(DD_SIG);
        out.write(ZipLong.getBytes(ze.getCrc(), zipBuffer.longBuffer));
        out.write(ZipLong.getBytes(ze.getCompressedSize(), zipBuffer.longBuffer));
        out.write(ZipLong.getBytes(ze.getSize(), zipBuffer.longBuffer));

        return 16;
    }

    /**
     * Writes central file header's 'Version made by' field, using the platform contained in the given ZipEntry.
     * The length of the field is returned, it is always 2 bytes.
     *
     * @param ze the entry for which to write the 'Version made by' field
     * @param out the OutputStream where to write the field
     * @param zipBuffer a ZipBuffer instance used to convert integer values to Zip variants
     * @return the number of bytes that were written, i.e. the size of the 'Version made by' field (2 bytes)
     * @throws IOException if an I/O error occurred
     */
    protected static long writeVersionMadeBy(ZipEntry ze, OutputStream out, ZipBuffer zipBuffer) throws IOException {
        out.write(ZipShort.getBytes((ze.getPlatform() << 8) | 20, zipBuffer.shortBuffer));

        return 2;
    }

    /**
     * Writes the central file header for the given entry.
     *
     * @param ze the entry for which to write the central file header
     * @param out the OutputStream to write the central file header to
     * @param encoding the encoding to use for writing the filename and optional comment
     * @param localFileHeaderOffset the offset to the local file header start
     * @param useDataDescriptor true if a data descriptor is used for the entry
     * @param zipBuffer a ZipBuffer instance used to convert integer values to Zip variants
     * @throws IOException if an I/O error occurred
     * @return the number of bytes that were written, i.e. the size of the central file header 
     */
    protected static long writeCentralFileHeader(ZipEntry ze, OutputStream out, String encoding, long localFileHeaderOffset, boolean useDataDescriptor, ZipBuffer zipBuffer) throws IOException {
        out.write(CFH_SIG);
        // nbWritten += 4;

        // version made by
        writeVersionMadeBy(ze, out, zipBuffer);
        // nbWritten += 2;

        // version needed to extract
        // general purpose bit flag
        writeVersionAndGPBF(out, encoding, useDataDescriptor);
        // nbWritten += 4;

        // compression method
        out.write(ZipShort.getBytes(ze.getMethod(), zipBuffer.shortBuffer));
        // nbWritten += 2;

        // last mod. time and date
        out.write(ZipLong.getBytes(ze.getDosTime(), zipBuffer.longBuffer));
        // nbWritten += 4;

        // CRC
        // compressed length
        // uncompressed length
        out.write(ZipLong.getBytes(ze.getCrc(), zipBuffer.longBuffer));
        out.write(ZipLong.getBytes(ze.getCompressedSize(), zipBuffer.longBuffer));
        out.write(ZipLong.getBytes(ze.getSize(), zipBuffer.longBuffer));
        // nbWritten += 12;

        // file name length
        byte[] name = getBytes(ze.getName(), encoding);
        out.write(ZipShort.getBytes(name.length, zipBuffer.shortBuffer));
        // nbWritten += 2;

        // extra field length
        byte[] extra = ze.getCentralDirectoryExtra();
        out.write(ZipShort.getBytes(extra.length, zipBuffer.shortBuffer));
        // nbWritten += 2;

        // file comment length
        String comm = ze.getComment();
        if (comm == null) {
            comm = "";
        }
        byte[] commentB = getBytes(comm, encoding);
        out.write(ZipShort.getBytes(commentB.length, zipBuffer.shortBuffer));
        // nbWritten += 2;

        // disk number start
        out.write(SHORT_0);
        // nbWritten += 2;

        // internal file attributes
        out.write(ZipShort.getBytes(ze.getInternalAttributes(), zipBuffer.shortBuffer));
        // nbWritten += 2;

        // external file attributes
        out.write(ZipLong.getBytes(ze.getExternalAttributes(), zipBuffer.longBuffer));
        // nbWritten += 4;

        // relative offset of LFH
        out.write(ZipLong.getBytes(localFileHeaderOffset, zipBuffer.longBuffer));
        // nbWritten += 4;

        long nbWritten = 46;

        // file name
        out.write(name);
        nbWritten += name.length;

        // extra field
        out.write(extra);
        nbWritten += extra.length;

        // file comment
        out.write(commentB);
        nbWritten += commentB.length;

        return nbWritten;
    }


    /**
     * Writes the 'version needed to extract' (2 bytes) and 'general purpose bit flag' (2 bytes) fields.
     *
     * @param out the OutputStream to write the fields to
     * @param encoding the encoding used for writing the filename and optional comment
     * @param useDataDescriptor true if a data descriptor is used for the entry
     * @return the number of bytes that were written, i.e. 4
     * @throws IOException if an I/O error occurred
     */
    protected static long writeVersionAndGPBF(OutputStream out, String encoding, boolean useDataDescriptor) throws IOException {
        boolean isUTF8 = isUTF8(encoding);

        // General purpose bit flag :
        // Bit 11 signals UTF-8 is used
        // Bit 3 signals a data descriptor is used

        if (useDataDescriptor) {
            // requires version 2 as we are going to store length info in the data descriptor
            out.write(SHORT_20);

            // General purpose bit flag
            out.write(isUTF8?
                SHORT_2056                  // Bit 3 | Bit 11 = 2056
                :SHORT_8                    // Bit 3          = 8
            );
        }
        else {
            // Version
            out.write(SHORT_10);

            // General purpose bit flag
            out.write(isUTF8?
                SHORT_2048                  // Bit 11 = 2048
                :SHORT_0                    // No bit set
            );
        }

        return 4;
    }


    /**
     * Writes the end of the central directory record.
     *
     * @param out the OutputStream to write the end of the central directory record to
     * @param nbEntries number of entries the Zip file contains
     * @param cdLength length (in bytes) of the central directory record
     * @param cdOffset offset from the beginning of the Zip file to the start of the central directory record
     * @param comment the optional Zip file comment
     * @param encoding the encoding to use for writing the optional Zip comment
     * @param zipBuffer a ZipBuffer instance used to convert integer values to Zip variants
     * @throws IOException if an I/O error occurred
     */
    protected static void writeCentralDirectoryEnd(OutputStream out, int nbEntries, long cdLength, long cdOffset, String comment, String encoding, ZipBuffer zipBuffer)
            throws IOException {

        out.write(EOCD_SIG);

        // disk numbers
        out.write(LONG_0);      // 2x SHORT_0

        // number of entries
        ZipShort.getBytes(nbEntries, zipBuffer.shortBuffer);
        out.write(zipBuffer.shortBuffer);
        out.write(zipBuffer.shortBuffer);

        // length and location of CD
        out.write(ZipLong.getBytes(cdLength, zipBuffer.longBuffer));
        out.write(ZipLong.getBytes(cdOffset, zipBuffer.longBuffer));

        // ZIP file comment
        byte[] data = getBytes(comment, encoding);
        out.write(ZipShort.getBytes(data.length, zipBuffer.shortBuffer));
        out.write(data);
    }

    /**
     * Retrieve the bytes for the given String in the encoding set for
     * this Stream.
     * @param name the string to get bytes from
     * @param encoding the encoding the string is encoded with
     * @return the bytes as a byte array
     * @throws ZipException on error
     */
    protected static byte[] getBytes(String name, String encoding) throws ZipException {
        if (encoding == null) {
            return name.getBytes();
        } else {
            try {
                return name.getBytes(encoding);
            } catch (UnsupportedEncodingException uee) {
                throw new ZipException(uee.getMessage());
            }
        }
    }

    /**
     * Returns a long that is the unsigned intepretation of the given (signed) int.
     *
     * @param i the value to treat as unsigned int
     * @return the unsigned int as a long
     */
    protected static long adjustToLong(int i) {
        return i & 0xFFFFFFFFl;
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    /**
     * Writes the given bytes to the current Zip entry opened with {@link #putNextEntry(ZipEntry)}, using the entry's
     * compression method. If no entry is currently open, the bytes will be written as-is to the underlying
     * <code>OutputStream</code>.
     *
     * @param b the byte array to write
     * @param offset the start position to write from
     * @param length the number of bytes to write
     * @throws IOException on error
     */
    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        (zeos==null?out:zeos).write(b, offset, length);
    }

    /**
     * Writes the given bytes to the current Zip entry opened with {@link #putNextEntry(ZipEntry)}, using the entry's
     * compression method. If no entry is currently open, the bytes will be written as-is to the underlying
     * <code>OutputStream</code>.
     *
     * @param b the byte array to write
     * @throws IOException on error
     */
    @Override
    public void write(byte[] b) throws IOException {
        (zeos==null?out:zeos).write(b, 0, b.length);
    }

    /**
     * Writes a single byte to the current Zip entry opened with {@link #putNextEntry(ZipEntry)}, using the entry's
     * compression method. If no entry is currently open, the bytes will be written as-is to the underlying
     * <code>OutputStream</code>.
     *
     * <p>Delegates to the three arg method.</p>
     * @param b the byte to write
     * @throws IOException on error
     */
    @Override
    public void write(int b) throws IOException {
        (zeos==null?out:zeos).write(b);
    }

    /**
     * Closes this output stream and releases any system resources associated with the stream.
     *
     * @exception IOException  if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        finish();

        if(deflaterBuf !=null) {         // Only if close() has not already been called already
            BufferPool.releaseByteArray(deflaterBuf);
            deflaterBuf = null;
        }

        out.close();
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }
}

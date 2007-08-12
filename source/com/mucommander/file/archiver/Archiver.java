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


package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;
import com.mucommander.io.BufferedRandomOutputStream;
import com.mucommander.io.RandomAccessOutputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Archiver is an abstract class that represents a generic file archiver and abstracts the underlying
 * compression method and specifics of the format.
 *
 * <p>Subclasses implement specific archive formats (Zip, Tar...) but cannot be instanciated directly.
 * Instead, the <code>getArchiver</code> methods can be used to retrieve an Archiver
 * instance for a specified archive format. A list of available archive formats can be dynamically retrieved
 * using {@link #getFormats(boolean) getFormats}.
 *
 * <p>Archive formats fall into 2 categories:
 * <ul>
 * <li><i>Single entry formats:</i> Formats that can only store one entry without any directory structure, e.g. Gzip or Bzip2.
 * <li><i>Many entries formats:</i> Formats that can store multiple entries along with a directory structure, e.g. Zip or Tar.
 * </ul>
 *
 * @author Maxence Bernard
 */
public abstract class Archiver {

    /** Zip archive format (many entries format) */
    public final static int ZIP_FORMAT = 0;
    /** Gzip archive format (single entry format) */
    public final static int GZ_FORMAT = 1;
    /** Bzip2 archive format (single entry format) */
    public final static int BZ2_FORMAT = 2;
    /** Tar archive format without any compression (many entries format) */
    public final static int TAR_FORMAT = 3;
    /** Tar archive compressed with Gzip format (many entries format) */
    public final static int TAR_GZ_FORMAT = 4;
    /** Tar archive compressed with Bzip2 format (many entries format) */
    public final static int TAR_BZ2_FORMAT = 5;

    /** Boolean array describing for each format if it can store more than one entry */
    private final static boolean SUPPORTS_MANY_ENTRIES[] = {
        true,
        false,
        false,
        true,
        true,
        true
    };
	
    /** Array of single entry formats: many entries formats are considered to be single entry formats as well */
    private final static int SINGLE_ENTRY_FORMATS[] = {
        ZIP_FORMAT,
        GZ_FORMAT,
        BZ2_FORMAT,
        TAR_FORMAT,
        TAR_GZ_FORMAT,
        TAR_BZ2_FORMAT
    };

    /** Array of many entries formats */
    private final static int MANY_ENTRIES_FORMATS[] = {
        ZIP_FORMAT,
        TAR_FORMAT,
        TAR_GZ_FORMAT,
        TAR_BZ2_FORMAT
    };
	
    /** Array of format names */
    private final static String FORMAT_NAMES[] = {
        "Zip",
        "Gzip",
        "Bzip2",
        "Tar",
        "Tar/Gzip",
        "Tar/Bzip2"
    };

    /** Array of format extensions */
    private final static String FORMAT_EXTENSIONS[] = {
        "zip",
        "gz",
        "bz2",
        "tar",
        "tar.gz",
        "tar.bz2"
    };
	

    /** The underlying stream this archiver is writing to */
    protected OutputStream out;
    /** Archive format of this Archiver */
    protected int format;
    /** Archive format's name of this Archiver */
    protected String formatName;
	
	
    /**
     * Creates a new Archiver.
     *
     * @param out the OutputStream this Archiver will write to
     */
    Archiver(OutputStream out) {
        this.out = out;
    }

    /**
     * Returns the <code>OutputStream</code> this Archiver is writing to.
     *
     * @return the OutputStream this Archiver is writing to
     */
    public OutputStream getOutputStream() {
        return out;
    }

    
    /**
     * Returns the archiver format used by this Archiver. See format constants.
     */
    public int getFormat() {
        return this.format;
    }
	
    /**
     * Sets the archiver format used by this Archiver, for internal use only.
     */
    private void setFormat(int format) {
        this.format = format;
    }
	
	
    /**
     * Returns the name of the archive format used by this Archiver.
     */
    public String getFormatName() {
        return FORMAT_NAMES[this.format];
    }

	
    /**
     * Returns true if the format used by this Archiver can store more than one entry.
     */
    public boolean supportsManyFiles() {
        return formatSupportsManyFiles(this.format);
    }


    /**
     * Returns true if the format used by this Archiver can store an optional comment.
     */
    public boolean supportsComment() {
        return formatSupportsComment(this.format);
    }


    /**
     * Sets an optional comment in the archive, the {@link #supportsComment()} or
     * {@link #formatSupportsComment(int)} must first be called to make sure
     * the archive format supports comment, otherwise calling this method will have no effect.
     *
     * <p>Implementation note: Archiver implementations must override this method to handle comments
     *
     * @param comment the comment to be stored in the archive
     */
    public void setComment(String comment) {
        // No-op
    }


    /**
     * Normalizes the entry path, that is :
     * <ul>
     * <li>replace any \ character occurrence by / as this usually is the default separator for archive files
     * <li>if the entry is a directory, add a trailing slash to the path if it doesn't have one already 
     * </ul>
     */
    protected String normalizePath(String entryPath, boolean isDirectory) {
        // Replace any \ character by /
        entryPath = entryPath.replace('\\', '/');
		
        // If entry is a directory, make sure the path contains a trailing / 
        if(isDirectory && !entryPath.endsWith("/"))
            entryPath += "/";
		
        return entryPath;
    }
	
	
    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns an Archiver for the specified format and that uses the given {@link AbstractFile} to write entries to.
     * <code>null</code> is returned if the specified format is not valid.
     *
     * <p>This method will first attempt to get a {@link RandomAccessOutputStream} if the given file is able to supply
     * one, and if not, fall back to a regular <code>OutputStream</code>. Note that if the file exists, its contents
     * will be overwritten. Write bufferring is used under the hood to improve performance.</p>
     *
     *
     * @param file the AbstractFile which the returned Archiver will write entries to
     * @param format an archive format
     * @return an Archiver for the specified format and that uses the given {@link AbstractFile} to write entries to ;
     * null if the specified format is not valid.
     * @throws IOException if the file cannot be opened for write, or if an error occurred while intializing the archiver
     */
    public static Archiver getArchiver(AbstractFile file, int format) throws IOException {
        OutputStream out = null;

        if(file.hasRandomAccessOutputStream()) {
            try {
                out = new BufferedRandomOutputStream(file.getRandomAccessOutputStream());
            }
            catch(IOException e) {
                // Fall back to a regular OutputStream
            }
        }

        if(out==null)
            out = new BufferedOutputStream(file.getOutputStream(false));

        return getArchiver(out, format);
    }


    /**
     * Returns an Archiver for the specified format and that uses the given <code>OutputStream</code> to write entries to.
     * <code>null</code> is returned if the specified format is not valid. Whenever possible, a
     * {@link RandomAccessOutputStream} should be supplied as some formats take advantage of having a random write access.
     *
     * @param out the OutputStream which the returned Archiver will write entries to
     * @param format an archive format
     * @return an Archiver for the specified format and that uses the given {@link AbstractFile} to write entries to ;
     * null if the specified format is not valid.
     * @throws IOException if an error occurred while intializing the archiver
     */
    public static Archiver getArchiver(OutputStream out, int format) throws IOException {
        Archiver archiver;

        switch(format) {
            case ZIP_FORMAT:
                archiver = new ZipArchiver(out);
                break;
            case GZ_FORMAT:
                archiver = new SingleFileArchiver(new GZIPOutputStream(out));
                break;
            case BZ2_FORMAT:
                archiver = new SingleFileArchiver(new CBZip2OutputStream(out));
                break;
            case TAR_FORMAT:
                archiver = new TarArchiver(out);
                break;
            case TAR_GZ_FORMAT:
                archiver = new TarArchiver(new GZIPOutputStream(out));
                break;
            case TAR_BZ2_FORMAT:
                archiver = new TarArchiver(new CBZip2OutputStream(out));
                break;

            default:
                return null;
        }
		
        archiver.setFormat(format);

        return archiver;
    }


    /**
     * Returns an array of available archive formats, single entry formats or many entries formats
     * depending on the value of the specified boolean parameter. 
     *
     * @param manyEntries if true, a list of many entries formats (a subset of single entry formats) will be returned
     */
    public static int[] getFormats(boolean manyEntries) {
        return manyEntries? MANY_ENTRIES_FORMATS : SINGLE_ENTRY_FORMATS;
    }

	
    /**
     * Returns the name of the given archive format. The returned name can be used for display in a GUI.
     *
     * @param format an archive format
     */
    public static String getFormatName(int format) {
        return FORMAT_NAMES[format];
    }

	
    /**
     * Returns the default archive format extension. Note: some formats such as Tar/Gzip have several common
     * extensions (e.g. tar.gz or tgz), the most common one will be returned.
     *
     * @param format an archive format	 
     */
    public static String getFormatExtension(int format) {
        return FORMAT_EXTENSIONS[format];
    }
	
	
    /**
     * Returns true if the specified archive format supports storage of more than one entry.
     *
     * @param format an archive format
     */
    public static boolean formatSupportsManyFiles(int format) {
        return SUPPORTS_MANY_ENTRIES[format];
    }
	
	
    /**
     * Returns true if the specified archive format can store an optional comment.
     *
     * @param format an archive format	 
     */
    public static boolean formatSupportsComment(int format) {
        return format==ZIP_FORMAT;
    }
	
	
    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Creates a new entry in the archive with the given path. The specified file is used to determine
     * whether the entry is a directory or a regular file, and to set the entry's length and date.
     * 
     * <p>If the entry is a regular file (not a directory), an OutputStream which can be used to write the contents
     * of the entry will be returned, <code>null</code> otherwise. The OutputStream <b>must not</b> be closed once
     * it has been used (Archiver takes care of this), only the {@link #close() close} method has to be called when
     * all entries have been created.
     *
     * <p>If this Archiver uses a single entry format, the specified path and file won't be used at all.
     * Also in this case, this method must be invoked only once (single entry), it will throw an IOException
     * if invoked more than once.
     *
     * @param entryPath the path to be used to create the entry in the archive.
     *	This parameter is simply ignored if the archive is a single entry format.
     * @param file AbstractFile instance used to determine if the entry is a directory, and to set the entry's date.
     *	This parameter is simply ignored if the archive is a single entry format.
     *
     * @exception IOException if this Archiver failed to write the entry, or in the case of a single entry archiver, if
     * this method was called more than once.
     */
    public abstract OutputStream createEntry(String entryPath, AbstractFile file) throws IOException;


    /**
     * Closes the underlying OuputStream and ressources used by this Archiver to write the archive. This method
     * must be called when all entries have been added to the archive.
     */
    public abstract void close() throws IOException;
}

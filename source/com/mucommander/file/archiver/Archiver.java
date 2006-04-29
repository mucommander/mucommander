
package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import org.apache.tools.bzip2.CBZip2OutputStream;
import java.util.zip.GZIPOutputStream;

import java.io.OutputStream;
import java.io.IOException;


/**
 * Archiver is an abstract class that represents a generic file archiver and abstracts the underlying
 * compression method and specifics of the format.
 *
 * <p>Subclasses implement specific archive formats (Zip, Tar...) but cannot be instanciated directly.
 * Instead, the {@link #getArchiver(OutputStream, int) getArchiver} method can be used to retrieve an Archiver
 * instance for a specified archive format. A list of available archive formats can be dynamically retrieved
 * using {@link #getFormats(boolean) getFormats}.
 *
 * <p>Archive formats fall into 2 categories:
 * <ul>
 * <li><i>Single file formats:</i> Formats that can only store one file without any directory structure, e.g. Gzip or Bzip2.
 * <li><i>Many files formats:</i> Formats that can store multiple files along with a directory structure, e.g. Zip or Tar.
 * </ul>
 *
 * @author Maxence Bernard
 */
public abstract class Archiver {

	/** Zip archive format (many files format) */
	public final static int ZIP_FORMAT = 0;
	/** Gzip archive format (single file format) */
	public final static int GZ_FORMAT = 1;
	/** Bzip2 archive format (single file format) */
	public final static int BZ2_FORMAT = 2;
	/** Tar archive format without any compression (many files format) */
	public final static int TAR_FORMAT = 3;
	/** Tar archive compressed with Gzip format (many files format) */
	public final static int TAR_GZ_FORMAT = 4;
	/** Tar archive compressed with Bzip2 format (many files format) */
	public final static int TAR_BZ2_FORMAT = 5;

	/** Boolean array describing for each format if it can store more than one file */
	private final static boolean SUPPORTS_MANY_FILES[] = {
		true,
		false,
		false,
		true,
		true,
		true
	};
	
	/** Array of single file formats: many files formats are considered to be single file formats as well */
	private final static int SINGLE_FILE_FORMATS[] = {
		ZIP_FORMAT,
		GZ_FORMAT,
		BZ2_FORMAT,
		TAR_FORMAT,
		TAR_GZ_FORMAT,
		TAR_BZ2_FORMAT
	};

	/** Array of many files formats */
	private final static int MANY_FILES_FORMATS[] = {
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
	

	/** Archive format of this Archiver */
	protected int format;
	/** Archive format's name of this Archiver */
	protected String formatName;
	
	
	/**
	 * Creates a new Archiver.
	 */
	Archiver() {	
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
	 * Returns true if the format used by this Archiver can store more than one file.
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
	 * Sets an optional comment in the archive, the {@link supportsComment() supportsComment} or
	 * {@link formatSupportsComment(int) formatSupportsComment} must first be called to make sure
	 * the archive format supports comment, otherwise calling this method will have no effect.
	 *
	 * <p>Implementation note: Archiver implementations must override this method
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
	 * Returns an Archiver for the given format and using the given OutputStream, or null
	 * if the specified format is not valid. 
	 *
	 * @param outputStream the OutputStream the returned Archiver will use to write entries
	 * @param format an archive format
	 */
	public static Archiver getArchiver(OutputStream outputStream, int format) throws IOException {
		Archiver archiver;

		switch(format) {
			case ZIP_FORMAT:
				archiver = new ZipArchiver(outputStream);
				break;
			case GZ_FORMAT:
				archiver = new SingleFileArchiver(new GZIPOutputStream(outputStream));
				break;
			case BZ2_FORMAT:
				archiver = new SingleFileArchiver(new CBZip2OutputStream(outputStream));
				break;
			case TAR_FORMAT:
				archiver = new TarArchiver(outputStream);
				break;
			case TAR_GZ_FORMAT:
				archiver = new TarArchiver(new GZIPOutputStream(outputStream));
				break;
			case TAR_BZ2_FORMAT:
				archiver = new TarArchiver(new CBZip2OutputStream(outputStream));
				break;
			
			default:
				return null;
		}
		
		archiver.setFormat(format);

		return archiver;
	}


	/**
	 * Returns an array of available archive formats, single file formats or many files formats
	 * depending on the value of the specified boolean parameter. 
	 *
	 * @param manyFiles if true, a list many files formats (a subset of single file formats) will be returned
	 * @param format an archive format
	 */
	public static int[] getFormats(boolean manyFiles) {
		return manyFiles?MANY_FILES_FORMATS:SINGLE_FILE_FORMATS;
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
	 * Returns true if the specified archive format supports storage of more than one file.
	 *
	 * @param format an archive format
	 */
	public static boolean formatSupportsManyFiles(int format) {
		return SUPPORTS_MANY_FILES[format];
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
	 * Creates a new entry in the archive with the given path. The specified file will be used to determine
	 * whether the entry is a directory or a regular file, and set the entry's length and date.
	 * 
	 * <p>If the entry is a regular file (not a directory), an OutputStream which can be used to write the contents
	 * of the entry will be returned, <code>null</code> otherwise. The OutputStream <b>must not</b> be closed once
	 * it has been used (Archiver takes care of this), only the {@link #close() close} method has to be called when
	 * all entries have been created.
	 *
	 * <p>If this Archiver uses a single file format, the specified path and file won't be used at all. 
	 * Also in this case, this method must be invoked only once (single file), it will throw an IOException
	 * if invoked more than once.
	 *
	 * @param entryPath the path to be used to create the entry in the archive.
	 *	This parameter is simply ignored if the archive is a single file format.
	 * @param file AbstractFile instance used to determine if the entry is a directory, and to set the entry's date.
	 *	This parameter is simply ignored if the archive is a single file format.
	 *
	 * @throw IOException if this Archiver failed to write the entry, or in the case of a single file archiver, if
	 * this method was called more than once.
	 */
	public abstract OutputStream createEntry(String entryPath, AbstractFile file) throws IOException;


	/**
	 * Closes the underlying OuputStream and ressources used by this Archiver to write the archive. This method
	 * must be called when all entries have been added to the archive.
	 */
	public abstract void close() throws IOException;
}
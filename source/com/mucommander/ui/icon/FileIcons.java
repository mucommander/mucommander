
package com.mucommander.ui.icon;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractArchiveFile;

import java.util.Hashtable;
import javax.swing.ImageIcon;


/**
 * Class that is responsible for providing icons for given files. Icons are chosen based on the files kind (archive, folder...) and extension.
 * 
 * <p>For memory efficiency reasons, icon instances are created only the first the icon is requested, and then
 * shared across the application.<p>
 *
 * @author Maxence Bernard
 */
public class FileIcons {

	/** Hashtable that associates file extensions with icon names */
	private static Hashtable iconExtensions;

	/** Icon for directories */
	private final static String FOLDER_ICON_NAME = "folder.png";
	/** Default icon for files without a known extension */
	private final static String FILE_ICON_NAME = "file.png";
	/** Icon for supported archives (browsable) */
	private final static String ARCHIVE_ICON_NAME = "archive_supported.png";
	/** Icon for parent folder (..) */
	private final static String PARENT_FOLDER_ICON_NAME = "parent.png";
	

	/** File icon <-> extensions association. For information about file extensions, see:
	 * <ul>
	 *  <li><a href="http://en.wikipedia.org/wiki/File_format">http://en.wikipedia.org/wiki/File_format</a>
	 *  <li><a href="http://filext.com/">http://filext.com/</a>
	 *	<li><a href="http://whatis.techtarget.com/fileFormatP/">http://whatis.techtarget.com/fileFormatP/</a>
	 *  <li><a href="http://www.fileinfo.net">http://www.fileinfo.net</a>
	 * </ul>
	 */	
	private final static String ICON_EXTENSIONS[][] = {
		{"archive_unsupported.png", "7z", "ace", "arj", "bin", "bz", "bz2", "cab", "dmg", "hqx", "ipk", "lha", "lzh", "lzx", "msi", "mpkg", "pak", "pkg", "pq6", "rar", "rk", "rz", "sea", "sit", "sitx", "sqx", "z", "zoo"},	// Unsupported archive formats (no native support), see http://en.wikipedia.org/wiki/Archive_formats
		{"audio.png", "aac", "aif", "aiff", "aifc", "amr", "ape", "au", "cda", "mp3", "mpa", "mp2", "mpc", "m3u", "m4a", "m4b", "m4p", "nap", "ogg", "pls", "ra", "ram", "wav", "wave", "flac", "wma", "mid", "midi", "smf", "mod", "mtm", "xm", "s3m"},	// Audio formats, see http://en.wikipedia.org/wiki/Audio_file_format
		{"cd_image.png", "iso", "nrg"},	// CD/DVD image
		{"certificate.png", "cer", "crt", "key"},	// Certificate file
		{"configuration.png", "cnf", "conf", "config", "inf", "ini", "pif", "prefs", "prf"},	// Configuration file
		{"database.png", "myi", "myd", "frm", "sql", "sqc", "sqr"},	// Database file
		{"executable.png", "bat", "com", "exe"},	// Executable files
		{"feed.png", "rdf", "rss"},	// RSS/RDF feed
		{"font.png", "fnt", "fon", "otf"},	// Non-TrueType font formats
		{"font_truetype.png", "ttc", "ttf"},	// TrueType font formats
		{"image_bitmap.png", "exif", "ico", "gif", "j2k", "jpg", "jpeg", "jpg2", "jp2", "bmp", "ico", "iff", "mng", "pcd", "pic", "pict", "png", "psd", "psp", "pbm", "pgm", "ppm", "raw", "tga", "tiff", "tif", "wbmp", "xbm", "xcf", "xpm"},	// Bitmap image formats, see http://en.wikipedia.org/wiki/Graphics_file_format and http://en.wikipedia.org/wiki/Image_file_formats
		{"image_vector.png", "ai", "cgm", "dpx", "dxf", "eps", "emf", "ps", "svg", "svgz", "wmf", "xar"},	// Vector image formats, http://en.wikipedia.org/wiki/Graphics_file_format
		{"linux.png", "deb", "rpm"},	// Linux packages
		{"macromedia_actionscript.png", "as"},	// Macromedia Actionscript
		{"macromedia_flash.png", "swf", "swd", "swa", "swc", "fla", "flv", "flp", "jsfl"},	// Macromedia Flash
		{"macromedia_freehand.png", "fh", "fhd"},	// Macromedia Freehand
		{"ms_excel.png", "xls", "xla", "xlb", "xlc", "xld", "xlk", "xll", "xlm", "xlr", "xlt", "xlv", "xlw", "xlshtml", "xlsmhtml", "xlsx", "xlthtml"},	// Microsoft Excel
		{"ms_word.png", "doc", "wbk", "wiz", "wpg", "wpk", "wpm", "wpt", "wrs", "wwl"},	// Microsoft Word
		{"ms_powerpoint.png", "pcb", "pot", "ppa", "ppi", "pps", "ppt", "pwz"},	// Microsoft Office (Powerpoint)
		{"ms_visualstudio.png",	"atp", "dbp", "hxc", "ncb", "pch", "pdb", "sln", "suo", "srf", "vaf", "vam", "vbg", "vbp", "vbproj", "vcproj", "vdp", "vdproj", "vip", "vmx", "vsdir", "vsmacros",	"vsmproj", "vup"},	// Microsoft Visual Studio
		{"ms_windows_shortcut.png", "lnk"},	// MS Windows .lnk shortcut files
		{"pdf.png", "pdf"},		// Adobe Acrobat / PDF
		{"source.png", "asm", "asp", "bas", "bcp", "cbl", "cob", "f", "fpp", "inc", "js", "lsp", "m4", "pas", "pl", "py", "src", "vb", "vbe", "vbs", "x"},	// Languages for which there is no special icon (generic source icon)
		{"source.png", "awk", "csh", "esh", "sh", "ksh", "ws", "wsf"},	// Shell scripts
		{"source_c.png", "c", "cc"},	// C source
		{"source_c_header.png", "h", "hh", "hhh"},	// C header
		{"source_cplusplus.png", "cpp", "c++"},	// C++ source
		{"source_csharp.png", "c#=", "cs"},	// C# source
		{"source_java.png", "java", "jsp"},	// Java source
		{"source_php.png", "php", "php3", "php4", "php5", "phtm", "phtml"},	// PHP source
		{"source_ruby.png", "rb", "rbx", "rhtml"},	// Ruby source
		{"source_web.png", "html", "htm", "xhtml", "wml", "wmlc", "wmls", "wmlsc", "hdml", "xhdml", "chtml", "vrml", "torrent", "url", "css"},	// Web formats
		{"source_xml.png", "xml", "dtd", "xfd", "xfdl", "xmap", "xmi", "xsc", "xsd", "xsl", "xslt", "xtd", "xul", "rss", "jnlp", "plist"},	// XML-based formats
		{"text.png", "1st", "ans", "asc", "ascii", "diz", "err", "faq", "latex", "log", "man", "msg", "nfo", "readme", "rtf", "sig", "tex", "text", "txt"},	// Text formats
		{"vcard.png", "vcf"},	// vCard
		{"video.png", "3g2", "3gp", "3gp2", "3gpp", "asf", "asx", "avi", "dir", "dv", "dxr", "m1v", "m4e", "m4u", "moov", "mov", "movie", "mp4", "mpe", "mpeg", "mpg", "mpv2", "qt", "rm", "rmvb", "rts", "vob", "wmv"}		// Video formats
	};


	/**
	 * Initializes extensions hashtables and preloads icons we're sure to use.
	 */
	static {
		// Maps known file extensions to icon names
		iconExtensions = new Hashtable();
		int nbIcons = ICON_EXTENSIONS.length;
		for(int i=0; i<nbIcons; i++) {
			int nbExtensions = ICON_EXTENSIONS[i].length;
			String iconName = ICON_EXTENSIONS[i][0];
			for(int j=1; j<nbExtensions; j++)
				iconExtensions.put(ICON_EXTENSIONS[i][j], iconName);
		}

		// Preloads icons so they're in IconManager's cache for when we need them
		IconManager.getFileIcon(FOLDER_ICON_NAME);
		IconManager.getFileIcon(FILE_ICON_NAME);
		IconManager.getFileIcon(ARCHIVE_ICON_NAME);
	}

	
	/**
	 * Dummy method which does nothing but trigger static block execution.
	 * Calling this method early enough at launch time makes initialization predictable.
	 */
	public static void init() {
	}

	
	/**
	 * Returns an ImageIcon instance for the given file. The icon is chosen based on the file kind (archive, folder...) and extension.
	 *
	 * @param file the AbstractFile instance for which an icon will be returned
	 */
	public static ImageIcon getFileIcon(AbstractFile file) {
		// Directory
		if(file.isDirectory()) {
			return IconManager.getFileIcon(FOLDER_ICON_NAME);
		}
		// Supported archive
		else if(file.isBrowsable()) {
			return IconManager.getFileIcon(ARCHIVE_ICON_NAME);
		}
		// Regular file
		else {
			// Determine if the extension has an associated icon
			String fileExtension = file.getExtension();
			if(fileExtension==null)	// File has no extension, return default file icon
				return IconManager.getFileIcon(FILE_ICON_NAME);

			// Compare extension against lower-cased extensions
			String iconName = (String)iconExtensions.get(fileExtension.toLowerCase());
			if(iconName==null)	// No icon associated to extension, return default file icon
				return IconManager.getFileIcon(FILE_ICON_NAME);
			
			// Retrieves the cached (or freshly loaded if not in cache already) ImageIcon instance corresponding to the icon's name
			ImageIcon icon = IconManager.getFileIcon(iconName);
			// Returned IconImage should never be null, but if it is (icon file missing), return default file icon
			if(icon==null)
				return IconManager.getFileIcon(FILE_ICON_NAME);
				
			return icon;
		}
	}

	
	/**
	 * Returns the icon for the parent folder (..).
	 */
	public static ImageIcon getParentFolderIcon() {
		return IconManager.getFileIcon(PARENT_FOLDER_ICON_NAME);
	}
	
	
	/**
	 * Tries to load all file icons and reports on their status : OK/FAILED.
	 */
	public static void main(String args[]) {
		for(int i=0; i<ICON_EXTENSIONS.length; i++) {
			String iconName = ICON_EXTENSIONS[i][0];
			System.out.print("Loading icon "+iconName+": ");
			ImageIcon icon = IconManager.getFileIcon(ICON_EXTENSIONS[i][0]);
			System.out.println(icon==null?"FAILED!":"OK");
		}
	}
}
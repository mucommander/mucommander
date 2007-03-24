
package com.mucommander.ui.icon;

import com.mucommander.PlatformManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.file.impl.local.LocalFile;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;


/**
 * FileIcons provides methods to retrieve file icons:
 * <ul>
 *  <li>{@link #getSystemFileIcon(AbstractFile)} returns a system icon (provided by the OS/desktop manager) for a
 * given AbstractFile. 
 *  <li>{@link #getCustomFileIcon(AbstractFile)} returns a custom icon (from the muCommander icon set)
 * for a given AbstractFile. Those icons are chosen based on the file's kind (archive, folder...) and extension.
 *  <li}{@link #getFileIcon(AbstractFile)} returns either a system icon or a custom icon, depending on the current
 * system icons policy.
 * </ul>
 *
 * Those methods can be used with any kind of {@link AbstractFile}: local, remote, archives entries...
 * 
 * <p>Some caching is used to share icon instances as much as possible and thus minimize I/O operations.<p>
 *
 * @author Maxence Bernard
 */
public class FileIcons {

    /** Hashtable that associates file extensions with icon names */
    private static Hashtable iconExtensions;

    /** Current icon scale factor */
    private static float scaleFactor = ConfigurationManager.getVariableFloat(ConfigurationVariables.TABLE_ICON_SCALE,
                                                                             ConfigurationVariables.DEFAULT_TABLE_ICON_SCALE);

    /** Icon for directories */
    public final static String FOLDER_ICON_NAME = "folder.png";
    /** Default icon for files without a known extension */
    public final static String FILE_ICON_NAME = "file.png";
    /** Icon for supported archives (browsable) */
    private final static String ARCHIVE_ICON_NAME = "archive_supported.png";
    /** Icon for parent folder (..) */
    private final static String PARENT_FOLDER_ICON_NAME = "parent.png";	
    /** Icon for Mac OS X's applications */
    private final static String MAC_OS_X_APP_ICON_NAME = "executable_osx.png";
	

    /** File icon <-> extensions association. For information about file extensions, see:
     * <ul>
     *  <li><a href="http://en.wikipedia.org/wiki/File_format">http://en.wikipedia.org/wiki/File_format</a>
     *  <li><a href="http://filext.com/">http://filext.com/</a>
     *	<li><a href="http://whatis.techtarget.com/fileFormatP/">http://whatis.techtarget.com/fileFormatP/</a>
     *  <li><a href="http://www.fileinfo.net">http://www.fileinfo.net</a>
     * </ul>
     */	
    private final static String ICON_EXTENSIONS[][] = {
        {"archive_unsupported.png", "7z", "ace", "arj", "bin", "bz", "cab", "dmg", "hqx", "ipk", "lha", "lzh", "lzx", "msi", "mpkg", "pak", "pkg", "pq6", "rar", "rk", "rz", "sea", "sit", "sitx", "sqx", "z", "zoo"},	// Unsupported archive formats (no native support), see http://en.wikipedia.org/wiki/Archive_formats
        {"audio.png", "aac", "aif", "aiff", "aifc", "amr", "ape", "au", "cda", "mp3", "mpa", "mp2", "mpc", "m3u", "m4a", "m4b", "m4p", "nap", "ogg", "pls", "ra", "ram", "wav", "wave", "flac", "wma", "mid", "midi", "smf", "mod", "mtm", "xm", "s3m"},	// Audio formats, see http://en.wikipedia.org/wiki/Audio_file_format
        {"cd_image.png", "iso", "nrg"},	// CD/DVD image
        {"certificate.png", "cer", "crt", "key"},	// Certificate file
        {"configuration.png", "cnf", "conf", "config", "inf", "ini", "pif", "prefs", "prf"},	// Configuration file
        {"database.png", "myi", "myd", "frm", "sql", "sqc", "sqr"},	// Database file
        {"executable_windows.png", "bat", "com", "exe"},	// Windows executables
        {"executable_osx.png", "app"},	// Mac OS X executables
        {"feed.png", "rdf", "rss"},	// RSS/RDF feed
        {"font.png", "fnt", "fon", "otf"},	// Non-TrueType font
        {"font_truetype.png", "ttc", "ttf"},	// TrueType font
        {"image_bitmap.png", "exif", "ico", "gif", "j2k", "jpg", "jpeg", "jpg2", "jp2", "bmp", "ico", "iff", "mng", "pcd", "pic", "pict", "png", "psd", "psp", "pbm", "pgm", "ppm", "raw", "tga", "tiff", "tif", "wbmp", "xbm", "xcf", "xpm"},	// Bitmap image formats, see http://en.wikipedia.org/wiki/Graphics_file_format and http://en.wikipedia.org/wiki/Image_file_formats
        {"image_vector.png", "ai", "cgm", "dpx", "dxf", "eps", "emf", "ps", "svg", "svgz", "wmf", "xar"},	// Vector image formats, http://en.wikipedia.org/wiki/Graphics_file_format
        {"library.png", "dylib", "la", "o", "so"},	// Libraries
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


    /** Width of all file icons */
    private final static int STANDARD_WIDTH = 16;

    /** Height of all file icons */
    private final static int STANDARD_HEIGHT = 16;


    /** Never use system file icons */
    public final static String USE_SYSTEM_ICONS_NEVER = "never";
    /** Use system file icons only for applications */
    public final static String USE_SYSTEM_ICONS_APPLICATIONS = "applications";
    /** Always use system file icons */
    public final static String USE_SYSTEM_ICONS_ALWAYS = "always";

    /** Controls if and when system file icons should be used instead of custom icons */
    private static String systemIconsPolicy = ConfigurationManager.getVariable(ConfigurationVariables.USE_SYSTEM_FILE_ICONS, ConfigurationVariables.DEFAULT_USE_SYSTEM_FILE_ICONS);

    // Swing objects used to retrieve system file icons
    private final static JFileChooser FILE_CHOOSER = new JFileChooser();
    private final static FileSystemView FILESYSTEM_VIEW = FileSystemView.getFileSystemView();

    /** Caches system icons for files located on remote locations, or in archives */ 
    private final static LRUCache SYSTEM_ICON_CACHE = LRUCache.createInstance(ConfigurationManager.getVariableInt(ConfigurationVariables.SYSTEM_ICON_CACHE_CAPACITY, ConfigurationVariables.DEFAULT_SYSTEM_ICON_CACHE_CAPACITY));


    /**
     * Initializes extensions hashtables and preloads icons we're sure to use.
     */
    public static void init() {
        // Map known file extensions to icon names
        iconExtensions = new Hashtable();
        int nbIcons = ICON_EXTENSIONS.length;
        for(int i=0; i<nbIcons; i++) {
            int nbExtensions = ICON_EXTENSIONS[i].length;
            String iconName = ICON_EXTENSIONS[i][0];
            for(int j=1; j<nbExtensions; j++)
                iconExtensions.put(ICON_EXTENSIONS[i][j], iconName);
        }

        if(!USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy)) {  // No need to preload icons if system icons are used
            // Preload icons so they're in IconManager's cache for when we need them
            IconManager.getIcon(IconManager.FILE_ICON_SET, FOLDER_ICON_NAME, scaleFactor);
            IconManager.getIcon(IconManager.FILE_ICON_SET, FILE_ICON_NAME, scaleFactor);
            IconManager.getIcon(IconManager.FILE_ICON_SET, ARCHIVE_ICON_NAME, scaleFactor);
        }
    }

	
    /**
     * Returns an icon for the given file. Depending on the current system icons policy, the returned icon is either
     * a system icon, or one from the custom icon set.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     * @see #getSystemIconsPolicy()
     */
    public static Icon getFileIcon(AbstractFile file) {
        if(USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy))
            return getSystemFileIcon(file);

        if(USE_SYSTEM_ICONS_APPLICATIONS.equals(systemIconsPolicy)) {
            String extension = file.getExtension();
            boolean systemIcon;

            if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X && "app".equalsIgnoreCase(extension))
                systemIcon = true;
            else if((PlatformManager.OS_FAMILY==PlatformManager.WINDOWS_9X || PlatformManager.OS_FAMILY==PlatformManager.WINDOWS_NT)
                    && "exe".equalsIgnoreCase(extension))
                systemIcon = true;
            else
                systemIcon = false;

            if(systemIcon)
                return getSystemFileIcon(file);
        }

        return getCustomFileIcon(file);
    }


    /**
     * Returns an icon for the given file using the custom icon set (i.e. not system icons).
     * The icon is chosen based on the file kind (archive, folder...) and extension.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     */
    public static ImageIcon getCustomFileIcon(AbstractFile file) {
        // Retrieve file's extension, null if file has no extension
        String fileExtension = file.getExtension();

        // If file is a directory, return folder icon. One exception is made for Mac OS X's applications
        // which are directories with .app extension and have a dedicated icon
        if(file.isDirectory()) {
            if(fileExtension!=null && fileExtension.equals("app"))
                return IconManager.getIcon(IconManager.FILE_ICON_SET, MAC_OS_X_APP_ICON_NAME, scaleFactor);
            return IconManager.getIcon(IconManager.FILE_ICON_SET, FOLDER_ICON_NAME, scaleFactor);
        }
        // If file is browsable (supported archive or other), return archive icon
        else if(file.isBrowsable()) {
            return IconManager.getIcon(IconManager.FILE_ICON_SET, ARCHIVE_ICON_NAME, scaleFactor);
        }
        // Regular file
        else {
            // Determine if the file's extension has an associated icon
            if(fileExtension==null)	// File has no extension, return default file icon
                return IconManager.getIcon(IconManager.FILE_ICON_SET, FILE_ICON_NAME, scaleFactor);

            // Compare extension against lower-cased extensions
            String iconName = (String)iconExtensions.get(fileExtension.toLowerCase());
            if(iconName==null)	// No icon associated to extension, return default file icon
                return IconManager.getIcon(IconManager.FILE_ICON_SET, FILE_ICON_NAME, scaleFactor);

            // Retrieves the cached (or freshly loaded if not in cache already) ImageIcon instance corresponding to the icon's name
            ImageIcon icon = IconManager.getIcon(IconManager.FILE_ICON_SET, iconName, scaleFactor);
            // Returned IconImage should never be null, but if it is (icon file missing), return default file icon
            if(icon==null)
                return IconManager.getIcon(IconManager.FILE_ICON_SET, FILE_ICON_NAME, scaleFactor);

            return icon;
        }
    }


    /**
     * Returns a system icon (one provided by the underlying OS/desktop manager) for the given file.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     */
    public static Icon getSystemFileIcon(AbstractFile file) {
        java.io.File javaIoFile = null;
        AbstractFile tempFile = null;
        Icon icon = null;

        // The javax.swing.JFileChooser#getIcon(java.io.File) method is used to retrieve system file icons.
        // This method expects a java.io.File which is fine for local files, but some magic has to be used to grab the
        // icon for remote files.

        // Specified file is a LocalFile or a ProxyFile proxying a LocalFile (e.g. an archive file):
        // get the system file icon using the underlying java.io.File instance
        if(file instanceof LocalFile)
            javaIoFile = ((LocalFile)file).getJavaIoFile();
        else if((file instanceof ProxyFile && (((ProxyFile)file).getProxiedFile() instanceof LocalFile)))
            javaIoFile = ((LocalFile)((ProxyFile)file).getProxiedFile()).getJavaIoFile();

        // File is a remote file: create a temporary local file (or directory) with the same extension to grab the icon
        // and then delete the file. This operation is I/O bound and thus expensive, so an LRU is used to cache
        // frequently-accessed file extensions.
        else {
            // Look for an existing icon instance for the file's extension
            String extension = file.getExtension();
            icon = (Icon)SYSTEM_ICON_CACHE.get(extension);

            // No existing icon, let's go ahead with creating a temporary file/directory with the same extension to
            // get the icon, and add it to the cache
            if(icon==null) {
                tempFile = FileFactory.getTemporaryFile(file.getName(), false);

                try {
                    // Create a directory
                    if(file.isDirectory())
                        tempFile.getParent().mkdir(tempFile.getName());
                    // Create a regular file
                    else
                        tempFile.getOutputStream(false).close();
                }
                catch(IOException e) {}

                javaIoFile = tempFile instanceof LocalFile?((LocalFile)tempFile).getJavaIoFile()
                        :((LocalFile)((ProxyFile)tempFile).getProxiedFile()).getJavaIoFile();

                // Get the system file icon
                icon = getSystemFileIcon(javaIoFile);

                // Cache the icon
                SYSTEM_ICON_CACHE.add(extension, icon);
            }
        }

        // Get the system file icon if not done already
        if(icon==null)
            icon = getSystemFileIcon(javaIoFile);

        // Scale the icon if needed
        if(scaleFactor!=1.0f)
            icon = IconManager.getScaledIcon(IconManager.getImageIcon(icon), scaleFactor);

        // If a temporary file was created, delete it
        if(tempFile!=null)
            try { tempFile.delete(); }
            catch(IOException e) {}

        return icon;
    }


    /**
     * Returns a system file icon for the given File, by calling the proper Java API method for the current OS.
     */
    private static Icon getSystemFileIcon(File file) {
        // FileSystemView#getSystemIcon(File) returns bogus icons under Mac OS X
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X)
            FILE_CHOOSER.getIcon(file);

        // JFileChooser#getSystemIcon(File) returns bogus icons under Windows
        return FILESYSTEM_VIEW.getSystemIcon(file);
    }


	
    /**
     * Returns the standard size of a file icon, multiplied by the current scale factor.
     */
    public static Dimension getStandardSize() {
        return new Dimension((int)(STANDARD_WIDTH*scaleFactor), (int)(STANDARD_HEIGHT*scaleFactor));
    }

	
    /**
     * Returns the icon for the parent folder (..).
     */
    public static Icon getParentFolderIcon() {
        return IconManager.getIcon(IconManager.FILE_ICON_SET, PARENT_FOLDER_ICON_NAME, scaleFactor);
    }


    /**
     * Returns the current file icons scale factor.
     */
    public static float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Sets the file icons scale factor.
     */
    public static void setScaleFactor(float factor) {
        scaleFactor = factor;
    }


    /**
     * Returns the current system icons policy, controlling when system file icons should be used instead
     * of custom file icons.
     *
     * <p>See constants fields for possible values.
     */
    public static String getSystemIconsPolicy() {
        return systemIconsPolicy;
    }

    /**
     * Sets the system icons policy, controlling when system file icons should be used instead of custom file icons.
     *
     * <p>See constants fields for possible values.
     */
    public static void setSystemIconsPolicy(String policy) {
        systemIconsPolicy = policy;
    }


    /////////////////
    // Test method //
    /////////////////

    /**
     * Tries to load all file icons and reports on their status : OK/FAILED.
     */
    public static void main(String args[]) {
        for(int i=0; i<ICON_EXTENSIONS.length; i++) {
            String iconName = ICON_EXTENSIONS[i][0];
            System.out.print("Loading icon "+iconName+": ");
            ImageIcon icon = IconManager.getIcon(IconManager.FILE_ICON_SET, ICON_EXTENSIONS[i][0], scaleFactor);
            System.out.println(icon==null?"FAILED!":"OK");
        }
    }
}

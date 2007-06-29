/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.ui.icon;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
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
 * system icons policy. The default policy is {@link #DEFAULT_SYSTEM_ICONS_POLICY} and can be changed using
 * {@link #setSystemIconsPolicy(String)}.
 * </ul>
 *
 * <p>Those methods can be used with any kind of {@link AbstractFile}: local, remote, archives entries...
 * Some caching is used to share icon instances as much as possible and thus minimize I/O operations.</p>
 *
 * <p>Note that not all platforms have proper support for system file icons. The {@link #hasProperSystemIcons()} method
 * can be used to determine that.
 *
 * @author Maxence Bernard
 */
public class FileIcons {

    /** Icon for directories */
    public final static String FOLDER_ICON_NAME = "folder.png";
    /** Default icon for files without a known extension */
    public final static String FILE_ICON_NAME = "file.png";
    /** Icon for supported archives (browsable) */
    public final static String ARCHIVE_ICON_NAME = "archive_supported.png";
    /** Icon for parent folder (..) */
    public final static String PARENT_FOLDER_ICON_NAME = "parent.png";
    /** Icon for Mac OS X's applications */
    public final static String MAC_OS_X_APP_ICON_NAME = "executable_osx.png";
	

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

    /** Default policy for system icons */
    public final static String DEFAULT_SYSTEM_ICONS_POLICY = USE_SYSTEM_ICONS_APPLICATIONS;

    /** Controls if and when system file icons should be used instead of custom icons */
    private static String systemIconsPolicy = DEFAULT_SYSTEM_ICONS_POLICY;

    /** Default icon scale factor (no rescaling) */
    public final static float DEFAULT_SCALE_FACTOR = 1.0f;

    /** Current icon scale factor */
    private static float scaleFactor = DEFAULT_SCALE_FACTOR;


    // Field used for custom file icons

    /** Has support for custom icons been initialized ? */
    private static boolean customIconsInitialized;

    /** Hashtable that associates file extensions with icon names */
    private static Hashtable customIconExtensions;


    // Fields used for system file icons

    /** Has support for system icons been initialized ? */
    private static boolean systemIconsInitialized;

    // Swing objects used to retrieve system file icons
    private static JFileChooser fileChooser;
    private static FileSystemView fileSystemView;

    /** Caches system icons for directories located on remote locations, or in archives */
    private static LRUCache systemIconDirCache;

    /** Caches system icons for files located on remote locations, or in archives */
    private static LRUCache systemIconFileCache;


    /**
     * Initializes fields used for custom icons.
     */
    private static void initCustomIcons() {
        // Map known file extensions to icon names
        customIconExtensions = new Hashtable();
        int nbIcons = ICON_EXTENSIONS.length;
        for(int i=0; i<nbIcons; i++) {
            int nbExtensions = ICON_EXTENSIONS[i].length;
            String iconName = ICON_EXTENSIONS[i][0];
            for(int j=1; j<nbExtensions; j++)
                 customIconExtensions.put(ICON_EXTENSIONS[i][j], iconName);
        }

        if(Debug.ON) Debug.trace("done");
        customIconsInitialized = true;
    }

    /**
     * Initializes fields used for system icons.
     */
    private static void initSystemIcons() {
        // Initialize the Swing object used to retrieve system file icons
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X)
            fileChooser = new JFileChooser();
        else
            fileSystemView = FileSystemView.getFileSystemView();

        // Initialize system icon caches to limit the number of calls made to the getIcon method of the Swing object:
        // - used for directories located on remote locations, or in archives
        systemIconDirCache = LRUCache.createInstance(ConfigurationManager.getVariableInt(ConfigurationVariables.SYSTEM_ICON_CACHE_CAPACITY, ConfigurationVariables.DEFAULT_SYSTEM_ICON_CACHE_CAPACITY));
        // - used for files located on remote locations, or in archives
        systemIconFileCache = LRUCache.createInstance(ConfigurationManager.getVariableInt(ConfigurationVariables.SYSTEM_ICON_CACHE_CAPACITY, ConfigurationVariables.DEFAULT_SYSTEM_ICON_CACHE_CAPACITY));

        if(Debug.ON) Debug.trace("done");
        systemIconsInitialized = true;
    }

    /**
     * Returns an icon for the given file. Depending on the current system icons policy, the returned icon is either
     * a system icon, or one from the custom icon set.
     * Returns <code>mull</code> if the icon couldn't be retrieved, either because the file doesn't exists or for any
     * other reason.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     * @see #getSystemIconsPolicy()
     */
    public static Icon getFileIcon(AbstractFile file) {
        if(USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy))
            return getSystemFileIcon(file);

        if(USE_SYSTEM_ICONS_APPLICATIONS.equals(systemIconsPolicy)) {
            String extension = file.getExtension();

            if(extension!=null) {
                boolean systemIcon;

                if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X && "app".equalsIgnoreCase(extension))
                    systemIcon = true;
                else if(PlatformManager.isWindowsFamily() && "exe".equalsIgnoreCase(extension))
                    systemIcon = true;
                else
                    systemIcon = false;

                if(systemIcon)
                    return getSystemFileIcon(file);
            }
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
        // Initialize custom icons support, if not done already
        if(!customIconsInitialized)
            initCustomIcons();

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
            String iconName = (String) customIconExtensions.get(fileExtension.toLowerCase());
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
     * Returns <code>mull</code> if the icon couldn't be retrieved, either because the file doesn't exists or for any
     * other reason.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     */
    public static Icon getSystemFileIcon(AbstractFile file) {
        // Initialize system icons support, if not done already
        if(!systemIconsInitialized)
            initSystemIcons();

        AbstractFile tempFile = null;
        Icon icon;

        // The javax.swing.JFileChooser#getIcon(java.io.File) method is used to retrieve system file icons.
        // This method expects a java.io.File which is fine for local files, but some magic has to be used to grab the
        // icon for remote files.

        // Specified file is a LocalFile or a ProxyFile proxying a LocalFile (e.g. an archive file):
        // get the system file icon using the underlying java.io.File instance
        file = file.getTopAncestor();
        if(file instanceof LocalFile) {
            icon = getSystemFileIcon((File)file.getUnderlyingFileObject());
        }
        // File is a remote file: create a temporary local file (or directory) with the same extension to grab the icon
        // and then delete the file. This operation is I/O bound and thus expensive, so an LRU is used to cache
        // frequently-accessed file extensions.
        else {
            // Look for an existing icon instance for the file's extension
            String extension = file.getExtension();
            boolean isDirectory = file.isDirectory();

            icon = (Icon)(isDirectory? systemIconDirCache : systemIconFileCache).get(extension);

//            if(Debug.ON) Debug.trace("Cache "+(icon==null?"miss":"hit")+" for extension="+extension+" isDirectory="+isDirectory);

            // No existing icon, let's go ahead with creating a temporary file/directory with the same extension to
            // get the icon, and add it to the cache
            if(icon==null) {
                tempFile = FileFactory.getTemporaryFile(file.getName(), false);

                try {
                    // Create a directory
                    if(isDirectory)
                        tempFile.getParent().mkdir(tempFile.getName());
                    // Create a regular file
                    else
                        tempFile.getOutputStream(false).close();
                }
                catch(IOException e) {}

                // Get the system file icon
                icon = getSystemFileIcon((File)tempFile.getTopAncestor().getUnderlyingFileObject());

                // Cache the icon
                (isDirectory? systemIconDirCache : systemIconFileCache).add(extension, icon);
            }
        }

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
     * Returns <code>null</code> if the given file doesn't exist.
     */
    private static Icon getSystemFileIcon(File file) {
        // Note: FileSystemView#getSystemIcon(File) returns bogus icons under Mac OS X
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X)
            return fileChooser.getIcon(file);

        try {
            // FileSystemView.getSystemIcon() will behave in the following way if the specified file doesn't exist when
            // the icon is requested:
            //  - throw a NullPointerException (caused by a java.io.FileNotFoundException)
            //  - dump the stack trace to System.err (bad! bad! bad!)
            //
            // A way to workaround this would be to test if the file exists when it is requested, but a/ this is an
            // expensive operation (especially under Windows) and b/ it wouldn't guarantee that the file effectively
            // exists when the icon is requested.
            // So the workaround here is to catch exceptions and disable System.err output during the call.

            Debug.setSystemErrEnabled(false);

            // Note: JFileChooser#getSystemIcon(File) returns bogus icons under Windows
            return fileSystemView.getSystemIcon(file);
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Caught exception while retrieving system icon for file "+file.getAbsolutePath()+" :"+e);
            return null;
        }
        finally {
            Debug.setSystemErrEnabled(true);
        }
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
     * Returns the current icon scale factor, initialized to {@link #DEFAULT_SCALE_FACTOR}.
     */
    public static float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Sets the icon scale factor.
     */
    public static void setScaleFactor(float factor) {
        scaleFactor = factor;
    }


    /**
     * Returns the current system icons policy, controlling when system file icons should be used instead
     * of custom file icons, see constant fields for possible values. The system icons policy is by default initialized
     * to {@link #DEFAULT_SYSTEM_ICONS_POLICY}.
     */
    public static String getSystemIconsPolicy() {
        return systemIconsPolicy;
    }


    /**
     * Sets the system icons policy, controlling when system file icons should be used instead of custom file icons.
     * See constants fields for allowed values.
     */
    public static void setSystemIconsPolicy(String policy) {
        systemIconsPolicy = policy;
    }


    /**
     * Returns <code>true</code> if the current platform is able to retrieve system icons that match the ones used in
     * the OS's default file manager. If <code>false</code> is returned and {@link #getSystemFileIcon(com.mucommander.file.AbstractFile)}
     * is used or {@link #getFileIcon(com.mucommander.file.AbstractFile)} together with a system policy different from
     * {@link #USE_SYSTEM_ICONS_NEVER}, the returned icon will probably look very bad. 
     *
     * @return true if the current platform is able to retrieve system icons that match the ones used in the OS's
     * default file manager
     */
    public static boolean hasProperSystemIcons() {
        return PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X || PlatformManager.isWindowsFamily();
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

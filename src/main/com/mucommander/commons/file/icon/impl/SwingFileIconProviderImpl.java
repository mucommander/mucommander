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


package com.mucommander.commons.file.icon.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.icon.CacheableFileIconProvider;
import com.mucommander.commons.file.icon.CachedFileIconProvider;
import com.mucommander.commons.file.icon.IconCache;
import com.mucommander.commons.file.icon.LocalFileIconProvider;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.commons.io.SilenceableOutputStream;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.net.URL;

/**
 * Package-protected class which provides the {@link com.mucommander.commons.file.icon.LocalFileIconProvider} and
 * {@link com.mucommander.commons.file.icon.CacheableFileIconProvider} implementations to {@link SwingFileIconProvider}.
 *
 * @see SwingFileIconProvider
 * @author Maxence Bernard
 */
class SwingFileIconProviderImpl extends LocalFileIconProvider implements CacheableFileIconProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwingFileIconProviderImpl.class);

    /** Swing object used to retrieve file icons, used on all platforms but Mac OS X */
    private static FileSystemView fileSystemView;

    /** Swing object used to retrieve file icons, used under Mac OS X only */
    private static JFileChooser fileChooser;

    /** Caches icons for directories, used only for non-local files */
    protected static IconCache directoryIconCache = CachedFileIconProvider.createCache();

    /** Caches icons for regular files, used only for non-local files */
    protected static IconCache fileIconCache = CachedFileIconProvider.createCache();

    /** True if init has been called */
    protected static boolean initialized;

    /** Name of the 'symlink' icon resource located in the same package as this class */
    private final static String SYMLINK_ICON_NAME = "link.png";

    /** Icon that is painted over a symlink's target file icon to symbolize a symlink to the target file. */
    protected static ImageIcon SYMLINK_OVERLAY_ICON;

    /** Allows stderr to be 'silenced' when needed */
    protected static SilenceableOutputStream errOut;


    /**
     * Initializes the Swing object used to retrieve icons the first time this method is called, does nothing
     * subsequent calls.
     * Note: instanciating this object is expensive (I/O bound) so we want to do that only if needed, and only once.
     */
    synchronized static void checkInit() {
        // This method is synchronized to ensure that the initialization happens only once
        if(initialized)
            return;

        if(OsFamily.MAC_OS_X.isCurrent())
            fileChooser = new JFileChooser();
        else
            fileSystemView = FileSystemView.getFileSystemView();

        // Loads the symlink overlay icon
        URL iconURL = ResourceLoader.getPackageResourceAsURL(SwingFileIconProviderImpl.class.getPackage(), SYMLINK_ICON_NAME);
        if(iconURL==null)
            throw new RuntimeException("Could not locate required symlink icon: "+SYMLINK_ICON_NAME);

        SYMLINK_OVERLAY_ICON = new ImageIcon(iconURL);

        // Replace stderr with a SilenceablePrintStream that can be 'silenced' when needed
        System.setErr(new PrintStream(errOut = new SilenceableOutputStream(System.err, false), true));

        initialized = true;
    }


    /**
     * Returns an icon for the given <code>java.io.File</code> using the underlying Swing provider component,
     * <code>null</code> in case of an error.
     *
     * @param javaIoFile the file for which to return an icon
     * @return an icon for the specified file, null in case of an unexpected error
     */
    private static Icon getSwingIcon(java.io.File javaIoFile) {
        try {
            if(fileSystemView!=null) {
                // FileSystemView.getSystemIcon() will behave in the following way if the specified file doesn't exist
                // when the icon is requested:
                //  - throw a NullPointerException (caused by a java.io.FileNotFoundException) => OK why not
                //  - dump the stack trace to System.err => bad! bad! bad!
                //
                // A way to workaround this odd behavior would be to test if the file exists when it is requested,
                // but a/ this is an expensive operation (especially under Windows) and b/ it wouldn't guarantee that
                // the file effectively exists when the icon is requested.
                // So the workaround here is to catch exceptions and 'silence' System.err output during the call.

                errOut.setSilenced(true);

                return fileSystemView.getSystemIcon(javaIoFile);
            }
            else {
                return fileChooser.getIcon(javaIoFile);
            }
        }
        catch(Exception e) {
            LOGGER.info("Caught exception while retrieving system icon for file {}", javaIoFile.getAbsolutePath(), e);
            return null;
        }
        finally {
            if(fileSystemView!=null)
                errOut.setSilenced(false);
        }
    }


    /**
     * Returns an icon symbolizing a symlink to the given target icon. The returned icon uses the specified icon as
     * its background and overlays a 'link' icon on top of it.
     *
     * @param targetFileIcon the icon representing the symlink's target
     * @return an icon symbolizing a symlink to the given target
     */
    private static ImageIcon getSymlinkIcon(Icon targetFileIcon) {
        BufferedImage bi = new BufferedImage(targetFileIcon.getIconWidth(), targetFileIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = bi.getGraphics();
        targetFileIcon.paintIcon(null, g, 0, 0);
        SYMLINK_OVERLAY_ICON.paintIcon(null, g, 0, 0);

        return new ImageIcon(bi);
    }

    /**
     * Returns the extension of the given file using {@link AbstractFile#getExtension()}. If the extension is
     * <code>null</code>, the empty string <code>""</code> is returned, making the returned extension safe for use
     * in a hash map where null keys are forbidden.
     *
     * @param file file on which to call {@link AbstractFile#getExtension}
     * @return the file's extension, may be the empty string but never <code>null</code>
     */
    private static String getCheckedExtension(AbstractFile file) {
        String extension = file.getExtension();
        return extension==null?"":extension;
    }


    //////////////////////////////////////////
    // LocalFileIconProvider implementation //
    //////////////////////////////////////////

    /**
     * <b>Implementation notes:</b> returns <code>false</code> (no caching) for:
     * <ul>
     *  <li>local files: their icons are cached by the Swing component that provides icons.</li>
     *  <li>symlinks: their icon cannot be cached using the file's extension as a key.</li>
     * </ul>
     * <code>true</code> is returned for non-local files that are not symlinks to avoid excessive temporary file
     * creation.
     */
    public boolean isCacheable(AbstractFile file, Dimension preferredResolution) {
        return !((file.getTopAncestor() instanceof LocalFile) || file.isSymlink());
    }

    public Icon lookupCache(AbstractFile file, Dimension preferredResolution) {
        // Under Mac OS X, return the icon of /Network for the root of remote (non-local) locations. 
        if(OsFamily.MAC_OS_X.isCurrent() && !FileProtocols.FILE.equals(file.getURL().getScheme()) && file.isRoot())
            return getSwingIcon(new java.io.File("/Network"));

        // Look for an existing icon instance for the file's extension
        return (file.isDirectory()? directoryIconCache : fileIconCache).get(getCheckedExtension(file));
    }

    public void addToCache(AbstractFile file, Icon icon, Dimension preferredResolution) {
        // Map the extension onto the given icon
        (file.isDirectory()? directoryIconCache : fileIconCache).put(getCheckedExtension(file), icon);
    }

    /**
     * <i>Implementation note:</i> only one resolution is available (usually 16x16) and blindly returned, the
     * <code>preferredResolution</code> argument is simply ignored.
     */
    @Override
    public Icon getLocalFileIcon(LocalFile localFile, AbstractFile originalFile, Dimension preferredResolution) {
        // Initialize the Swing object the first time this method is called
        checkInit();

        // Retrieve the icon using the Swing provider component
        Icon icon = getSwingIcon((java.io.File)localFile.getUnderlyingFileObject());

        // Add a symlink indication to the icon if:
        // - the original file is a symlink AND
        //   - the original file is not a local file OR
        //   - the original file is a local file but the Swing component generates icons which do not have a symlink
        // indication. That is the case on Mac OS X 10.5 (regression, 10.4 did this just fine).
        //
        // Note that the symlink test is performed last because it is the most expensive.
        //
        if((!(originalFile.getTopAncestor() instanceof LocalFile) || (OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_5.isCurrent()))
                && originalFile.isSymlink()) {
            icon = getSymlinkIcon(icon);
        }

        return icon;
    }
}

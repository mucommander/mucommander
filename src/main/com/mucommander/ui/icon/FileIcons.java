/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.icon;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.icon.FileIconProvider;
import com.mucommander.commons.runtime.OsFamily;

/**
 * <code>FileIcons</code> provides several methods to retrieve file icons for a given file:
 * <ul>
 *  <li>{@link #getSystemFileIcon(AbstractFile)}: returns a system icon, provided by the underlying OS/desktop manager.
 *   Under supported platforms, those file icons are the same as the ones displayed in the default file manager.</li>
 *  <li>{@link #getCustomFileIcon(AbstractFile)}: returns a custom icon, fetched from the muCommander icon set and
 * based on the file's kind (archive, folder...) and extension.</li>
 *  <li}{@link #getFileIcon(AbstractFile)} returns either a system icon or a custom icon, depending on the current
 * system icons policy. The default policy is {@link #DEFAULT_SYSTEM_ICONS_POLICY} and can be changed using
 * {@link #setSystemIconsPolicy(String)}.</li>
 * </ul>
 * Icons can be requested indifferently for any type of {@link AbstractFile} files: local files, remote files,
 * archives entries... The </p>
 *
 * <p>It is important to note that not all platforms have proper support for system file icons.
 * The {@link #hasProperSystemIcons()} method can be used to determine if the current platform properly supports system
 * icons. Non-supported platforms may return no icon (<code>null</code> values), or icons that do not resemble the
 * system ones.</p>
 *
 * @author Maxence Bernard
 */
public class FileIcons {

    /** Never use system file icons */
    public final static String USE_SYSTEM_ICONS_NEVER = "never";
    /** Use system file icons only for applications */
    public final static String USE_SYSTEM_ICONS_APPLICATIONS = "applications";
    /** Always use system file icons */
    public final static String USE_SYSTEM_ICONS_ALWAYS = "always";

    /** Default policy for system icons */
    public final static String DEFAULT_SYSTEM_ICONS_POLICY = USE_SYSTEM_ICONS_APPLICATIONS;

    /** Default icon scale factor (no rescaling) */
    public final static float DEFAULT_SCALE_FACTOR = 1.0f;

    /** Base width and height of icons for a scale factor of 1 */
    private final static int BASE_ICON_DIMENSION = 16;

    /** Controls if and when system file icons should be used instead of custom icons */
    private static String systemIconsPolicy = DEFAULT_SYSTEM_ICONS_POLICY;

    /** Current icon scale factor */
    private static float scaleFactor = DEFAULT_SCALE_FACTOR;

    /** FileIconProvider instance for custom icons */
    private static FileIconProvider customFileIconProvider;

    /** FileIconProvider instance for system icons */
    private static FileIconProvider systemFileIconProvider;

    /** Current dimension of returned file icons */
    private static Dimension iconDimension = new Dimension((int)(BASE_ICON_DIMENSION * DEFAULT_SCALE_FACTOR), (int)(BASE_ICON_DIMENSION * DEFAULT_SCALE_FACTOR));


    /**
     * Initializes the system and custom file icon providers.
     */
    static {
        setCustomFileIconProvider(new CustomFileIconProvider());
        setSystemFileIconProvider(FileFactory.getDefaultFileIconProvider());
    }


    /**
     * Shorthand for {@link #getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)} called with the
     * icon dimension returned by {@link #getIconDimension()}.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     * @return an icon for the given file
     * @see #getSystemIconsPolicy()
     */
    public static Icon getFileIcon(AbstractFile file) {
        return getFileIcon(file, iconDimension);
    }

    /**
     * Returns an icon for the given file and of the specified dimension.
     * The returned icon will either be a system icon, or one from the custom icon set, depending on the current
     * {@link #getSystemIconsPolicy() system icons policy}.
     * If a system icon should have been returned for the specified file but could not be resolved
     * ({@link #getSystemFileIcon(AbstractFile, Dimension)} returned <code>null</code>), an icon from the
     * custom icon set will be returned instead. Therefore, this method never returns <code>null</code>.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     * @param iconDimension the icon's dimension
     * @return an icon for the given file
     * @see #getSystemIconsPolicy()
     */
    public static Icon getFileIcon(AbstractFile file, Dimension iconDimension) {
        boolean systemIcon = false;

        if(USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy))
            systemIcon = true;

        if(USE_SYSTEM_ICONS_APPLICATIONS.equals(systemIconsPolicy))
            systemIcon = com.mucommander.desktop.DesktopManager.isApplication(file);

        if(systemIcon) {
            Icon icon = getSystemFileIcon(file, iconDimension);
            if(icon!=null)
                return icon;
            // If the system icon could not be resolved, return a custom file icon
        }

        return getCustomFileIcon(file, iconDimension);
    }


    /**
     * Shorthand for {@link #getCustomFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)} called with the
     * icon dimension returned by {@link #getIconDimension()}.
     *
     * @param file the file for which an icon is to be returned
     * @return a custom icon for the given file
     */
    public static Icon getCustomFileIcon(AbstractFile file) {
        return getCustomFileIcon(file, iconDimension);
    }

    /**
     * Returns an icon of the specified dimension for the given file. The icon is provided by the
     * {@link #getCustomFileIconProvider() custom file icon provider}. This method is guaranteed to never return
     * <code>null</code>.
     *
     * @param file the file for which an icon is to be returned
     * @param iconDimension the icon's dimension
     * @return a custom icon for the given file
     * @see #getCustomFileIconProvider()
     */
    public static Icon getCustomFileIcon(AbstractFile file, Dimension iconDimension) {
        return getFileProviderIcon(customFileIconProvider, file, iconDimension);
    }

    /**
     * Shorthand for {@link #getSystemFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)} called with the
     * icon dimension returned by {@link #getIconDimension()}.
     *
     * @param file the file for which an icon is to be returned
     * @return a system icon for the given file
     */
    public static Icon getSystemFileIcon(AbstractFile file) {
        return getSystemFileIcon(file, iconDimension);
    }

    /**
     * Returns an icon of the specified dimension for the given file. The returned icon is provided by the
     * underlying OS/desktop manager, using the {@link com.mucommander.commons.file.icon.FileIconProvider} currently set.
     * Returns <code>null</code> if the icon couldn't be retrieved, either because the file doesn't exist or for
     * any other reason.
     *
     * @param file the file for which an icon is to be returned
     * @param iconDimension the icon's dimension
     * @return a system icon for the given file
     */
    public static Icon getSystemFileIcon(AbstractFile file, Dimension iconDimension) {
        return getFileProviderIcon(systemFileIconProvider, file, iconDimension);
    }

    /**
     * Returns an icon of the specified dimension for the given file. The return icon is provided by the specified
     * {@link FileIconProvider}. This method takes care of up/down-scaling the icon returned by the provider if it
     * doesn't match the specified dimension.
     *
     * @param fip the FileIconProvider from which to fetch the icon
     * @param file the file for which an icon is to be returned
     * @param iconDimension the icon's dimension 
     * @return an icon for the specified file
     */
    private static Icon getFileProviderIcon(FileIconProvider fip, AbstractFile file, Dimension iconDimension) {
        Icon icon = fip.getFileIcon(file, iconDimension);
        if(icon==null)
            return null;

        if(iconDimension.width==icon.getIconWidth() && iconDimension.height==icon.getIconHeight())
            return icon;    // the icon already has the right dimension

        // Scale the icon to the target dimension
        ImageIcon imageIcon = IconManager.getImageIcon(icon);
        return new ImageIcon(imageIcon.getImage().getScaledInstance(iconDimension.width, iconDimension.height, Image.SCALE_AREA_AVERAGING));
    }


    /**
     * Returns the {@link com.mucommander.commons.file.icon.FileIconProvider} instance that provides 'custom' file icons.
     *
     * @return the FileIconProvider instance that provides 'custom' file icons.
     */
    public static FileIconProvider getCustomFileIconProvider() {
        return customFileIconProvider;
    }

    /**
     * Sets the {@link com.mucommander.commons.file.icon.FileIconProvider} instance that provides 'custom' file icons.
     *
     * @param fip the FileIconProvider instance that provides 'custom' file icons
     */
    public static void setCustomFileIconProvider(FileIconProvider fip) {
        customFileIconProvider = fip;
    }

    /**
     * Returns the {@link com.mucommander.commons.file.icon.FileIconProvider} instance that provides 'system' file icons.
     *
     * @return the FileIconProvider instance that provides 'custom' file icons.
     */
    public static FileIconProvider getSystemFileIconProvider() {
        return systemFileIconProvider;
    }

    /**
     * Sets the {@link com.mucommander.commons.file.icon.FileIconProvider} instance that provides 'custom' file icons.
     *
     * @param fip the FileIconProvider instance that provides 'custom' file icons
     */
    public static void setSystemFileIconProvider(FileIconProvider fip) {
        systemFileIconProvider = fip;
    }


    /**
     * Returns the dimension of file icons currently returned by this class, which is the base icon dimension (16x16)
     * multiplied by the current scale factor.
     *
     * @return the dimension of file icons currently returned by this class
     */
    public static Dimension getIconDimension() {
        return iconDimension;
    }

	
    /**
     * Returns the current icon scale factor, initialized by default to {@link #DEFAULT_SCALE_FACTOR}.
     *
     * @return the current icon scale factor
     */
    public static float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Sets the current icon scale factor. The given value must be greater than 0.
     *
     * @param factor the new icon scale factor to use
     * @throws IllegalArgumentException if factor is lower or equal to 0
     */
    public static void setScaleFactor(float factor) {
        if(scaleFactor<=0)
            throw new IllegalArgumentException("Scale factor must be greater than 0, ("+factor+")");

        scaleFactor = factor;
        iconDimension = new Dimension((int)(BASE_ICON_DIMENSION *scaleFactor), (int)(BASE_ICON_DIMENSION*scaleFactor));
    }


    /**
     * Returns the current system icons policy, controlling when system file icons should be used instead
     * of custom file icons, see constant fields for possible values. The system icons policy is by default initialized
     * to {@link #DEFAULT_SYSTEM_ICONS_POLICY}.
     *
     * @return the current system icons policy
     */
    public static String getSystemIconsPolicy() {
        return systemIconsPolicy;
    }


    /**
     * Sets the system icons policy, controlling when system file icons should be used instead of custom file icons.
     * See constants fields for allowed values.
     *
     * @param policy the new system icons policy to use
     */
    public static void setSystemIconsPolicy(String policy) {
        systemIconsPolicy = policy;
    }


    /**
     * Returns <code>true</code> if the current platform is able to retrieve system icons that match the ones used in
     * the OS's default file manager. If <code>false</code> is returned and {@link #getSystemFileIcon(com.mucommander.commons.file.AbstractFile)}
     * is used or {@link #getFileIcon(com.mucommander.commons.file.AbstractFile)} together with a system policy different from
     * {@link #USE_SYSTEM_ICONS_NEVER}, the returned icon will probably look very bad. 
     *
     * @return true if the current platform is able to retrieve system icons that match the ones used in the OS's
     * default file manager
     */
    public static boolean hasProperSystemIcons() {
        return OsFamily.MAC_OS_X.isCurrent() || OsFamily.WINDOWS.isCurrent();
    }
}

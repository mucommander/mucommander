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

package com.mucommander.ui.icon;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.icon.FileIconProvider;

import javax.swing.*;
import java.awt.*;

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
    private static Dimension iconDimension = new Dimension((int)(BASE_ICON_DIMENSION * DEFAULT_SCALE_FACTOR), (int)(BASE_ICON_DIMENSION * DEFAULT_SCALE_FACTOR));;


    /**
     * Initializes the system and custom file icon providers.
     */
    static {
        setCustomFileIconProvider(new CustomFileIconProvider());
        setSystemFileIconProvider(FileFactory.getDefaultFileIconProvider());
    }


    /**
     * Returns an icon for the given file. The returned icon will either a system icon, or one from the custom icon set,
     * depending on the current system icons policy.
     * This method <code>null</code> if an icon couldn't be retrieved, either because the file doesn't exist or for
     * any other reason.
     *
     * @param file the AbstractFile instance for which an icon will be returned
     * @return an icon for the given file
     * @see #getSystemIconsPolicy()
     */
    public static Icon getFileIcon(AbstractFile file) {
        if(USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy))
            return getSystemFileIcon(file);

        if(USE_SYSTEM_ICONS_APPLICATIONS.equals(systemIconsPolicy)) {
            String extension = file.getExtension();

            if(extension!=null) {
                boolean systemIcon;

                if(PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X && "app".equalsIgnoreCase(extension))
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
     * Returns an icon for the given file from the custom icon set, using the custom {@link com.mucommander.file.icon.FileIconProvider}
     * currently set. This method returns <code>null</code> if an icon couldn't be retrieved, either because the file
     * doesn't exist or for any other reason.</br>
     * The dimension of the returned icon is the one returned by {@link #getIconDimension()}.
     *
     * @param file the file for which an icon is to be returned
     * @return an icon from the custom icon set for the given file
     */
    public static Icon getCustomFileIcon(AbstractFile file) {
        return getFileProviderIcon(customFileIconProvider, file);
    }


    /**
     * Returns a system icon for the given file (one provided by the underlying OS/desktop manager), using the {@link com.mucommander.file.icon.FileIconProvider}
     * currently set. This method returns <code>null</code> if an icon couldn't be retrieved, either because the file
     * doesn't exist or for any other reason.</br>
     * The dimension of the returned icon is the one returned by {@link #getIconDimension()}.
     *
     * @param file the file for which an icon is to be returned
     * @return a system icon for the given file
     */
    public static Icon getSystemFileIcon(AbstractFile file) {
        return getFileProviderIcon(systemFileIconProvider, file);
    }

    /**
     * Fetches the file icon for the specified file from the {@link FileIconProvider} and returns it. This method
     * takes care of up/down-scaling the icon returned by the provider if it doesn't match the current icon dimension.
     *
     * @param fip the FileIconProvider from which to fetch the icon
     * @param file the file for which an icon is to be returned
     * @return an icon for the specified file fetched from the FileIconProvider
     */
    private static Icon getFileProviderIcon(FileIconProvider fip, AbstractFile file) {
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
     * Returns the {@link com.mucommander.file.icon.FileIconProvider} instance that provides 'custom' file icons.
     *
     * @return the FileIconProvider instance that provides 'custom' file icons.
     */
    public static FileIconProvider getCustomFileIconProvider() {
        return customFileIconProvider;
    }

    /**
     * Sets the {@link com.mucommander.file.icon.FileIconProvider} instance that provides 'custom' file icons.
     *
     * @param fip the FileIconProvider instance that provides 'custom' file icons
     */
    public static void setCustomFileIconProvider(FileIconProvider fip) {
        customFileIconProvider = fip;
    }

    /**
     * Returns the {@link com.mucommander.file.icon.FileIconProvider} instance that provides 'system' file icons.
     *
     * @return the FileIconProvider instance that provides 'custom' file icons.
     */
    public static FileIconProvider getSystemFileIconProvider() {
        return systemFileIconProvider;
    }

    /**
     * Sets the {@link com.mucommander.file.icon.FileIconProvider} instance that provides 'custom' file icons.
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
     * Returns the current icon scale factor, initialized to {@link #DEFAULT_SCALE_FACTOR}.
     *
     * @return the current icon scale factor
     */
    public static float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Sets the current icon scale factor to the given one.
     *
     * @param factor the new icon scale factor to use
     */
    public static void setScaleFactor(float factor) {
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
     * the OS's default file manager. If <code>false</code> is returned and {@link #getSystemFileIcon(com.mucommander.file.AbstractFile)}
     * is used or {@link #getFileIcon(com.mucommander.file.AbstractFile)} together with a system policy different from
     * {@link #USE_SYSTEM_ICONS_NEVER}, the returned icon will probably look very bad. 
     *
     * @return true if the current platform is able to retrieve system icons that match the ones used in the OS's
     * default file manager
     */
    public static boolean hasProperSystemIcons() {
        return PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X || PlatformManager.isWindowsFamily();
    }
}

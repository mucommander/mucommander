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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.util.ResourceLoader;

/**
 * IconManager takes care of loading, caching, rescaling the icons contained inside the application's JAR file.
 *
 * @author Maxence Bernard
 */
public class IconManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(IconManager.class);
	
    /** Caches for the different icon sets */
    private final static Map<String, ImageIcon> caches[];

    /** Designates the file icon set */
    public final static int FILE_ICON_SET        = 0;
    /** Designates the action icon set */
    public final static int ACTION_ICON_SET      = 1;
    /** Designates the toolbar icon set */
    public final static int STATUS_BAR_ICON_SET  = 2;
    /** Designates the table icon set */
    public final static int COMMON_ICON_SET      = 3;
    /** Designates the preferences icon set */
    public final static int PREFERENCES_ICON_SET = 4;
    /** Designates the progress icon set */
    public final static int PROGRESS_ICON_SET    = 5;
    /** Designates the language icon set */
    public final static int LANGUAGE_ICON_SET    = 6;
    /** Designates the mucommander icon set */
    public final static int MUCOMMANDER_ICON_SET = 7;

    /** Base folder of all images */
    private final static String BASE_IMAGE_FOLDER = "/images";
    /** Icon sets folders within the application's JAR file */
    private final static String ICON_SET_FOLDERS[] = {
        BASE_IMAGE_FOLDER +"/file/",
        BASE_IMAGE_FOLDER +"/action/",
        BASE_IMAGE_FOLDER +"/status_bar/",
        BASE_IMAGE_FOLDER +"/common/",
        BASE_IMAGE_FOLDER +"/preferences/",
        BASE_IMAGE_FOLDER +"/progress/",
        BASE_IMAGE_FOLDER +"/language/",
        BASE_IMAGE_FOLDER +"/mucommander/"
    };


    static {
        // Initialize caches for icon sets that need it.
        // Icons which are displayed once in a while like preferences icons don't need to be cached
        caches = new Hashtable[ICON_SET_FOLDERS.length];
        caches[FILE_ICON_SET]       = new Hashtable<String, ImageIcon>();
        caches[ACTION_ICON_SET]     = new Hashtable<String, ImageIcon>();
        caches[STATUS_BAR_ICON_SET] = new Hashtable<String, ImageIcon>();
        caches[COMMON_ICON_SET]     = new Hashtable<String, ImageIcon>();
        caches[PROGRESS_ICON_SET]   = new Hashtable<String, ImageIcon>();
    }


    /**
     * Creates a new instance of IconManager.
     */
    private IconManager() {}


    /**
     * Creates and returns an ImageIcon instance using the specified icon path and scale factor. No caching.
     *
     * @param iconPath path of the icon resource inside the application's JAR file
     * @param scaleFactor the icon scale factor, <code>1.0f</code> to have the icon in its original size (no rescaling)
     */
    public static ImageIcon getIcon(String iconPath, float scaleFactor) {
        URL resourceURL = ResourceLoader.getResourceAsURL(iconPath);
        if(resourceURL==null) {
            LOGGER.debug("Warning: attempt to load non-existing icon: "+iconPath+" , icon missing ?");
            return null;
        }

        ImageIcon icon = new ImageIcon(resourceURL);
        return scaleFactor==1.0f?icon:getScaledIcon(icon, scaleFactor);
    }

    /**
     * Convenience method, calls and returns the result of {@link #getIcon(String, float) getIcon(iconPath, scaleFactor)}
     * with a scale factor of 1.0f (no rescaling).
     */
    public static ImageIcon getIcon(String iconPath) {
        return getIcon(iconPath, 1.0f);
    }


    /**
     * Returns a scaled version of the given ImageIcon instance, using the specified scale factor.
     *
     * @param icon the icon to scale.
     * @param scaleFactor the icon scale factor, <code>1.0f</code> to have the icon in its original size (no rescaling)
     */
    public static ImageIcon getScaledIcon(ImageIcon icon, float scaleFactor) {
        if(scaleFactor==1.0f || icon==null)
            return icon;

        Image image = icon.getImage();
        return new ImageIcon(image.getScaledInstance((int)(scaleFactor*image.getWidth(null)), (int)(scaleFactor*image.getHeight(null)), Image.SCALE_AREA_AVERAGING));
    }

    /**
     * Returns a 'composite' icon made by composing the two given icons: the <code>backgroundIcon</code> is painted
     * first, and the <code>foregroundIcon</code> is superposed, letting its non-transparent pixels reveal the
     * background icon.
     * For this method to provide a meaningful result, the two icons should have the same dimensions and the
     * <code>foreground</code> should have some transparent pixels.
     *
     * @param backgroundIcon the icon that is painted first
     * @param foregroundIcon the icon that is superposed above backgroundIcon, should use transparency
     * @return a 'composite' icon made by composing the two given icons
     */
    public static ImageIcon getCompositeIcon(Icon backgroundIcon, Icon foregroundIcon) {
        BufferedImage bi = new BufferedImage(backgroundIcon.getIconWidth(), backgroundIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = bi.getGraphics();
        backgroundIcon.paintIcon(null, g, 0, 0);
        foregroundIcon.paintIcon(null, g, 0, 0);

        return new ImageIcon(bi);
    }


    /**
     * Returns an icon in the specified icon set and with the given name. If a scale factor other than 1.0f is passed,
     * the return icon will be scaled accordingly.
     *
     * <p>If the icon set has a cache, first looks for an existing instance in the cache, and if it couldn't be found, 
     * create an instance and store it in the cache for future access. Note that the cached icon is unscaled, i.e.
     * the scaled icon is not cached.</p>
     *
     * @param iconSet an icon set (see public constants for possible values)
     * @param iconName filename of the icon to retrieve
     * @param scaleFactor the icon scale factor, <code>1.0f</code> to have the icon in its original size (no rescaling)
     * @return an ImageIcon instance corresponding to the specified icon set, name and scale factor,
     * <code>null</code> if the image wasn't found or couldn't be loaded
     */
    public static ImageIcon getIcon(int iconSet, String iconName, float scaleFactor) {
        Map<String, ImageIcon> cache = caches[iconSet];
        ImageIcon icon;

        if(cache==null) {
            // No caching, simply create the icon
            icon = getIcon(ICON_SET_FOLDERS[iconSet]+iconName);
        }
        else {
            // Look for the icon in the cache
            icon = cache.get(iconName);
            if(icon==null) {
                // Icon is not in the cache, let's create it
                icon = getIcon(ICON_SET_FOLDERS[iconSet]+iconName);
                // and add it to the cache if icon exists
                if(icon!=null)
                    cache.put(iconName, icon);
            }
        }

        if(icon==null)
            return null;

        return scaleFactor==1.0f?icon:getScaledIcon(icon, scaleFactor);
    }


    /**
     * Convenience method, calls and returns the result of {@link #getIcon(int, String, float) getIcon(iconSet, iconName, scaleFactor)}
     * with a scale factor of 1.0f (no rescaling).
     */
    public static ImageIcon getIcon(int iconSet, String iconName) {
        return getIcon(iconSet, iconName, 1.0f);
    }


    /**
     * Returns an icon made of the specified icon and some transparent space around it.
     *
     * @param icon the original icon, will be painted at the center of the new icon
     * @param insets specifies the dimensions of the transparent space around the returned icon
     * @return an icon made of the specified icon and some transparent space around it
     */
    public static ImageIcon getPaddedIcon(ImageIcon icon, Insets insets) {
        BufferedImage bi = new BufferedImage(
                icon.getIconWidth()+insets.left+insets.right,
                icon.getIconHeight()+insets.top+insets.bottom,
                BufferedImage.TYPE_INT_ARGB);

        Graphics g = bi.getGraphics();
        g.drawImage(icon.getImage(), insets.left, insets.top, null);

        return new ImageIcon(bi);
    }



    /**
     * Creates and returns an ImageIcon with the same content and dimensions. This method is useful when an ImageIcon
     * is needed and only an Icon is available.
     *
     * <p>If the given Icon is already an ImageIcon, the same instance is returned. If it is not, a new ImageIcon is 
     * created and returned.
     */
    public static ImageIcon getImageIcon(Icon icon) {
        if(icon instanceof ImageIcon)
            return (ImageIcon)icon;

        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        icon.paintIcon(null, bi.getGraphics(), 0, 0);

        return new ImageIcon(bi);
    }


    /**
     * Returns the path to the folder that contains the image resource files of the given icon set.
     * The returned path is relative to the application JAR file's root and contains a trailing slash.
     *
     * @param iconSet an icon set (see public constants for possible values)
     */
    public static String getIconSetFolder(int iconSet) {
        return ICON_SET_FOLDERS[iconSet];
    }
}

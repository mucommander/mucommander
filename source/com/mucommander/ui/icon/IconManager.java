
package com.mucommander.ui.icon;

import com.mucommander.file.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Hashtable;

/**
 * IconManager takes care of loading, caching, rescaling the icons contained inside the application's JAR file.
 *
 * @author Maxence Bernard
 */
public class IconManager {

    /** Singleton instance */
    private final static IconManager instance = new IconManager();

    /** Class instance used to retrieve JAR resource files  */
    private final static Class classInstance = instance.getClass();

    /** Caches for the different icon sets */
    private final static Hashtable caches[];

    /** Designates the file icon set */
    public final static int FILE_ICON_SET = 0;
    /** Designates the action icon set */
    public final static int ACTION_ICON_SET = 1;
    /** Designates the toolbar icon set */
    public final static int STATUS_BAR_ICON_SET = 2;
    /** Designates the table icon set */
    public final static int COMMON_ICON_SET = 3;
    /** Designates the preferences icon set */
    public final static int PREFERENCES_ICON_SET = 4;
    /** Designates the progress icon set */
    public final static int PROGRESS_ICON_SET = 5;

    /** Icon sets folders within the application's JAR file */
    private final static String ICON_SET_FOLDERS[] = {
        "/file/",
        "/action/",
        "/status_bar/",
        "/common/",
        "/preferences/",
        "/progress/"
    };

    /** Number of icon sets */
    private final static int NB_ICON_SETS = 6;

    static {
        // Initialize caches for icon sets that need it.
        // Icons which are displayed once in a while like preferences icons don't need to be cached
        caches = new Hashtable[NB_ICON_SETS];
        caches[FILE_ICON_SET] = new Hashtable();
        caches[ACTION_ICON_SET] = new Hashtable();
        caches[STATUS_BAR_ICON_SET] = new Hashtable();
        caches[COMMON_ICON_SET] = new Hashtable();
        caches[PROGRESS_ICON_SET] = new Hashtable();
    }


    /**
     * Creates a new instance of IconManager.
     */
    private IconManager() {
    }


    /**
     * Creates and returns an ImageIcon instance using the specified icon path and scale factor. No caching.
     *
     * @param iconPath path of the icon resource inside the application's JAR file
     * @param scaleFactor the icon scale factor, <code>1.0f</code> to have the icon in its original size (no rescaling)
     */
    public static ImageIcon getIcon(String iconPath, float scaleFactor) {
        URL resourceURL = ResourceLoader.getResource(iconPath);
        if(resourceURL==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Warning: attempt to load non-existing icon: "+iconPath+" , icon missing ?");
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
     * Returns an icon in the specified icon set and with the given name. If a scale factor other than 1.0f is passed,
     * the return icon will be scaled accordingly.
     *
     * <p>If the icon set has a cache, first looks for an existing instance in the cache, and if it couldn't be found, 
     * create an instance and store it in the cache for future access. Note that the cached icon is unscaled, i.e.
     * the scaled icon is not cached.</p>
     *
     * @param iconSet an icon set (see public constants)
     * @param iconName filename of the icon to retrieve
     * @param scaleFactor the icon scale factor, <code>1.0f</code> to have the icon in its original size (no rescaling)
     * @return an ImageIcon instance corresponding to the specified icon set, name and scale factor,
     * <code>null</code> if the image wasn't found or couldn't be loaded
     */
    public static ImageIcon getIcon(int iconSet, String iconName, float scaleFactor) {
        Hashtable cache = caches[iconSet];
        ImageIcon icon;

        if(cache==null) {
            // No caching, simply create the icon
            icon = getIcon(ICON_SET_FOLDERS[iconSet]+iconName);
        }
        else {
            // Look for the icon in the cache
            icon = (ImageIcon)cache.get(iconName);
            if(icon==null) {
                // Icon is not in the cache, let's create it
                icon = getIcon(ICON_SET_FOLDERS[iconSet]+iconName);
                // and add it to the cache
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
}

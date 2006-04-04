
package com.mucommander.ui.icon;

import com.mucommander.conf.*;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Dimension;

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
	
	/** Scale factors for the different icon sets */
	private final static float scaleFactors[];

	/** Caches for the different icon sets */
	private final static Hashtable caches[];

	/** Designates file icon set */	
	public final static int FILE_ICON_SET = 0;
	/** Designates toolbar icon set */	
	public final static int TOOLBAR_ICON_SET = 1;
	/** Designates command bar icon set */	
	public final static int COMMAND_BAR_ICON_SET = 2;
	/** Designates table icon set */	
	public final static int TABLE_ICON_SET = 3;
	/** Designates preferences icon set */	
	public final static int PREFERENCES_ICON_SET = 4;
	
	/** Icon sets folders within the application's JAR file */	
	private final static String ICON_SET_FOLDERS[] = {
		"/file_icons/",
		"/toolbar_icons/",
		"/command_bar_icons/",
		"/table_icons/",
		"/preferences_icons/"
	};

	/** Number of icon sets */
	private final static int NB_ICON_SETS = 5;

	/** Configuration variable key for table icons scale */
	public final static String FILE_ICON_SCALE_CONF_VAR = "prefs.file_table.icon_scale";
	/** Configuration variable key for toolbar icons scale */
	public final static String TOOLBAR_ICON_SCALE_CONF_VAR = "prefs.toolbar.icon_scale";
	/** Configuration variable key for command bar icons scale */
	public final static String COMMAND_BAR_ICON_SCALE_CONF_VAR = "prefs.command_bar.icon_scale";


	static {
		// Initialize scale factors, defaut is 1.0f
		scaleFactors = new float[NB_ICON_SETS];
		for(int i=0; i<NB_ICON_SETS; i++)
			scaleFactors[i] = 1.0f;
		
		// Retrieves scale factors from configuration file
		scaleFactors[FILE_ICON_SET] = ConfigurationManager.getVariableFloat(FILE_ICON_SCALE_CONF_VAR, 1.0f);
		scaleFactors[TOOLBAR_ICON_SET] = ConfigurationManager.getVariableFloat(TOOLBAR_ICON_SCALE_CONF_VAR, 1.0f);
		scaleFactors[COMMAND_BAR_ICON_SET] = ConfigurationManager.getVariableFloat(COMMAND_BAR_ICON_SCALE_CONF_VAR, 1.0f);
	
		// Initialize caches for icon sets that need it.
		// Icons which are displayed once in a while like preferences icons don't need to be cached
		caches = new Hashtable[NB_ICON_SETS];
		caches[FILE_ICON_SET] = new Hashtable();
		caches[TOOLBAR_ICON_SET] = new Hashtable();
		caches[COMMAND_BAR_ICON_SET] = new Hashtable();
		caches[TABLE_ICON_SET] = new Hashtable();
	}


	/**
	 * Creates a new instance of IconManager.
	 */
	private IconManager() {
	}
	

	/**
	 * Creates and returns an ImageIcon instance using the specified icon path and scale factor. No caching.
	 */
	public static ImageIcon getIcon(String iconPath, float scaleFactor) {
		try {
			ImageIcon icon = new ImageIcon(classInstance.getResource(iconPath));
			
			if(scaleFactor==1.0f)
				return icon;
			
			Image image = icon.getImage();
			return new ImageIcon(image.getScaledInstance((int)(scaleFactor*image.getWidth(null)), (int)(scaleFactor*image.getHeight(null)), Image.SCALE_AREA_AVERAGING));
		}
		catch(Exception e) {
			// An exception is thrown by ImageIcon if the image doesn't exist or could not be properly read
			if(com.mucommander.Debug.ON)
				com.mucommander.Debug.trace("/!\\/!\\/!\\ "+e+" caught while trying to load icon "+iconPath+", icon missing ?");
			return null;
		}
	}

	/**
	 * Convenience method for retrieving an ImageIcon. No caching. No rescaling.
	 */
	public static ImageIcon getIcon(String iconPath) {
		return getIcon(iconPath, 1.0f);
	}


	/**
	 * Sets a new scale factor for the specified icon set. If the scale factor has changed and the icon set
	 * has a cache, the cache will be reset so that it won't return icons of the previous scale. 
	 *
	 * @param iconSet an icon set (see public constants)
	 * @scaleFactor the icon scale factor, <code>1.0f</code> to use the original icon's size (no rescaling)
	 */
	public static void setScaleFactor(int iconSet, float scaleFactor) {
		// Empty icon set's cache if scale factor has changed for this icon set
		if(scaleFactor!=scaleFactors[iconSet] && caches[iconSet]!=null)
			caches[iconSet] = new Hashtable();

		scaleFactors[iconSet] = scaleFactor;
	}
	
	
	/**
	 *
	 * @param iconSet an icon set (see public constants)
	 * @return the icon scale factor, <code>1.0f</code> means the original icon's size (no rescaling)
	 */
	public static float getScaleFactor(int iconSet) {
		return scaleFactors[iconSet];
	}
	

	/**
	 * Returns an icon in the specified icon set and with the given name. The current scale factor for the
	 * icon set will be used.
	 *
	 * <p>If the icon set has a cache, first looks for an existing instance in the cache, and if it couldn't be found, 
	 * create an instance and store it in the cache for future access.</p>
	 *
	 * @param iconSet an icon set (see public constants)
	 * @param iconName filename of the icon to retrieve
	 * @return an ImageIcon instance corresponding to the specified icon set and name, and current scale factor,
	 * <code>null</code> if the image wasn't found or couldn't be loaded
	 */
	public static ImageIcon getIcon(int iconSet, String iconName) {
		Hashtable cache = caches[iconSet];
		if(cache==null) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("no cache for icon set="+iconSet);
			// No caching, simply create the icon and return it
			return getIcon(ICON_SET_FOLDERS[iconSet]+iconName, scaleFactors[iconSet]);
		}
		else {
			// Look for the icon in the cache
			ImageIcon icon = (ImageIcon)cache.get(iconName);
			if(icon==null) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cache miss for icon="+iconName+" icon set="+iconSet);
				// Icon is not in the cache, let's create it
				icon = getIcon(ICON_SET_FOLDERS[iconSet]+iconName, scaleFactors[iconSet]);
				// and add it to the cache
				cache.put(iconName, icon);
			}
			return icon;
		}		
	}


	///////////////////////////////////
	// ConfigurationListener methods //
	///////////////////////////////////

	
    /**
     * Listens to certain configuration variables.
     */
// ConfigurationListener cannot be used to empty caches as the order in which ConfigurationManager notifies
// its listeners is not predicable
/*
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();

		// Clears caches for which the icon scale has changed, to force
		// the creation of new ImageIcon instances with the new scale
		
		if (var.equals(FILE_TABLE_ICON_SCALE_CONF_VAR)) {
			fileIconScaleFactor = event.getFloatValue();
			fileIconsCache = new Hashtable();
		}
		else if (var.equals(TOOLBAR_ICON_SCALE_CONF_VAR)) {
			toolBarIconScaleFactor = event.getFloatValue();
			toolBarIconsCache = new Hashtable();
		}
		else if (var.equals(COMMAND_BAR_ICON_SCALE_CONF_VAR)) {
			commandBarIconScaleFactor = event.getFloatValue();
			commandBarIconsCache = new Hashtable();
		}
	
		return true;
	}
*/

}
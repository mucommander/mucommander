
package com.mucommander.ui.icon;

import com.mucommander.conf.*;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Dimension;

import java.util.Hashtable;

/**
 * IconManager takes care of loading, caching, scaling the icons contained inside the application's JAR file.s
 *
 * @author Maxence Bernard
 */
public class IconManager implements ConfigurationListener {
	
	/** Singleton instance */
	private final static IconManager instance = new IconManager();
	
	/** Class instance used to retrieve JAR resource files  */
	private final static Class classInstance = instance.getClass();
	
	/** Contains cached ImageIcon instances for file icons */
	private static Hashtable fileIconsCache = new Hashtable();

	/** Contains cached ImageIcon instances for toolbar icons */
	private static Hashtable toolBarIconsCache = new Hashtable();

	/** Contains cached ImageIcon instances for command bar icons */
	private static Hashtable commandBarIconsCache = new Hashtable();
	
	/** Contains cached ImageIcon instances for table icons */
	private static Hashtable tableIconsCache = new Hashtable();
	
	/** File icons folder within the application's JAR file */	
	private final static String FILE_ICONS_FOLDER = "/file_icons/";
	/** Toolbar icons folder within the application's JAR file */	
	private final static String TOOLBAR_ICONS_FOLDER = "/toolbar_icons/";
	/** Command bar icons folder within the application's JAR file */	
	private final static String COMMANDBAR_ICONS_FOLDER = "/command_bar_icons/";
	/** Preferences icons folder within the application's JAR file */	
	private final static String PREFERENCES_ICONS_FOLDER = "/preferences_icons/";
	/** Table icons folder within the application's JAR file */	
	private final static String TABLE_ICONS_FOLDER = "/table_icons/";

	/** Configuration variable key for table icons scale */
	public final static String FILE_TABLE_ICON_SCALE_CONF_VAR = "prefs.file_table.icon_scale";
	/** Configuration variable key for toolbar icons scale */
	public final static String TOOLBAR_ICON_SCALE_CONF_VAR = "prefs.toolbar.icon_scale";
	/** Configuration variable key for command bar icons scale */
	public final static String COMMAND_BAR_ICON_SCALE_CONF_VAR = "prefs.command_bar.icon_scale";

	/** Scale factor for file icons, default is 1.0 */
	private static float fileIconScaleFactor = ConfigurationManager.getVariableFloat(FILE_TABLE_ICON_SCALE_CONF_VAR, 1.0f);
	/** Scale factor for toolbar icons, default is 1.0 */
	private static float toolBarIconScaleFactor = ConfigurationManager.getVariableFloat(TOOLBAR_ICON_SCALE_CONF_VAR, 1.0f);
	/** Scale factor for command bar icons, default is 1.0 */
	private static float commandBarIconScaleFactor = ConfigurationManager.getVariableFloat(COMMAND_BAR_ICON_SCALE_CONF_VAR, 1.0f);
	
	
	private IconManager() {
		// Listener to changes in icon scale configuration variables
		ConfigurationManager.addConfigurationListener(this);
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
	 * Convenience method for retrieving an ImageIcon with a default 1.0f scale factor. No caching.
	 */
	public static ImageIcon getIcon(String iconPath) {
		return getIcon(iconPath, 1.0f);
	}


	/**
	 * Returns an ImageIcon instance corresponding to the specificied parameters.
	 * First looks an existing instance in the given cache, and if it couldn't be found, create an instance
	 * and store it in the cache for later retrieval.
	 *
	 * @param iconsCache icon cache to use for storage and retrieval
	 * @param iconFolder base icon folder
	 * @param iconName filename of the icon to retrieve
	 * @param iconScaleFactor the icon scale/magnification factor
	 * @return an ImageIcon instance corresponding to the specificied parameters
	 */
	private static ImageIcon getCachedIcon(Hashtable iconsCache, String iconFolder, String iconName, float iconScaleFactor) {
		ImageIcon icon = (ImageIcon)iconsCache.get(iconName);
		
		if(icon==null) {
			// Icon is not in cache, let's create it
			icon = getIcon(iconFolder+iconName, iconScaleFactor);
			// and add it to the cache if it is not null (should never be)
			if(icon!=null)
				iconsCache.put(iconName, icon);
		}
		
		return icon;
	}


	/**
	 * Returns an ImageIcon instance corresponding to the specified file icon's filename, and
	 * the icon scale factor set in the preferences.
	 *
	 * <p>File icons are cached so that they can be shared across file tables: 
	 * an ImageIcon is created the first time it is requested and stored in a cache,
	 * the same instance will be returned when the icon is requested again.</p>
	 *
	 * @param iconName filename of the icon to retrieve
	 * @return an ImageIcon instance corresponding to the specified file icon's filename and scale factor
	 */
	public static ImageIcon getFileIcon(String iconName) {
		return getCachedIcon(fileIconsCache, FILE_ICONS_FOLDER, iconName, fileIconScaleFactor);
	}


	/**
	 * Returns the size of file icons, taking into account the icon scale factor set in the preferences.
	 */
	public static Dimension getFileIconSize() {
		int dim = (int)(16 * fileIconScaleFactor);
		return new Dimension(dim, dim);
	}


	/**
	 * Returns an ImageIcon instance corresponding to the specified toolbar icon's filename, and
	 * the icon scale factor set in the preferences.
	 *
	 * <p>Toolbar icons are cached so that they can be shared across application windows: 
	 * an ImageIcon is created the first time it is requested and stored in a cache,
	 * the same instance will be returned when the icon is requested again.</p>
	 *
	 * @param iconName filename of the icon to retrieve
	 * @return an ImageIcon instance corresponding to the specified toolbar icon's filename and scale factor
	 */
	public static ImageIcon getToolBarIcon(String iconName) {
		return getCachedIcon(toolBarIconsCache, TOOLBAR_ICONS_FOLDER, iconName, toolBarIconScaleFactor);
	}


	/**
	 * Returns an ImageIcon instance corresponding to the specified command bar icon's filename, and
	 * the icon scale factor set in the preferences.
	 *
	 * <p>Command bar icons are cached so that they can be shared across application windows:
	 * an ImageIcon is created the first time it is requested and stored in a cache,
	 * the same instance will be returned when the icon is requested again.</p>
	 *
	 * @param iconName filename of the icon to retrieve
	 * @return an ImageIcon instance corresponding to the specified command bar icon's filename and scale factor
	 */
	public static ImageIcon getCommandBarIcon(String iconName) {
		return getCachedIcon(commandBarIconsCache, COMMANDBAR_ICONS_FOLDER, iconName, commandBarIconScaleFactor);
	}


	/**
	 * Returns an ImageIcon instance corresponding to the specified table icon's filename.
	 *
	 * <p>Table icons are cached so that they can be shared across file tables:
	 * an ImageIcon is created the first time it is requested and stored in a cache,
	 * the same instance will be returned when the icon is requested again.</p>
	 *
	 * @param iconName filename of the icon to retrieve
	 * @return an ImageIcon instance corresponding to the specified table bar icon's filename
	 */
	public static ImageIcon getTableIcon(String iconName) {
		return getCachedIcon(tableIconsCache, TABLE_ICONS_FOLDER, iconName, 1.0f);
	}


	/**
	 * Returns an ImageIcon instance corresponding to the specified preferences icon's filename.
	 * Preferences icons are not cached.
	 *
	 * @param iconName filename of the icon to retrieve
	 * @return an ImageIcon instance corresponding to the specified preferences bar icon's filename
	 */
	public static ImageIcon getPreferencesIcon(String iconName) {
		return getIcon(PREFERENCES_ICONS_FOLDER+iconName, 1.0f);	
	}


	///////////////////////////////////
	// ConfigurationListener methods //
	///////////////////////////////////
	
    /**
     * Listens to certain configuration variables.
     */
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
}
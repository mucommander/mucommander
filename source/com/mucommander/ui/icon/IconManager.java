
package com.mucommander.ui.icon;

import com.mucommander.conf.*;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Dimension;

import java.util.Hashtable;

/**
 * IconManager takes care of loading icons inside the application's JAR file and provides
 * simple methods to retrieve icon instances.
 *
 * @author Maxence Bernard
 */
public class IconManager implements ConfigurationListener {
	
	/** Singleton instance */
	private final static IconManager instance = new IconManager();
	
	/** Class instance used to retrieve JAR resource files  */
	private final static Class classInstance = instance.getClass();
	
	private static Hashtable fileIconsCache = new Hashtable();

	private static Hashtable toolBarIconsCache = new Hashtable();

	private static Hashtable commandBarIconsCache = new Hashtable();
	
	
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

	public final static String FILE_TABLE_ICON_SCALE_CONF_VAR = "prefs.file_table.icon_scale";
	public final static String TOOLBAR_ICON_SCALE_CONF_VAR = "prefs.toolbar.icon_scale";
	public final static String COMMAND_BAR_ICON_SCALE_CONF_VAR = "prefs.command_bar.icon_scale";

	/** Scale factor for file icons, default is 1.0 */
	private static float fileIconScaleFactor = ConfigurationManager.getVariableFloat(FILE_TABLE_ICON_SCALE_CONF_VAR, 1.0f);
	/** Scale factor for toolbar icons, default is 1.0 */
	private static float toolBarIconScaleFactor = ConfigurationManager.getVariableFloat(TOOLBAR_ICON_SCALE_CONF_VAR, 1.0f);
	/** Scale factor for command bar icons, default is 1.0 */
	private static float commandBarIconScaleFactor = ConfigurationManager.getVariableFloat(COMMAND_BAR_ICON_SCALE_CONF_VAR, 1.0f);
	
	
	private IconManager() {
		ConfigurationManager.addConfigurationListener(this);
	}
	

	/**
	 * 
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

	public static ImageIcon getIcon(String iconPath) {
		return getIcon(iconPath, 1.0f);
	}


	private static ImageIcon getCachedIcon(Hashtable iconsCache, String iconFolder, String iconName, float iconScaleFactor) {
		ImageIcon icon = (ImageIcon)iconsCache.get(iconName);
		
		if(icon==null) {
//			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("icon "+iconFolder+iconName+" not in cache");
			// Icon is not in cache, let's create it
			icon = getIcon(iconFolder+iconName, iconScaleFactor);
			// and add it to the cache if it is not null (should never be)
			if(icon!=null)
				iconsCache.put(iconName, icon);
		}
//		else if(com.mucommander.Debug.ON) {
//			com.mucommander.Debug.trace("retrieved cached "+iconFolder+iconName+" icon");
//		}
		
		return icon;
	}


	public static ImageIcon getFileIcon(String iconName) {
		return getCachedIcon(fileIconsCache, FILE_ICONS_FOLDER, iconName, fileIconScaleFactor);
	}


	public static Dimension getFileIconSize() {
		int dim = (int)(16 * fileIconScaleFactor);
		return new Dimension(dim, dim);
	}


	public static ImageIcon getToolBarIcon(String iconName) {
		return getCachedIcon(toolBarIconsCache, TOOLBAR_ICONS_FOLDER, iconName, toolBarIconScaleFactor);
	}


	public static ImageIcon getCommandBarIcon(String iconName) {
		return getCachedIcon(commandBarIconsCache, COMMANDBAR_ICONS_FOLDER, iconName, commandBarIconScaleFactor);
	}


	public static ImageIcon getPreferencesIcon(String iconName) {
		return getIcon(PREFERENCES_ICONS_FOLDER+iconName, 1.0f);	
	}


	public static ImageIcon getTableIcon(String iconName) {
		return getIcon(TABLE_ICONS_FOLDER+iconName, 1.0f);	
	}


	///////////////////////////////////
	// ConfigurationListener methods //
	///////////////////////////////////
	
    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();

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
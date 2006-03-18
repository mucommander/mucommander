
package com.mucommander.ui.icon;

import javax.swing.ImageIcon;


public class IconManager {
	
	/** A Class instance, used to retrieve JAR resource files  */
	private static Class classInstance = Runtime.getRuntime().getClass();
	
	/** File icons folder within the application's JAR file */	
	private final static String FILE_ICONS_FOLDER = "/file_icons/";
	/** Toolbar icons folder within the application's JAR file */	
	private final static String TOOLBAR_ICONS_FOLDER = "/toolbar_icons/";
	/** Command bar icons folder within the application's JAR file */	
	private final static String COMMANDBAR_ICONS_FOLDER = "/command_bar_icons/";
	/** Preferences icons folder within the application's JAR file */	
	private final static String PREFERENCES_ICONS_FOLDER = "/preferences_icons/";
	
	
	public static ImageIcon getIcon(String iconPath) {
		try {
			return new ImageIcon(classInstance.getResource(iconPath));
		}
		catch(Exception e) {
			// An exception is thrown by ImageIcon if the image doesn't exist or could not be properly read
			if(com.mucommander.Debug.ON)
				com.mucommander.Debug.trace("/!\\/!\\/!\\ "+e+" caught while trying to load icon "+iconPath+", icon missing ?");
			return null;
		}
	}


	public static ImageIcon getFileIcon(String iconName) {
		return getIcon(FILE_ICONS_FOLDER+iconName);
	}


	public static ImageIcon getToolBarIcon(String iconName) {
		return getIcon(TOOLBAR_ICONS_FOLDER+iconName);
	}


	public static ImageIcon getCommandBarIcon(String iconName) {
		return getIcon(COMMANDBAR_ICONS_FOLDER+iconName);
	}


	public static ImageIcon getPreferencesIcon(String iconName) {
		return getIcon(PREFERENCES_ICONS_FOLDER+iconName);	
	}
}
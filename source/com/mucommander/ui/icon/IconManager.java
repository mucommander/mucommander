
package com.mucommander.ui.icon;

import javax.swing.ImageIcon;
import java.awt.Image;


/**
 * IconManager takes care of loading icons inside the application's JAR file and provides
 * simple methods to retrieve icon instances.
 *
 * @author Maxence Bernard
 */
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
	/** Table icons folder within the application's JAR file */	
	private final static String TABLE_ICONS_FOLDER = "/table_icons/";
	
	public final static float SCALE_100 = 1;
	public final static float SCALE_150 = 1.5f;
	public final static float SCALE_200 = 2.0f;
	
	
	/**
	 * 
	 */
	public static ImageIcon getIcon(String iconPath, float scaleFactor) {
		try {
			ImageIcon icon = new ImageIcon(classInstance.getResource(iconPath));
			
			if(scaleFactor==SCALE_100)
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
		return getIcon(iconPath, SCALE_100);
	}

	public static ImageIcon getFileIcon(String iconName) {
		return getIcon(FILE_ICONS_FOLDER+iconName, SCALE_100);
	}


	public static ImageIcon getToolBarIcon(String iconName) {
		return getIcon(TOOLBAR_ICONS_FOLDER+iconName, SCALE_200);
	}


	public static ImageIcon getCommandBarIcon(String iconName) {
		return getIcon(COMMANDBAR_ICONS_FOLDER+iconName, SCALE_200);
	}


	public static ImageIcon getPreferencesIcon(String iconName) {
		return getIcon(PREFERENCES_ICONS_FOLDER+iconName, SCALE_100);	
	}


	public static ImageIcon getTableIcon(String iconName) {
		return getIcon(TABLE_ICONS_FOLDER+iconName, SCALE_100);	
	}
}
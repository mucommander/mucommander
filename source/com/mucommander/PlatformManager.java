
package com.mucommander;

import com.mucommander.file.AbstractFile;
import java.io.IOException;

public class PlatformManager {

	// OS types
	// Windows 95, 98, Me
	private final static int WINDOWS_9X = 11;
	// Windows NT, 2000, XP and up
	private final static int WINDOWS_NT = 12;
	// Mac OS 7.x, 8.x or 9.x
	private final static int MAC_OS = 21;
	// Mac OS X and up
	private final static int MAC_OS_X = 22;
	// Other OS
	private final static int OTHER = 0;
	
	private static String osName;
	private static String osVersion;
	private static int osType;


	/**
	 * Finds out what kind of OS muCommander is running on
	 */
	static {
		osName = System.getProperty("os.name");
		osVersion = System.getProperty("os.version");
		
		// Windows family
		if (osName.startsWith("Windows")) {
			// Windows 95, 98, Me
			if (osName.startsWith("Windows 95") || osName.startsWith("Windows 98") || osName.startsWith("Windows Me"))
				osType = WINDOWS_9X;
			// Windows NT, 2000, XP and up
			else
				osType = WINDOWS_NT;
		}
		// Mac OS family
		else if (osName.startsWith("Mac OS")) {
			// Mac OS 7.x, 8.x or 9.x
			if(osVersion.startsWith("7.")
			|| osVersion.startsWith("8.")
			|| osVersion.startsWith("9."))
				osType = MAC_OS;
			// Mac OS X or up
			else		 
				osType = MAC_OS_X;
		}
		// Any other OS
		else {
			osType = OTHER;
		}
	}

	
	/**
	 * Returns <code>true</code> if the current platform is capable of opening the given URL
	 * in a new browser window.
	 */
	public static boolean canOpenURL() {
		return osName.startsWith("Mac OS") || osName.startsWith("Windows");
	}


	/**
	 * Opens/executes the given file, from the given folder.
	 */
	public static boolean open(String filePath, AbstractFile currentFolder) {
		try {
			// Here, we use exec(String[],String[],File) instead of exec(String,String[],File)
			// so we parse the tokens ourself (messes up the command otherwise)
			Process p = Runtime.getRuntime().exec(getCommandTokens(filePath), null, new java.io.File(currentFolder.getAbsolutePath()));
			return true;
		}
		catch(IOException e) {
			return false;
		}
	}


	private static String[] getCommandTokens(String filePath) {
		// Under Win32, the 'start' command opens a file with the program
		// registered with this file's extension (great!)
		// Windows 95, 98, Me : syntax is start "myfile"
		if (osType == WINDOWS_9X) {
			return new String[] {"start", "\""+filePath+"\""};
		}
		// Windows NT, 2000, XP : syntax is cmd /c start "" "myfile"
		else if (osType == WINDOWS_NT) {
			return new String[] {"cmd", "/c", "start", "\"\"", "\""+filePath+"\""};
		}
		// Mac OS X can do the same with 'open'
		else if (osType == MAC_OS_X)  {
			return new String[] {"open", filePath};
		}
		else {
			StringBuffer sb = new StringBuffer();
			char c;
			for(int i=0; i<filePath.length(); i++) {
				c = filePath.charAt(i);
				if(c==' ')
					sb.append("\\ ");
				else
					sb.append(c);
			}
			filePath = sb.toString();

			return new String[] {filePath};
		}
	}
	
	
}
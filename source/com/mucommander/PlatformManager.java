
package com.mucommander;

import com.mucommander.file.AbstractFile;

import java.io.IOException;

import java.util.Vector;

import java.awt.*;


/**
 * This class takes care of Platform-specific issues, such as getting screen dimensions
 * and issuing commands.
 *
 * @author Maxence Bernard
 */
public class PlatformManager {

	// OS types
	// Windows 95, 98, Me
	public final static int WINDOWS_9X = 11;
	// Windows NT, 2000, XP and up
	public final static int WINDOWS_NT = 12;
	// Mac OS 7.x, 8.x or 9.x
	public final static int MAC_OS = 21;
	// Mac OS X and up
	public final static int MAC_OS_X = 22;
	// Other OS
	public final static int OTHER = 0;
	
	private static String osName;
	private static String osVersion;
	private static int osType;

    
	/**
	 * Finds out what kind of OS muCommander is running on.
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
	 * Returns OS type (OS family if you wish).
	 */
	public static int getOsType() {
		return osType;
	}
	
	
	/**
	 * Returns screen insets: accurate information under Java 1.4,
	 * null inset values for Java 1.3 (except under OS X)
	 */
	private static Insets getScreenInsets(Frame frame) {
		// Code for Java 1.4
		try {
			// Java 1.4 has a method which returns real screen insets 
			return Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());		
		}
		// Code for Java 1.3
		catch(NoSuchMethodError e) {
			// Apple menu bar
			int top = getOsType()==MAC_OS_X?22:0;
			int left = 0;
			// Could add windows task bar here ?
			int bottom = 0;
			int right = 0;
			return new Insets(top, left, bottom, right);		
		}
	}
	
	
	/**
	 * Returns <code>true</code> if given coordinates are inside 'usable' screen space,
	 * taking into accounts screen insets. The result is very accurate under Java 1.4, not
	 * so accurate under Java 1.3.
	 *
	 * @param x x-coordinate, if value is below 0, this coordinate will not be used for the test
	 * @param y y-coordinate, if value is below 0, this coordinate will not be used for the test
	 */
	public static boolean isInsideUsableScreen(Frame frame, int x, int y) {
		Insets screenInsets = getScreenInsets(frame);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return (x<0 || (x >= screenInsets.left && x <screenSize.width-screenInsets.right))
			&& (y<0 || (y >= screenInsets.top && y<screenSize.height-screenInsets.bottom));
	}
	
	
	/**
	 * Returns full-screen window bounds. The result is very accurate under Java 1.4, just an
	 * estimate under Java 1.3.
	 */
	public static Rectangle getFullScreenBounds(Frame frame) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();

		// Code for Java 1.4
		try {
			// Java 1.4 makes it easy to get full screen bounds
			Insets screenInsets = toolkit.getScreenInsets(frame.getGraphicsConfiguration());		
			return new Rectangle(screenInsets.left, screenInsets.top, screenSize.width-screenInsets.left-screenInsets.right, screenSize.height-screenInsets.top-screenInsets.bottom);		
		}
		// Code for Java 1.3
		catch(NoSuchMethodError e) {
			// Sets frame to a decent size usable screen size
			int x = 0;
			int y = 0;
			int width = screenSize.width;
			int height = screenSize.height;
			
			// Mac OS X, assuming that dock is at the bottom of the screen
			if(getOsType()==MAC_OS_X) {
				// Menu bar height
				y += 22;
				height -= 22;
			}

			// Try to give enough space for 'everyone' with a 4/3 pixel ratio:
			// - for Window's task bar (ok)
			// - for Mac OS X's dock (not so sure)  
			width -= 60;
			height -= 45;
			
			return new Rectangle(x, y, width, height);		
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
	 * Executes an arbitrary command in the given folder and returns the corresponding Process object,
	 * or <code>null</code> if the command failed to execute
	 */
	public static Process execute(String command, AbstractFile currentFolder) {
		try {
            if(com.mucommander.Debug.ON)
                System.out.println("Executing "+command);

            Vector tokensV = new Vector();
			if (osType == WINDOWS_NT) {
				tokensV.add("cmd");
				tokensV.add("/c");
			}
			// Splits the command into tokens ourself, as exec() does it anyway
			command.trim();
			char c;
			int pos = 0;
			int len = command.length();
			StringBuffer token = new StringBuffer();
			while(pos<len) {
				c = command.charAt(pos);
				if((c==' ' && command.charAt(pos-1)!='\\') || c=='\t' || c=='\n' || c=='\r' || c=='\f') {
					tokensV.add(token.toString());
					token = new StringBuffer();
				}
				else if(!(c=='\\' && pos!=len-1 && command.charAt(pos+1)==' ')) {
					token.append(c);
				}
				pos ++;
			}
			tokensV.add(token.toString());

			String tokens[] = new String[tokensV.size()];
			tokensV.toArray(tokens);

			return Runtime.getRuntime().exec(tokens, null, new java.io.File(currentFolder.getAbsolutePath()));
//			return Runtime.getRuntime().exec(command, null, new java.io.File(currentFolder.getAbsolutePath()));
		}
		catch(IOException e) {
            if(com.mucommander.Debug.ON)
                System.out.println("Error while executing "+command+": "+e);
            return null;
		}
	}


	/**
	 * Opens/executes the given file, from the given folder and returns <code>true</code>
	 * if the operation succeeded.
	 */
	public static boolean open(String filePath, AbstractFile currentFolder) {
		try {
			// Here, we use exec(String[],String[],File) instead of exec(String,String[],File)
			// so we parse the tokens ourself (messes up the command otherwise)

            if(com.mucommander.Debug.ON)
                System.out.println("Opening "+filePath);

			if(currentFolder instanceof com.mucommander.file.FSFile)
				Runtime.getRuntime().exec(getOpenTokens(filePath), null, new java.io.File(currentFolder.getAbsolutePath()));
            else
				Runtime.getRuntime().exec(getOpenTokens(filePath), null);
			
            return true;
		}
		catch(IOException e) {
            if(com.mucommander.Debug.ON)
                System.out.println("Error while opening "+filePath+": "+e);
            return false;
		}
	}


	private static String[] getOpenTokens(String filePath) {
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
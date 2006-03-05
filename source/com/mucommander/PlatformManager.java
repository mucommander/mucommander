
package com.mucommander;

import com.mucommander.file.AbstractFile;

import java.io.File;

import java.util.Vector;

import java.awt.*;


/**
 * This class takes care of platform-specific issues, such as getting screen dimensions
 * and issuing commands.
 *
 * @author Maxence Bernard
 */
public class PlatformManager {

	/** OS family muCommander is running on (see constants) */
	private final static int osFamily;

	/** Java version muCommander is running on (see constants) */
	private final static int javaVersion;

	/** Unix desktop muCommander is running on (see constants), used only if OS family
	 * is LINUX, SOLARIS or OTHER */
	private static int unixDesktop;

	// OS families
	
	/** Windows 95, 98, Me */
	public final static int WINDOWS_9X = 10;
	/** Windows NT, 2000, XP and up */
	public final static int WINDOWS_NT = 11;

	/** Mac OS classic (not supported) */
	public final static int MAC_OS = 20;
	/** Mac OS X */
	public final static int MAC_OS_X = 21;

	/** Linux */
	public final static int LINUX = 30;
	
	/** Solaris */
	public final static int SOLARIS = 40;
	
	/** OS/2 */
	public final static int OS_2 = 50;

	/** Other OS */
	public final static int OTHER = 0;


	// Unix desktops

	/** Unknown desktop */
	public final static int UNKNOWN_DESKTOP = 0;

	/** KDE desktop */
	public final static int KDE_DESKTOP = 1;

	/** GNOME desktop */
	public final static int GNOME_DESKTOP = 2;


	// Java versions

	/** Java 1.0.x */
	public final static int JAVA_1_0 = 0;

	/** Java 1.1.x */
	public final static int JAVA_1_1 = 1;

	/** Java 1.2.x */
	public final static int JAVA_1_2 = 2;

	/** Java 1.3.x */
	public final static int JAVA_1_3 = 3;

	/** Java 1.4.x */
	public final static int JAVA_1_4 = 4;

	/** Java 1.5.x */
	public final static int JAVA_1_5 = 5;

	/** Java 1.6.x */
	public final static int JAVA_1_6 = 6;
	
	
	/**
	 * Finds out what kind of OS and Java version muCommander is running on.
	 */
	static {
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		
		// OS Family detection //
		
		// Windows family
		if(osName.startsWith("Windows")) {
			// Windows 95, 98, Me
			if (osName.startsWith("Windows 95") || osName.startsWith("Windows 98") || osName.startsWith("Windows Me"))
				osFamily = WINDOWS_9X;
			// Windows NT, 2000, XP and up
			else
				osFamily = WINDOWS_NT;
		}
		// Mac OS family
		else if(osName.startsWith("Mac OS")) {
			// Mac OS 7.x, 8.x or 9.x (doesn't run under Mac OS classic)
			if(osVersion.startsWith("7.")
			|| osVersion.startsWith("8.")
			|| osVersion.startsWith("9."))
				osFamily = MAC_OS;
			// Mac OS X or up
			else		 
				osFamily = MAC_OS_X;
		}
		// Linux family
		else if(osName.startsWith("Linux")) {
			osFamily = LINUX;
		}
		// Solaris family
		else if(osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
			osFamily = SOLARIS;
		}
		// OS/2 family
		else if(osName.startsWith("OS/2")) {
			osFamily = OS_2;
		}
		// Any other OS
		else {
			osFamily = OTHER;
		}

		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("detected OS family value = "+osFamily);

		// Unix desktop (KDE/GNOME) detection, only if OS is Linux, Solaris or Other (BSD)

//		if(osFamily==LINUX || osFamily==SOLARIS || osFamily==OTHER) {
		if(osFamily==MAC_OS_X) {
			unixDesktop = UNKNOWN_DESKTOP;

			// Are we running on KDE or GNOME ?
			// First, we look for typical KDE/GNOME environment variables, 
			// but we can't rely on them being defined, as they only have a value if muCommander was launched
			// with proper java -D options (-> mucommander.sh script), and will be null otherwise (-> java -jar mucommander.jar)
			String envVar;
			if((envVar=System.getProperty("KDE_FULL_SESSION"))!=null && !envVar.equals(""))
				unixDesktop = KDE_DESKTOP;
			else if((envVar=System.getProperty("GOME_DESKTOP_SESSION_ID"))!=null && !envVar.equals(""))
				unixDesktop = GNOME_DESKTOP;
			else {
				// At this point, muCommander was either not started from the shell script (null environment variables)
				// or it is simply not running on KDE or GNOME, let's figure out.
				// -> check if 'kfmclient' (KDE's equivalent of OS X's open command) or 'gnome-open' (GNOME's equivalent of OS X's open command) is available
				try {
					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("trying to execute kfmclient");
					
					// Try to execute 'kfmclient' and see if exit value is 0 (normal termination)
					if(Runtime.getRuntime().exec("kfmclient").waitFor()==0)
						unixDesktop = KDE_DESKTOP;
				} catch(Exception e) {}
			
				if(unixDesktop == UNKNOWN_DESKTOP)
					// Try to execute 'gnome-open' and see if exit value is 0 (normal termination)
					try {
						if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("trying to execute gnome-open");
						
						if(Runtime.getRuntime().exec("gnome-open").waitFor()==0)
							unixDesktop = GNOME_DESKTOP;
					} catch(Exception e) {}
			}

			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("detected desktop value = "+unixDesktop);
		}


		// Java version detection //
		String javaVersionProp = System.getProperty("java.version");

		// Java version property should never be null or empty, but better be safe than sorry ... 
		if(javaVersionProp==null || (javaVersionProp=javaVersionProp.trim()).equals("")) {
			// Assume java 1.3 (first supported Java version)
			javaVersion = JAVA_1_3;
		}
		// Java 1.6
		else if(javaVersionProp.startsWith("1.6")) {
			javaVersion = JAVA_1_6;
		}
		// Java 1.5
		else if(javaVersionProp.startsWith("1.5")) {
			javaVersion = JAVA_1_5;
		}
		// Java 1.4
		else if(javaVersionProp.startsWith("1.4")) {
			javaVersion = JAVA_1_4;
		}
		// Java 1.3
		else if(javaVersionProp.startsWith("1.3")) {
			javaVersion = JAVA_1_3;
		}
		// Java 1.2
		else if(javaVersionProp.startsWith("1.2")) {
			javaVersion = JAVA_1_2;
		}
		// Java 1.1
		else if(javaVersionProp.startsWith("1.1")) {
			javaVersion = JAVA_1_1;
		}
		// Java 1.0
		else if(javaVersionProp.startsWith("1.0")) {
			javaVersion = JAVA_1_0;
		}
		// Newer version we don't know of yet, assume latest supported Java version
		else {
			javaVersion = JAVA_1_5;
		}

		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("detected Java version value = "+javaVersion);
	}

	
	/**
	 * Returns the OS family we're currently running on (check constants for returned values).
	 */
	public static int getOSFamily() {
		return osFamily;
	}


	/**
	 * Returns the Java version we're currently running.
	 */
	public static int getJavaVersion() {
		return javaVersion;
	}


	/**
	 * Checks that muCommander's preferences folder exists, and if it doesn't tries to create it,
	 * reporting any error to the standard output.
	 * <p>This method should be called once during startup.</p>
	 */
	public static void checkCreatePreferencesFolder() {
		File prefsFolder = getPreferencesFolder();
		if(!prefsFolder.exists()) {
			if(com.mucommander.Debug.ON) System.out.println("Creating mucommander preferences folder "+prefsFolder.getAbsolutePath());
			if(!prefsFolder.mkdir())
				System.out.println("Warning: unable to create mucommander prefs folder: "+prefsFolder.getAbsolutePath());
		}
	}

	/**
	 * Returns the preferences folder, where user-specific (configuration, bookmarks...) information
	 * is stored. 
	 */
    public static File getPreferencesFolder() {
		// Mac OS X specific folder (~/Library/Preferences/)
		if(getOSFamily()==MAC_OS_X)
			return new File(System.getProperty("user.home")+"/Library/Preferences/muCommander");		
		// For all other platforms, return generic folder (~/.mucommander)
		else
			return getGenericPreferencesFolder();
    }
	
	/**
	 * Returns the 'generic' preferences folder (.mucommander directory in user home folder), 
	 * which is the same as the one returned by getPreferencesFolder() except for platforms which
	 * have a special place to store preferences files (Mac OS X for example).
	 */
	public static File getGenericPreferencesFolder() {
		return new File(System.getProperty("user.home")+"/.mucommander");		
	}
	
	
	/**
	 * Returns screen insets: accurate information under Java 1.4 and up,
	 * empty inset values for Java 1.3 (except under OS X)
	 */
	private static Insets getScreenInsets(Frame frame) {
		// Code for Java 1.4 and up
		if(PlatformManager.getJavaVersion()>=PlatformManager.JAVA_1_4) {
			// Java 1.4 has a method which returns real screen insets 
			return Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());		
		}
		// Code for Java 1.3
		else {
			// Apple menu bar
			int top = getOSFamily()==MAC_OS_X?22:0;
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
	 * Returns full-screen window bounds. The result is pretty accurate under Java 1.4 (not under Linux+Gnome though),
	 * just an estimate under Java 1.3.
	 */
	public static Rectangle getFullScreenBounds(Window window) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();

		// Code for Java 1.4 and up
		if(PlatformManager.getJavaVersion()>=PlatformManager.JAVA_1_4) {
			// Java 1.4 makes it easy to get full screen bounds
			Insets screenInsets = toolkit.getScreenInsets(window.getGraphicsConfiguration());		
			return new Rectangle(screenInsets.left, screenInsets.top, screenSize.width-screenInsets.left-screenInsets.right, screenSize.height-screenInsets.top-screenInsets.bottom);		
		}
		// Code for Java 1.3
		else {
			int x = 0;
			int y = 0;
			int width = screenSize.width;
			int height = screenSize.height;
			
			// Mac OS X, assuming that dock is at the bottom of the screen
			if(getOSFamily()==MAC_OS_X) {
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
		return osFamily==MAC_OS_X || osFamily==WINDOWS_9X || osFamily==WINDOWS_NT || unixDesktop==KDE_DESKTOP || unixDesktop==GNOME_DESKTOP;
	}


	/**
	 * Executes an arbitrary command in the given folder and returns the corresponding Process object,
	 * or <code>null</code> if the command failed to execute
	 */
	public static Process execute(String command, AbstractFile currentFolder) {
		try {
            if(Debug.ON) Debug.trace("Executing "+command);

            Vector tokensV = new Vector();
			if (osFamily == WINDOWS_NT) {
				tokensV.add("cmd");
				tokensV.add("/c");
			}
			// Split the command into tokens
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
		}
		catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while executing "+command+": "+e);
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
			// so we can parse the tokens ourself (messes up the command otherwise)

            if(Debug.ON) Debug.trace("Opening "+filePath);

            Process p;
			if(currentFolder instanceof com.mucommander.file.FSFile)
				p = Runtime.getRuntime().exec(getOpenTokens(filePath), null, new java.io.File(currentFolder.getAbsolutePath()));
            else
				p = Runtime.getRuntime().exec(getOpenTokens(filePath), null);
			
			if(Debug.ON) showProcessOutput(p);
			
            return true;
		}
		catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while opening "+filePath+": "+e);
            return false;
		}
	}


	/**
	 * Opens the given file in the Mac OS X Finder. A Finder window will be opened, revealing:
	 * <ul>
	 *  <li>if the given file is a folder, the folder contents
	 *  <li>if the given file is a regular file, the enclosing folder's contents (Finder is unable to jump to the file unfortunately)
	 * </ul>
	 */
	public static void openInFinder(AbstractFile file) {
		try {
			// Return if file is not on a local/mounted filesytem
			if(!file.getURL().getProtocol().equals("file"))
				return;

			if(!file.isDirectory())
				file = file.getParent();
			
            if(Debug.ON) Debug.trace("Opening in finder "+file.getAbsolutePath());
            	Runtime.getRuntime().exec(new String[]{"open", "-a", "Finder", file.getAbsolutePath()}, null);
		}
		catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while opening "+file.getAbsolutePath()+" in Finder: "+e);
		}
	}


	private static String[] getOpenTokens(String filePath) {
		// Under Win32, the 'start' command opens a file with the program
		// registered for this file's extension (great!)
		// Windows 95, 98, Me : syntax is start "myfile"
		String tokens[];
		if (osFamily == WINDOWS_9X) {
			tokens = new String[] {"start", "\""+filePath+"\""};
		}
		// Windows NT, 2000, XP : syntax is cmd /c start "" "myfile"
		else if (osFamily == WINDOWS_NT) {
			tokens = new String[] {"cmd", "/c", "start", "\"\"", "\""+filePath+"\""};
		}
		// Mac OS X can do the same with 'open'
		else if (osFamily == MAC_OS_X)  {
			tokens = new String[] {"open", filePath};
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

			tokens = new String[] {filePath};
		}
	
		if(Debug.ON)
			for(int i=0; i<tokens.length; i++)
				Debug.trace("token["+i+"]");
		
		return tokens;
	}

	
	private static void showProcessOutput(Process p) {
		try {
			p.waitFor();
			Debug.trace("exitValue="+p.exitValue());
	
			Debug.trace("reading process inputstream");
			int i;
			java.io.InputStream is = p.getInputStream();
			while((i=is.read())!=-1)
				System.out.print(i);
			is.close();
			
			Debug.trace("reading process errorstream");
			is = p.getErrorStream();
			while((i=is.read())!=-1)
				System.out.print(i);
			is.close();
		}
		catch(Exception e) {
		}
	}
}

package com.mucommander;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;

import com.mucommander.conf.ConfigurationManager;

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
	

    /** Environment variable used to determine if GNOME is the desktop currently running */
    private final static String GNOME_ENV_VAR = "GNOME_DESKTOP_SESSION_ID";

    /** Environment variable used to determine if KDE is the desktop currently running */
    private final static String KDE_ENV_VAR = "KDE_FULL_SESSION";
	
	
	/** Configuration variable used to store the custom shell command */
	private final static String CUSTOM_SHELL_CONF_VAR = "prefs.shell.custom_command";

	/** Configuration variable used to store the custom shell command */
	private final static String USE_CUSTOM_SHELL_CONF_VAR = "prefs.shell.use_custom";
	
	
    /**
     * Finds out what kind of OS and Java version muCommander is running on.
     */
    static {
        // Java version detection //
        String javaVersionProp = System.getProperty("java.version");

        // Java version property should never be null or empty, but better be safe than sorry ... 
        if(javaVersionProp==null || (javaVersionProp=javaVersionProp.trim()).equals("")) {
            // Assume java 1.3 (first supported Java version)
            javaVersion = JAVA_1_3;
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
		
        if(Debug.ON) Debug.trace("detected Java version value = "+javaVersion);


        // OS Family detection //

        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
		
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

        if(Debug.ON) Debug.trace("detected OS family value = "+osFamily);

        // Desktop (KDE/GNOME) detection, only if OS is Linux, Solaris or other (maybe *BSD)

        if(osFamily==LINUX || osFamily==SOLARIS || osFamily==OTHER) {
            unixDesktop = UNKNOWN_DESKTOP;

            // Are we running on KDE, GNOME or some other desktop ?
            // First, we look for typical KDE/GNOME environment variables
            // but we can't rely on them being defined, as they only have a value under Java 1.5 (using System.getenv())
            // or under Java 1.3 and 1.4 if muCommander was launched with proper java -D options (-> mucommander.sh script)

            String gnomeEnvValue;
            String kdeEnvValue;
            // System.getenv() has been deprecated and not usable (throws an exception) under Java 1.3 and 1.4,
            // let's use System.getProperty() instead
            if(javaVersion<=JAVA_1_4) {
                gnomeEnvValue = System.getProperty(GNOME_ENV_VAR);
                kdeEnvValue = System.getProperty(KDE_ENV_VAR);
            }
            // System.getenv() has been un-deprecated (reprecated?) under Java 1.5, great!
            else {
                gnomeEnvValue = System.getenv(GNOME_ENV_VAR);
                kdeEnvValue = System.getenv(KDE_ENV_VAR);
            }

            // Does the GNOME_DESKTOP_SESSION_ID environment variable have a value ?
            if(gnomeEnvValue!=null && !gnomeEnvValue.trim().equals(""))
                unixDesktop = GNOME_DESKTOP;
            // Does the KDE_FULL_SESSION environment variable have a value ?
            else if(kdeEnvValue!=null && !kdeEnvValue.trim().equals(""))
                unixDesktop = KDE_DESKTOP;
            else {
                // At this point, neither GNOME nor KDE environment variables had a value :
                // Either those variables could not be retrieved (muCommander is running on Java 1.4 or 1.3
                //  and was not started from the mucommander.sh script with the proper java -D parameters)
                // or it is simply not running on KDE or GNOME, let's give it one more try:
                // -> check if 'kfmclient' (KDE's equivalent of OS X's open command) 
                //  or 'gnome-open' (GNOME's equivalent of OS X's open command) is available

                // Since this test has a cost and GNOME seems to be more widespread than KDE, GNOME test comes first
                try {
                    if(Debug.ON) Debug.trace("trying to execute gnome-open");
					
                    // Try to execute 'gnome-open' to see if command exists (will thrown an IOException if it doesn't)
                    Runtime.getRuntime().exec("gnome-open");
                    unixDesktop = GNOME_DESKTOP;
                } catch(Exception e) {}
			
                if(unixDesktop == UNKNOWN_DESKTOP)
                    try {
                        if(Debug.ON) Debug.trace("trying to execute kfmclient");
						
                        // Try to execute 'kfmclient' to see if command exists (will thrown an IOException if it doesn't)
                        Runtime.getRuntime().exec("kfmclient");
                        unixDesktop = KDE_DESKTOP;
                    } catch(Exception e) {}
            }

            if(Debug.ON) Debug.trace("detected desktop value = "+unixDesktop);
        }
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
            if(Debug.ON) System.out.println("Creating mucommander preferences folder "+prefsFolder.getAbsolutePath());
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
	
	
	public static String getDefaultShellCommand() {
		String shellCommand;
		
		// Windows NT OSes use cmd.exe.
		if (osFamily == WINDOWS_NT) {
			shellCommand = "cmd /c";
		}
		// Windows 9X OSes use command.com.
		else if(osFamily == WINDOWS_9X) {
			shellCommand = "command.com /c";
		}
		// All other OSes are assumed to be POSIX compliant
		// and to have a /bin/sh shell.
		else {
			shellCommand = "/bin/sh -c";
		}
		
		return shellCommand;
	}
	
	
    /**
     * Executes an arbitrary command in the given folder and returns the corresponding Process object,
     * or <code>null</code> if the command failed to execute.
     */
    public static Process execute(String command, AbstractFile currentFolder) {
        try {
            if(Debug.ON) Debug.trace("Executing "+command);

			String defaultShellCommand = getDefaultShellCommand();
			String shellCommand;
			// Retrieve preferred shell command
			if(ConfigurationManager.getVariableBoolean(USE_CUSTOM_SHELL_CONF_VAR, false))
				shellCommand = ConfigurationManager.getVariable(CUSTOM_SHELL_CONF_VAR, defaultShellCommand);
			else
				shellCommand = defaultShellCommand;

			// Split the shell command into tokens
            Vector tokensV = splitCommand(shellCommand);
            // Add the command as a single token to let the shell parse it
			tokensV.add(command);

			if(Debug.ON) Debug.trace("Tokens= "+tokensV);

            // Convert the tokens Vector into a good old array
			String tokens[] = new String[tokensV.size()];
            tokensV.toArray(tokens);

            // We use Runtime.exec(String[],String[],File) instead of Runtime.exec(String,String[],File)
            // so that we can provide the tokens instead of letting Runtime.exec() parse the command and mess up 
			// the command otherwise

            // Command is run from a folder which is either :
			// - the current folder of muCommander's active panel, only if the folder is on a local filesystem
			//	(and is not an archive)
			// - user's home in all other cases (archive, remote filesystem such as SMB, FTP, ...), since the os/shell
			//	can't access those 'folders'

			return Runtime.getRuntime().exec(tokens, null, 
				new java.io.File((currentFolder instanceof FSFile)?currentFolder.getAbsolutePath():System.getProperty("user.home"))
			);
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while executing "+command+": "+e);
            return null;
        }
    }


	/**
	 * Splits the given command into an arrary of tokens.
	 */
	private static Vector splitCommand(String command) {
		char c;
		int pos = 0;
		int len = command.length();
		StringBuffer tokenSB = new StringBuffer();
		String token;
		Vector tokensV = new Vector();
		while(pos<len) {
			c = command.charAt(pos);
// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("char="+c+" ("+(int)c+")"+" pos="+pos+" len="+len+" token="+tokenSB);
			if((c==' ' && command.charAt(pos-1)!='\\') || c=='\t' || c=='\n' || c=='\r' || c=='\f') {
				token = tokenSB.toString().trim();
				if(!token.equals(""))
					tokensV.add(token.toString());
				tokenSB = new StringBuffer();
			}
			else if(!(c=='\\' && pos!=len-1 && command.charAt(pos+1)==' ')) {
				tokenSB.append(c);
			}
			
			pos ++;
		}

		token = tokenSB.toString().trim();
		if(!token.equals(""))
			tokensV.add(token.toString());

		return tokensV;
	}


    /**
     * Opens/executes the given file, from the given folder and returns <code>true</code>
     * if the operation succeeded.
     */
    public static void open(AbstractFile file) {
        if(Debug.ON) Debug.trace("Opening "+file.getAbsolutePath());

        AbstractFile currentFolder = file.getURL().getProtocol().equals("file") && (currentFolder=file.getParent())!=null?currentFolder:null;
        String filePath = file.getAbsolutePath();
        Process p = execute(getOpenTokens(filePath), currentFolder);
	
        // GNOME's 'gnome-open' command won't execute files, and we have no way to know if the given file is an exectuable file,
        // so if 'gnome-open' returned an error, we try to execute the file
        if(unixDesktop==GNOME_DESKTOP && p!=null) {
            try {
                int exitCode = p.waitFor();
                if(exitCode!=0)
                    execute(new String[]{escapeSpaceCharacters(filePath)}, currentFolder);
            } catch(Exception e) {
                if(Debug.ON) Debug.trace("Error while executing "+filePath+": "+e);
            }
        }
    }


    /**
     * Returns <code>true</code> if the current platform is capable of opening a URL in a new (default) browser window.
     */
    public static boolean canOpenURLInBrowser() {
        return osFamily==MAC_OS_X || osFamily==WINDOWS_9X || osFamily==WINDOWS_NT || unixDesktop==KDE_DESKTOP || unixDesktop==GNOME_DESKTOP;
    }

	
    /**
     * Opens the given URL in a new (default) browser window.
     *
     * <p>Not all OS/desktops are capable of doing this, {@link #canOpenURLInBrowser() canOpenURLInBrowser} 
     * should be called before to ensure the current platform can do it.
     */
    public static void openURLInBrowser(String url) {
        if(Debug.ON) Debug.trace("Opening "+url+" in a new browser window");

        String tokens[];
        if(unixDesktop == KDE_DESKTOP)
            tokens = new String[] {"kfmclient", "openURL", url};
        else
            tokens = getOpenTokens(url);
	
        execute(tokens, null);
    }


    /**
     * Returns <code>true</code> if the current platform is capable of opening a file or folder in the desktop's
     * default file manager (Finder for Mac OS X, Explorer for Windows...).
     */
    public static boolean canOpenInDesktop() {
        return osFamily==MAC_OS_X || osFamily==WINDOWS_9X || osFamily==WINDOWS_NT || unixDesktop==KDE_DESKTOP || unixDesktop==GNOME_DESKTOP;
    }	


    /**
     * Opens the given file in the currently running OS/desktop's file manager : 
     * Explorer for Windows, Finder for Mac OS X, Nautilus for GNOME, Konqueror for KDE.
     * <ul>
     *  <li>if the given file is a folder, the folder contents
     *  <li>if the given file is a regular file, the enclosing folder's contents (Finder is unable to jump to the file unfortunately)
     * </ul>
     */
    public static void openInDesktop(AbstractFile file) {
        try {
            // Return if file is not a local file
            if(!file.getURL().getProtocol().equals("file"))
                return;

            if(!file.isDirectory())
                file = file.getParent();
			
            if(Debug.ON) Debug.trace("Opening "+file.getAbsolutePath()+" in desktop");

            String filePath = file.getAbsolutePath();
            String tokens[];
            if (osFamily == MAC_OS_X)
                tokens = new String[] {"open", "-a", "Finder", filePath};
            else
                tokens = getOpenTokens(filePath);
				
            execute(tokens, null);
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while opening "+file.getAbsolutePath()+" in desktop: "+e);
        }
    }
	
	
    /**
     * Returns the name of the default file manager on the currently running OS/Desktop: 
     * "Explorer" for Windows, "Finder" for Mac OS X, "Nautilus" for GNOME, "Konqueror" for KDE, or an empty
     * String if unknown.
     */
    public static String getDefaultDesktopFMName() {
        if (osFamily==WINDOWS_9X || osFamily == WINDOWS_NT) {
            return "Explorer";
        }
        else if (osFamily == MAC_OS_X)  {
            return "Finder";
        }
        else if(unixDesktop == KDE_DESKTOP) {
            return "Konqueror";			
        }
        else if(unixDesktop == GNOME_DESKTOP) {
            return "Nautilus";
        }	
        else
            return "";
    }


    /**
     * Executes the given command tokens from the specifed current folder.
     *
     * @param tokens an array of command tokens to execute
     * @param currentFolder the folder to execute the command from, can be <code>null</code>
     */
    private static Process execute(String tokens[], AbstractFile currentFolder) {
        if(Debug.ON) Debug.trace("executing : "+tokensToString(tokens));
        try {
            Process p = Runtime.getRuntime().exec(tokens, null, currentFolder==null?null:new java.io.File(currentFolder.getAbsolutePath()));
            if(Debug.ON) showProcessOutput(p);
            return p;
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while executing "+tokensToString(tokens)+": "+e);
            return null;
        }
    }


    /**
     * Returns an array of command tokens that can be used to open the given file in the OS/Desktop's
     * default file manager.
     */
    private static String[] getOpenTokens(String filePath) {
        // Under Windows, the 'start' command opens a file with the program
        // registered for the file's extension, or executes the file if the file is executable, or opens 
        // up a new browser window if the given file is a web URL 
        // Windows 95, 98, Me : syntax is start "myfile"
        String tokens[];
        if (osFamily == WINDOWS_9X) {
            tokens = new String[] {"start", "\""+filePath+"\""};
        }
        // Windows NT, 2000, XP : syntax is cmd /c start "" "myfile"
        else if (osFamily == WINDOWS_NT) {
            tokens = new String[] {"cmd", "/c", "start", "\"\"", "\""+filePath+"\""};
        }
        // Mac OS X can do the same with the 'open' command 
        else if (osFamily == MAC_OS_X)  {
            tokens = new String[] {"open", filePath};
        }
        // KDE has 'kfmclient exec' which opens/executes a file, but it won't work with web URLs.
        // For web URLs, 'kfmclient openURL' has to be called. 
        else if(unixDesktop == KDE_DESKTOP) {
            tokens = new String[] {"kfmclient", "exec", filePath};			
        }
        // GNOME has 'gnome-open' which opens a file with a registered extension / opens a web URL in a new window,
        // but it won't execute an executable file.
        // For executable files, the file's path has to be executed as a command 
        else if(unixDesktop == GNOME_DESKTOP) {
            tokens = new String[] {"gnome-open", filePath};
        }
        // No launcher command for this platform, let's just execute the file in
        // case it's an executable
        else {
            tokens = new String[] {escapeSpaceCharacters(filePath)};
        }
	
        return tokens;
    }


    /**
     * Escapes space characters in the given string (replaces space characters ' ' instances by '\ ')
     * and returns the escaped string.
     */
    private static String escapeSpaceCharacters(String filePath) {
        StringBuffer sb = new StringBuffer();
        char c;
        int len = filePath.length();
        for(int i=0; i<len; i++) {
            c = filePath.charAt(i);
            if(c==' ')
                sb.append("\\ ");
            else
                sb.append(c);
        }
        return sb.toString();
    }


    ///////////////////
    // Debug methods //
    ///////////////////

    private static String tokensToString(String tokens[]) {
        StringBuffer sb = new StringBuffer();
        int nbTokens = tokens.length;
        for(int i=0; i<nbTokens; i++)
            sb.append(tokens[i]+" ");
			
        return sb.toString();
    }

	
    private static void showProcessOutput(Process p) {
        try {
            p.waitFor();
            Debug.trace("exitValue="+p.exitValue());
	
            Debug.trace("reading process inputstream");
            int i;
            java.io.InputStream is = p.getInputStream();
            while((i=is.read())!=-1)
                System.out.print((char)i);
            is.close();
			
            Debug.trace("reading process errorstream");
            is = p.getErrorStream();
            while((i=is.read())!=-1)
                System.out.print((char)i);
            is.close();
        }
        catch(Exception e) {
        }
    }

    public static void main(String args[]) {
        //		open(args, null);
        System.out.println("$"+args[0]+"="+System.getenv(args[0]));
    }
}

package com.mucommander;

import com.mucommander.file.AbstractFile;

import java.io.File;


/**
 * This class takes care of platform-specific issues, such as getting screen dimensions
 * and issuing commands.
 *
 * @author Maxence Bernard
 */
public class PlatformManager {
    // - Misc. constants --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Custom user agent for HTTP requests */
    public static final String USER_AGENT = RuntimeConstants.APP_STRING  + " (Java "+System.getProperty("java.vm.version")
                                            + "; " + System.getProperty("os.name") + " " +
                                            System.getProperty("os.version") + " " + System.getProperty("os.arch") + ")";



    // - OS definition ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Windows 95, 98, Me */
    public final static int WINDOWS_9X = 10;
    /** Windows NT, 2000, XP and up */
    public final static int WINDOWS_NT = 11;
    /** Mac OS classic (not supported) */
    public final static int MAC_OS     = 20;
    /** Mac OS X */
    public final static int MAC_OS_X   = 21;
    /** Linux */
    public final static int LINUX      = 30;
    /** Solaris */
    public final static int SOLARIS    = 40;
    /** OS/2 */
    public final static int OS_2       = 50;
    /** Other OS */
    public final static int OTHER      = 0;

    /** OS family muCommander is running on (see constants) */
    public final static int OS_FAMILY;



    // - Java version -----------------------------------------------------------
    // --------------------------------------------------------------------------
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

    /** Java version muCommander is running on (see constants) */
    public final static int JAVA_VERSION;



    // - Unix desktop -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Unknown desktop */
    public final static int UNKNOWN_DESKTOP = 0;
    /** KDE desktop */
    public final static int KDE_DESKTOP     = 1;
    /** GNOME desktop */
    public final static int GNOME_DESKTOP   = 2;

    /** Environment variable used to determine if GNOME is the desktop currently running */
    private final static String GNOME_ENV_VAR = "GNOME_DESKTOP_SESSION_ID";
    /** Environment variable used to determine if KDE is the desktop currently running */
    private final static String KDE_ENV_VAR = "KDE_FULL_SESSION";

    /** Unix desktop muCommander is running on (see constants), used only if OS family
     * is LINUX, SOLARIS or OTHER */
    public static final int UNIX_DESKTOP;



    // - Misc. fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Folder in which to store the preferences. */
    private static File prefFolder;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Finds out all the information it can about the system it'so currenty running.
     */
    static {
        // - Java version ----------------------------
        // -------------------------------------------
        // Java version detection //
        String javaVersion = System.getProperty("java.version");

        // Java version property should never be null or empty, but better be safe than sorry ...
        if(javaVersion==null || (javaVersion=javaVersion.trim()).equals(""))
            // Assume java 1.3 (first supported Java version)
            JAVA_VERSION = JAVA_1_3;
        // Java 1.5
        else if(javaVersion.startsWith("1.5"))
            JAVA_VERSION = JAVA_1_5;
        // Java 1.4
        else if(javaVersion.startsWith("1.4"))
            JAVA_VERSION = JAVA_1_4;
        // Java 1.3
        else if(javaVersion.startsWith("1.3"))
            JAVA_VERSION = JAVA_1_3;
        // Java 1.2
        else if(javaVersion.startsWith("1.2"))
            JAVA_VERSION = JAVA_1_2;
        // Java 1.1
        else if(javaVersion.startsWith("1.1"))
            JAVA_VERSION = JAVA_1_1;
        // Java 1.0
        else if(javaVersion.startsWith("1.0"))
            JAVA_VERSION = JAVA_1_0;
        // Newer version we don't know of yet, assume latest supported Java version
        else
            JAVA_VERSION = JAVA_1_5;

        if(Debug.ON) Debug.trace("detected Java version value = "+JAVA_VERSION);


        // - OS family -------------------------------
        // -------------------------------------------

        String osName    = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        // Windows family
        if(osName.startsWith("Windows")) {
            // Windows 95, 98, Me
            if (osName.startsWith("Windows 95") || osName.startsWith("Windows 98") || osName.startsWith("Windows Me"))
                OS_FAMILY = WINDOWS_9X;
            // Windows NT, 2000, XP and up
            else
                OS_FAMILY = WINDOWS_NT;
        }
        // Mac OS family
        else if(osName.startsWith("Mac OS")) {
            // Mac OS 7.x, 8.x or 9.x (doesn't run under Mac OS classic)
            if(osVersion.startsWith("7.")
               || osVersion.startsWith("8.")
               || osVersion.startsWith("9."))
                OS_FAMILY = MAC_OS;
            // Mac OS X or up
            else
                OS_FAMILY = MAC_OS_X;
        }
        // Linux family
        else if(osName.startsWith("Linux")) {
            OS_FAMILY = LINUX;
        }
        // Solaris family
        else if(osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            OS_FAMILY = SOLARIS;
        }
        // OS/2 family
        else if(osName.startsWith("OS/2")) {
            OS_FAMILY = OS_2;
        }
        // Any other OS
        else {
            OS_FAMILY = OTHER;
        }

        if(Debug.ON) Debug.trace("detected OS family value = "+OS_FAMILY);


        // - Unix desktop ----------------------------
        // -------------------------------------------
        // Desktop (KDE/GNOME) detection, only if OS is Linux, Solaris or other (maybe *BSD)

        if(OS_FAMILY==LINUX || OS_FAMILY==SOLARIS || OS_FAMILY==OTHER) {
            // Are we running on KDE, GNOME or some other desktop ?
            // First, we look for typical KDE/GNOME environment variables
            // but we can't rely on them being defined, as they only have a value under Java 1.5 (using System.getenv())
            // or under Java 1.3 and 1.4 if muCommander was launched with proper java -D options (-> mucommander.sh script)

            String gnomeEnvValue;
            String kdeEnvValue;
            // System.getenv() has been deprecated and not usable (throws an exception) under Java 1.3 and 1.4,
            // let's use System.getProperty() instead
            if(JAVA_VERSION<=JAVA_1_4) {
                gnomeEnvValue = System.getProperty(GNOME_ENV_VAR);
                kdeEnvValue   = System.getProperty(KDE_ENV_VAR);
            }
            // System.getenv() has been un-deprecated (reprecated?) under Java 1.5, great!
            else {
                gnomeEnvValue = System.getenv(GNOME_ENV_VAR);
                kdeEnvValue   = System.getenv(KDE_ENV_VAR);
            }

            // Does the GNOME_DESKTOP_SESSION_ID environment variable have a value ?
            if(gnomeEnvValue!=null && !gnomeEnvValue.trim().equals(""))
                UNIX_DESKTOP = GNOME_DESKTOP;
            // Does the KDE_FULL_SESSION environment variable have a value ?
            else if(kdeEnvValue!=null && !kdeEnvValue.trim().equals(""))
                UNIX_DESKTOP = KDE_DESKTOP;
            else {
                // At this point, neither GNOME nor KDE environment variables had a value :
                // Either those variables could not be retrieved (muCommander is running on Java 1.4 or 1.3
                //  and was not started from the mucommander.sh script with the proper java -D parameters)
                // or it is simply not running on KDE or GNOME, let's give it one more try:
                // -> check if 'kfmclient' (KDE's equivalent of OS X's open command)
                //  or 'gnome-open' (GNOME's equivalent of OS X's open command) is available

                // Since this test has a cost and GNOME seems to be more widespread than KDE, GNOME test comes first
                if(couldExec("gnome-open"))
                    UNIX_DESKTOP = GNOME_DESKTOP;
                else if(couldExec("kfmclient"))
                    UNIX_DESKTOP = KDE_DESKTOP;
                else
                    UNIX_DESKTOP = UNKNOWN_DESKTOP;
            }

            if(Debug.ON) Debug.trace("detected desktop value = "+UNIX_DESKTOP);
        }
        else
            UNIX_DESKTOP = UNKNOWN_DESKTOP;
    }


    /**
     * Convenience method which returns true if the current OS is Windows-based,
     * that is if the OS family is either {@link WINDOWS_9X} or {@link WINDOWS_NT}.
     */
    public static boolean isWindowsFamily() {
        return OS_FAMILY==WINDOWS_9X || OS_FAMILY==WINDOWS_NT;
    }

    /**
     * Returns true if the specified command could be executed.
     * @param  cmd command to execute.
     * @return     <code>true</code> if executing the specified command didn't trigger an Exception.
     */
    private static final boolean couldExec(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
            return true;
        }
        catch(Exception e) {return false;}
    }



    // - Shell management -------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the default shell command of the current platform.
     * <p>
     * At the time of writing, this means:<br/>
     * - <code>cmd /c</code> for the Windows NT family of operating systems.<br/>
     * - <code>command.com /c</code> for the Windows 9X family of operating systems.<br/>
     * - <code>/bin/sh -l -c</code> for any other OS.<br/>
     * </p>
     * <p>
     * Please note that this method is not returning the shell <b>binary</b>, but the shell
     * <b>command</b>. Due to some persistant bugs on some platforms, it's not always possible
     * to open a shell and write to its standard input. muCommander chooses to run commands
     * as shell scripts to work around that problem, and thus needs the full <i>run command under</i>
     * shell command.
     * </p>
     * @return the default shell command of the current platform.
     */
    public static String getDefaultShellCommand() {
        switch(OS_FAMILY) {
            // NT systems use cmd.exe
        case WINDOWS_NT:
            return "cmd /c";
            // Win9x systems use command.com
        case WINDOWS_9X:
            return "command.com /c";
            // Any other OS is assumed to be POSIX compliant,
            // and thus have a valid /bin/sh shell.
        default:
            return "/bin/sh -l -c";
        }
    }



    // - System browser ---------------------------------------------------------
    // --------------------------------------------------------------------------


    /**
     * Returns <code>true</code> if the current platform is capable of opening a URL in a new (default) browser window.
     */
    public static boolean canOpenURLInBrowser() {
        return OS_FAMILY==MAC_OS_X || OS_FAMILY==WINDOWS_9X || OS_FAMILY==WINDOWS_NT || UNIX_DESKTOP==KDE_DESKTOP || UNIX_DESKTOP==GNOME_DESKTOP;
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
        if(UNIX_DESKTOP == KDE_DESKTOP)
            tokens = new String[] {"kfmclient", "openURL", url};
        else
            tokens = getOpenTokens(url);
	
        execute(tokens, null);
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
        if(UNIX_DESKTOP==GNOME_DESKTOP && p!=null) {
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
     * Returns <code>true</code> if the current platform is capable of opening a file or folder in the desktop's
     * default file manager (Finder for Mac OS X, Explorer for Windows...).
     */
    public static boolean canOpenInDesktop() {
        return OS_FAMILY==MAC_OS_X || OS_FAMILY==WINDOWS_9X || OS_FAMILY==WINDOWS_NT || UNIX_DESKTOP==KDE_DESKTOP || UNIX_DESKTOP==GNOME_DESKTOP;
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
            if (OS_FAMILY == MAC_OS_X)
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
        if (OS_FAMILY==WINDOWS_9X || OS_FAMILY == WINDOWS_NT) {
            return "Explorer";
        }
        else if (OS_FAMILY == MAC_OS_X)  {
            return "Finder";
        }
        else if(UNIX_DESKTOP == KDE_DESKTOP) {
            return "Konqueror";			
        }
        else if(UNIX_DESKTOP == GNOME_DESKTOP) {
            return "Nautilus";
        }	
        else
            return "";
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
        if (OS_FAMILY == WINDOWS_9X) {
            tokens = new String[] {"start", "\""+filePath+"\""};
        }
        // Windows NT, 2000, XP : syntax is cmd /c start "" "myfile"
        else if (OS_FAMILY == WINDOWS_NT) {
            tokens = new String[] {"cmd", "/c", "start", "\"\"", "\""+filePath+"\""};
        }
        // Mac OS X can do the same with the 'open' command 
        else if (OS_FAMILY == MAC_OS_X)  {
            tokens = new String[] {"open", filePath};
        }
        // KDE has 'kfmclient exec' which opens/executes a file, but it won't work with web URLs.
        // For web URLs, 'kfmclient openURL' has to be called. 
        else if(UNIX_DESKTOP == KDE_DESKTOP) {
            tokens = new String[] {"kfmclient", "exec", filePath};			
        }
        // GNOME has 'gnome-open' which opens a file with a registered extension / opens a web URL in a new window,
        // but it won't execute an executable file.
        // For executable files, the file's path has to be executed as a command 
        else if(UNIX_DESKTOP == GNOME_DESKTOP) {
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



    // - Misc. ------------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the path to the default muCommander preferences folder.
     * @return the path to the default muCommander preferences folder.
     */
    public static File getDefaultPreferencesFolder() {
        File folder;

        // Mac OS X specific folder (~/Library/Preferences/muCommander)
        if(OS_FAMILY==MAC_OS_X)
            folder = new File(System.getProperty("user.home")+"/Library/Preferences/muCommander");
        // For all other platforms, use generic folder (~/.mucommander)
        else
            folder = new File(System.getProperty("user.home"), "/.mucommander");

        // Makes sure the folder exists.
        if(!folder.exists())
            if(!folder.mkdir())
                if(Debug.ON)
                    Debug.trace("Could not create preference folder: " + folder.getAbsolutePath());

        return folder;
    }

    /**
     * Returns the path to the folder that contains all the user specific data (configuration,
     * bookmarks, ...).
     * <p>
     * If the folder does not exist, this method will try to create it.
     * </p>
     * @return the path to the user's preference folder.
     */
    public static File getPreferencesFolder() {
        // If the preferences folder has been set, use 
        if(prefFolder != null)
            return prefFolder;

        return getDefaultPreferencesFolder();
    }

    /**
     * Sets the path to the folder in which muCommander will look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead.
     * </p>
     * @param     folder                   path to the folder in which muCommander will look for its preferences.
     * @exception IllegalArgumentException thrown if <code>folder</code> is not a valid folder path.
     */
    public static void setPreferencesFolder(File folder) {
        // Makes sure we get the canonical path
        // (for 'dirty hacks' such as ./mucommander.sh/../.mucommander)
        try {folder = folder.getCanonicalFile();}
        catch(Exception e) {throw new IllegalArgumentException(e);}

        // Makes sure the specified folder exists and is valid.
        if(!folder.isDirectory()) {
            if(folder.exists())
                folder = folder.getParentFile();
            else if(!folder.mkdirs())
                throw new IllegalArgumentException("Could not create folder " + folder);
        }
        else
            prefFolder = folder;
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
            if(Debug.ON) Debug.trace("exitValue="+p.exitValue());
	
            if(Debug.ON) Debug.trace("reading process inputstream");
            int i;
            java.io.InputStream is = p.getInputStream();
            while((i=is.read())!=-1)
                System.out.print((char)i);
            is.close();
			
            if(Debug.ON) Debug.trace("reading process errorstream");
            is = p.getErrorStream();
            while((i=is.read())!=-1)
                System.out.print((char)i);
            is.close();
        }
        catch(Exception e) {
        }
    }
}

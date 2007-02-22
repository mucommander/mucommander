package com.mucommander;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.process.*;
import com.mucommander.command.*;

import java.io.File;
import java.awt.event.MouseEvent;


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
    /** Java 1.0.x (not supported). */
    public final static int JAVA_1_0 = 0;
    /** Java 1.1.x (not supported). */
    public final static int JAVA_1_1 = 1;
    /** Java 1.2.x (not supported). */
    public final static int JAVA_1_2 = 2;
    /** Java 1.3.x (not supported). */
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
    public final static int UNKNOWN_DESKTOP   = 0;
    /** KDE desktop */
    public final static int KDE_DESKTOP       = 1;
    /** GNOME desktop */
    public final static int GNOME_DESKTOP     = 2;

    /** Environment variable used to determine if GNOME is the desktop currently running */
    private final static String GNOME_ENV_VAR = "GNOME_DESKTOP_SESSION_ID";
    /** Environment variable used to determine if KDE is the desktop currently running */
    private final static String KDE_ENV_VAR   = "KDE_FULL_SESSION";

    /** Unix desktop muCommander is running on, used only if OS family is LINUX, SOLARIS or OTHER */
    public static final int UNIX_DESKTOP;



    // - Default commands -------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Windows file manager name. */
    private static final String WINDOWS_FILE_MANAGER_NAME  = "Explorer";
    /** MAC OS X file manager name. */
    private static final String MAC_OS_X_FILE_MANAGER_NAME = "Finder";
    /** KDE file manager name. */
    private static final String KDE_FILE_MANAGER_NAME      = "Konqueror";
    /** Gnome file manager name. */
    private static final String GNOME_FILE_MANAGER_NAME    = "Nautilus";
    /** File opener for Windows 9x OSes. */
    private static final String WINDOWS_9X_FILE_OPENER     = "start \"$f\"";
    /** File opener for Windows NT OSes. */
    private static final String WINDOWS_NT_FILE_OPENER     = "cmd /c start \"\" \"$f\"";
    /** File opener for MAC OS X OSes. */
    private static final String MAC_OS_X_FILE_OPENER       = "open $f";
    /** File opener for KDE. */
    private static final String KDE_FILE_OPENER            = "kfmclient exec $f";
    /** File opener for Gnome. */
    private static final String GNOME_FILE_OPENER          = "gnome-open $f";
    /** File manager command for MAC OS X OSes. */
    private static final String MAC_OS_X_FILE_MANAGER      = "open -a Finder $f";
    /** URL opener command for KDE. */
    private static final String KDE_URL_OPENER             = "kmfclient openURL $f";
    /** Default Windows 9x shell. */
    private static final String WINDOWS_9X_SHELL           = "command.com /c";
    /** Default Windows NT shell. */
    private static final String WINDOWS_NT_SHELL           = "cmd /c";
    /** Default shell for non-windows OSes. */
    private static final String DEFAULT_SHELL              = "/bin/sh -l -c";
    /** Alias for the default system file opener. */
    private static final String DEFAULT_FILE_OPENER_ALIAS = "open";
    /** Alias for the default system URL opener. */
    private static final String DEFAULT_URL_OPENER_ALIAS  = "openURL";
    /** Alias for the system file manager. */
    private static final String FILE_MANAGER_ALIAS        = "file_manager";
    /** Alias for the default system executable file opener. */
    private static final String DEFAULT_EXE_OPENER_ALIAS  = "openEXE";



    // - Default association regexps --------------------------------------------
    // --------------------------------------------------------------------------
    /** Regular expression matching everything. */
    private static final String ALL_FILES_REGEXP           = ".*";
    /** Regular expression matching URLs. */
    private static final String URL_REGEXP                 = "^https?:\\/\\/.+";
    /** Regular expression that tries to match POSIX executable files. */
    private static final String POSIX_EXE_REGEXP           = "[^.]+";
    /** Regular expression that tries to match Windows executable files. */
    private static final String WINDOWS_EXE_REGEXP         = ".*\\.[eE][xX][eE]";



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
            // Assume java 1.4 (first supported Java version)
            JAVA_VERSION = JAVA_1_4;
        // Java 1.6
        else if(javaVersion.startsWith("1.6"))
            JAVA_VERSION = JAVA_1_6;
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
            JAVA_VERSION = JAVA_1_6;

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
     * that is if the OS family is either {@link #WINDOWS_9X} or {@link #WINDOWS_NT}.
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



    // - Default commands and associations --------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Registers default system dependent commands.
     * <p>
     * There's no good reason to call this method, it's only meant for the {@link com.mucommander.command.CommandManager}.
     * </p>
     */
    public static void registerDefaultCommands() {
        try {
            // Registers windows 9x specific commands.
            if(OS_FAMILY == WINDOWS_9X) {
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_FILE_OPENER_ALIAS, WINDOWS_9X_FILE_OPENER, Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(FILE_MANAGER_ALIAS, WINDOWS_9X_FILE_OPENER, Command.INVISIBLE_COMMAND, WINDOWS_FILE_MANAGER_NAME));
            }

            // Registers windows NT specific commands.
            else if(OS_FAMILY == WINDOWS_NT) {
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_EXE_OPENER_ALIAS,  "cmd /c $f", Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_FILE_OPENER_ALIAS, WINDOWS_NT_FILE_OPENER, Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(FILE_MANAGER_ALIAS, WINDOWS_NT_FILE_OPENER, Command.INVISIBLE_COMMAND, WINDOWS_FILE_MANAGER_NAME));
            }

            // Registers Mac OS X specific commands.
            else if(OS_FAMILY == MAC_OS_X) {
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_FILE_OPENER_ALIAS,  MAC_OS_X_FILE_OPENER, Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(FILE_MANAGER_ALIAS, MAC_OS_X_FILE_MANAGER, Command.INVISIBLE_COMMAND, MAC_OS_X_FILE_MANAGER_NAME));
            }

            // Registers KDE specific commands.
            else if(UNIX_DESKTOP == KDE_DESKTOP) {
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_FILE_OPENER_ALIAS, KDE_FILE_OPENER, Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_URL_OPENER_ALIAS,  KDE_URL_OPENER, Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(FILE_MANAGER_ALIAS,     KDE_FILE_OPENER, Command.INVISIBLE_COMMAND, KDE_FILE_MANAGER_NAME));
            }

            // Registers Gnome specific commands.
            else if(UNIX_DESKTOP == GNOME_DESKTOP) {
                CommandManager.registerCommand(CommandParser.getCommand(DEFAULT_FILE_OPENER_ALIAS, GNOME_FILE_OPENER, Command.SYSTEM_COMMAND));
                CommandManager.registerCommand(CommandParser.getCommand(FILE_MANAGER_ALIAS,   GNOME_FILE_OPENER, Command.INVISIBLE_COMMAND, GNOME_FILE_MANAGER_NAME));
                CommandManager.registerCommand(CommandManager.RUN_AS_EXECUTABLE_COMMAND);
            }

            // Unknown systems, the only command we know is 'run as executable'.
            else
                CommandManager.registerCommand(CommandManager.RUN_AS_EXECUTABLE_COMMAND);
        }
        catch(Exception e) {if(Debug.ON) Debug.trace("Couldn't register default commands: " + e);}
    }

    /**
     * Registers default system dependent associations.
     * <p>
     * There's no good reason to call this method, it's only meant for the {@link com.mucommander.command.CommandManager}.
     * </p>
     */
    public static void registerDefaultAssociations() {
        try {
            // The only required association under Windows 9x is 'file opener'.
            if(OS_FAMILY == WINDOWS_9X)
                CommandManager.registerAssociation(ALL_FILES_REGEXP, DEFAULT_FILE_OPENER_ALIAS);

            // Under Windows NT, the recommanded way of opening executable files is through cmd /c.
            // All other files are opened with cmd /c start "" "$f"
            else if(OS_FAMILY == WINDOWS_NT) {
                CommandManager.registerAssociation(WINDOWS_EXE_REGEXP, DEFAULT_EXE_OPENER_ALIAS);
                CommandManager.registerAssociation(ALL_FILES_REGEXP, DEFAULT_FILE_OPENER_ALIAS);
            }

            // Registers Mac OS X specific associations.
            else if(OS_FAMILY == MAC_OS_X)
                CommandManager.registerAssociation(ALL_FILES_REGEXP, DEFAULT_FILE_OPENER_ALIAS);

            // Registers KDE specific associations.
            else if(UNIX_DESKTOP == KDE_DESKTOP) {
                // kmfclient doesn't know how to open URLs. We need to add a specific URL matching
                // association for kmfclient openURL
                CommandManager.registerAssociation(URL_REGEXP, DEFAULT_URL_OPENER_ALIAS);
                CommandManager.registerAssociation(ALL_FILES_REGEXP, DEFAULT_FILE_OPENER_ALIAS);
            }

            // Registers Gnome specific associations.
            else if(UNIX_DESKTOP == GNOME_DESKTOP) {
                // If we're running under a version of Java that doesn't support the file system's
                // 'executable' flag, we'll try to match files that look like they might just be executable.
                if(JAVA_VERSION <= JAVA_1_5)
                    CommandManager.registerAssociation(POSIX_EXE_REGEXP, CommandManager.RUN_AS_EXECUTABLE_ALIAS);

                // Order is important here: 'all executable files' must be matched before 'all files'.
                CommandManager.registerAssociation(ALL_FILES_REGEXP,
                                                   CommandAssociation.UNFILTERED, CommandAssociation.UNFILTERED,
                                                   CommandAssociation.YES, CommandManager.RUN_AS_EXECUTABLE_ALIAS);
                CommandManager.registerAssociation(ALL_FILES_REGEXP, DEFAULT_FILE_OPENER_ALIAS);
            }

            // Unknown systems, the only association we can make is
            // 'if it looks like an executable, try to open it as an executable'.
            else {
                // If we're running under a version of Java that doesn't support the file system's
                // 'executable' flag, we'll try to match files that look like they might be executable.
                if(JAVA_VERSION <= JAVA_1_5)
                    CommandManager.registerAssociation(POSIX_EXE_REGEXP, CommandManager.RUN_AS_EXECUTABLE_ALIAS);
                CommandManager.registerAssociation(ALL_FILES_REGEXP,
                                                   CommandAssociation.UNFILTERED, CommandAssociation.UNFILTERED,
                                                   CommandAssociation.YES, CommandManager.RUN_AS_EXECUTABLE_ALIAS);
            }

        }
        catch(Exception e) {if(Debug.ON) Debug.trace("Couldn't register default associations: " + e);}
    }



    // - Platform specific commands ---------------------------------------------
    /**
     * Returns the current OS' default shell command.
     * @return the current OS' default shell command.
     */
    // --------------------------------------------------------------------------
    public static String getDefaultShellCommand() {
        switch(OS_FAMILY) {
            // NT systems use cmd.exe
        case WINDOWS_NT:
            return WINDOWS_NT_SHELL;
            // Win9x systems use command.com
        case WINDOWS_9X:
            return WINDOWS_9X_SHELL;
            // Any other OS is assumed to be POSIX compliant,
            // and thus have a valid /bin/sh shell.
        default:
            return DEFAULT_SHELL;
        }
    }

    /**
     * Returns the current OS' default file manager name.
     * <p>
     * This method will only work properly if we're under a known system. This can be checked
     * through {@link #canOpenInDesktop()}.
     * </p>
     * @return the current OS' default file manager name, <code>null</code> if not found.
     */
    public static String getDefaultDesktopFMName() {
        if(OS_FAMILY==WINDOWS_9X || OS_FAMILY == WINDOWS_NT)
            return WINDOWS_FILE_MANAGER_NAME;
        else if(OS_FAMILY == MAC_OS_X)
            return MAC_OS_X_FILE_MANAGER_NAME;
        else if(UNIX_DESKTOP == KDE_DESKTOP)
            return KDE_FILE_MANAGER_NAME;
        else if(UNIX_DESKTOP == GNOME_DESKTOP)
            return GNOME_FILE_MANAGER_NAME;
        else
            return null;
    }

    /**
     * Returns <code>true</code> if the current platform is capable of opening a URL in a new browser window.
     * <p>
     * This test is done by checking whether muCommander's homepage is associated to any command. If it's not, then we'll
     * assume that no URL can be opened in the current configuration.
     * </p>
     * @return <code>true</code> if the current platform is capable of opening a URL in a new browser window.
     */
    public static boolean canOpenURLInBrowser() {return CommandManager.getCommandForFile(FileFactory.getFile(RuntimeConstants.HOMEPAGE_URL), false) != null;}

    /**
     * Attempts to open the specified file through registered command associations.
     * @param  file file to open.
     * @return      the process in which the file was opened, or <code>null</code> if an error occured.
     */
    public static AbstractProcess open(AbstractFile file) {
        if(Debug.ON) Debug.trace("Opening " + file.getAbsolutePath());

        try {return ProcessRunner.execute(CommandManager.getTokensForFile(file), file);}
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while executing " + file + ": " + e);
            return null;
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
     * Tries to open the specified file in the system's file manager.
     * <p>
     * Developers should make sure the operation is possible before calling this.
     * This can be done through {@link #canOpenInDesktop()}.
     * </p>
     */
    public static void openInDesktop(AbstractFile file) {
        try {
            // Return if file is not a local file
            if(!file.getURL().getProtocol().equals(FileProtocols.FILE))
                return;

            if(!file.isDirectory())
                file = file.getParent();

            Command command;
            if((command = CommandManager.getCommandForAlias(FILE_MANAGER_ALIAS)) != null) {
                if(Debug.ON) Debug.trace("Opening "+file.getAbsolutePath()+" in desktop");
                ProcessRunner.execute(command.getTokens(file), file);
            }
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error while opening "+file.getAbsolutePath()+" in desktop: "+e);
        }
    }



    // - Mouse handling. --------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns true if the given MouseEvent corresponds to the left mouse button, taking into account the Mac OS X
     * specificity where a left-click with the control key down is equivalent to a right-click (false is returned
     * in that case).
     *
     * @param e the MouseEvent to test
     * @return true if the given MouseEvent corresponds to the left mouse button, false otherwise
     */
    public static boolean isLeftMouseButton(MouseEvent e) {
        int modifiers = e.getModifiers();
        return (modifiers & MouseEvent.BUTTON1_MASK)!=0 && !(OS_FAMILY==MAC_OS_X && e.isControlDown());
    }

    /**
     * Returns true if the given MouseEvent corresponds to the right mouse button, taking into account the Mac OS X
     * specificity where a left-click with the control key down is equivalent to a right-click (true is returned
     * in that case).
     *
     * @param e the MouseEvent to test
     * @return true if the given MouseEvent corresponds to the right mouse button, false otherwise
     */
    public static boolean isRightMouseButton(MouseEvent e) {
        int modifiers = e.getModifiers();
        return (modifiers & MouseEvent.BUTTON3_MASK)!=0 || (OS_FAMILY==MAC_OS_X && (modifiers & MouseEvent.BUTTON1_MASK)!=0 && e.isControlDown());
    }

    /**
     * Returns true if the given MouseEvent corresponds to the middle mouse button.
     *
     * @param e the MouseEvent to test
     * @return true if the given MouseEvent corresponds to the middle mouse button, false otherwise
     */
    public static boolean isMiddleMouseButton(MouseEvent e) {
        return (e.getModifiers() & MouseEvent.BUTTON2_MASK)!=0;
    }



    // - Preferences folder -----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the path to the default muCommander preferences folder.
     * <p>
     * This folder is:
     * <ul>
     *  <li><code>~/Library/Preferences/muCommander/</code> under MAC OS X.</li>
     *  <li><code>~/.mucommander/</code> under all other OSes.</li>
     * </ul>
     * </p>
     * <p>
     * If the default preferences folder doesn't exist, this method will create it.
     * </p>
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
     * Returns the path to the folder that contains all of the user's data.
     * <p>
     * All modules that save user data to a file should do so in a file located in
     * the folder returned by this method.
     * </p>
     * <p>
     * The value returned by this method can be set through {@link #setPreferencesFolder(File)}.
     * Otherwise, the {@link #getDefaultPreferencesFolder() default preference folder} will be
     * used.
     * </p>
     * @return the path to the user's preference folder.
     */
    public static File getPreferencesFolder() {
        // If the preferences folder has been set, use it.
        if(prefFolder != null)
            return prefFolder;

        return getDefaultPreferencesFolder();
    }

    /**
     * Sets the path to the folder in which muCommander will look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param     folder                   path to the folder in which muCommander will look for its preferences.
     * @exception IllegalArgumentException thrown if <code>folder</code> is not a valid folder path.
     */
    public static void setPreferencesFolder(File folder) throws IllegalArgumentException {
        // Makes sure we get the canonical path
        // (for 'dirty hacks' such as ./mucommander.sh/../.mucommander)
        try {folder = folder.getCanonicalFile();}
        catch(Exception e) {throw new IllegalArgumentException(e);}

        // Makes sure the specified folder exists and is valid.
        if(!folder.isDirectory()) {
            if(folder.exists())
                folder = folder.getParentFile();
            else if(!folder.mkdirs()) {
                if(Debug.ON) Debug.trace("Could not create preferences directory: " + folder);
                throw new IllegalArgumentException("Could not create folder " + folder);
            }
        }
        prefFolder = folder;
    }
}

package com.mucommander;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.process.ProcessRunner;
import com.mucommander.process.AbstractProcess;
import com.mucommander.command.CommandManager;
import com.mucommander.command.Command;

import java.io.File;
import java.awt.event.MouseEvent;


/**
 * This class takes care of platform-specific issues, such as getting screen dimensions
 * and issuing commands.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
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
    public static final int WINDOWS_9X = 10;
    /** Windows NT, 2000, XP and up */
    public static final int WINDOWS_NT = 11;
    /** Mac OS classic (not supported) */
    public static final int MAC_OS     = 20;
    /** Mac OS X */
    public static final int MAC_OS_X   = 21;
    /** Linux */
    public static final int LINUX      = 30;
    /** Solaris */
    public static final int SOLARIS    = 40;
    /** OS/2 */
    public static final int OS_2       = 50;
    /** Other OS */
    public static final int OTHER      = 0;
    /** OS family muCommander is running on (see constants) */
    public static final int OS_FAMILY;



    // - Java version -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Java 1.0.x (not supported). */
    public static final int JAVA_1_0 = 0;
    /** Java 1.1.x (not supported). */
    public static final int JAVA_1_1 = 1;
    /** Java 1.2.x (not supported). */
    public static final int JAVA_1_2 = 2;
    /** Java 1.3.x (not supported). */
    public static final int JAVA_1_3 = 3;
    /** Java 1.4.x */
    public static final int JAVA_1_4 = 4;
    /** Java 1.5.x */
    public static final int JAVA_1_5 = 5;
    /** Java 1.6.x */
    public static final int JAVA_1_6 = 6;
    /** Java version muCommander is running on (see constants) */
    public static final int JAVA_VERSION;



    // - Unix desktop -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Unknown desktop */
    public static final int UNKNOWN_DESKTOP   = 0;
    /** KDE desktop */
    public static final int KDE_DESKTOP       = 1;
    /** GNOME desktop */
    public static final int GNOME_DESKTOP     = 2;
    /** Unix desktop muCommander is running on, used only if OS family is LINUX, SOLARIS or OTHER */
    public static final int UNIX_DESKTOP;

    /** Environment variable used to determine if GNOME is the desktop currently running */
    private static final String GNOME_ENV_VAR = "GNOME_DESKTOP_SESSION_ID";
    /** Environment variable used to determine if KDE is the desktop currently running */
    private static final String KDE_ENV_VAR   = "KDE_FULL_SESSION";



    // - Default commands -------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Name of the system's default file manager. */
    public static final String  DEFAULT_FILE_MANAGER_NAME;
    /** Command used to start the system's default file manager. */
    public static final String  DEFAULT_FILE_MANAGER_COMMAND;
    /** Command used to start the system's default file opener. */
    public static final String  DEFAULT_FILE_OPENER_COMMAND;
    /** Command used to start the system's default URL opener. */
    public static final String  DEFAULT_URL_OPENER_COMMAND;
    /** Command used to start the system's default executable file opener. */
    public static final String  DEFAULT_EXE_OPENER_COMMAND;
    /** Command used to run the system's default shell. */
    public static final String  DEFAULT_SHELL_COMMAND;



    // - Default file associations ----------------------------------------------
    // --------------------------------------------------------------------------
    /** Matches all files (should always be registered last). */
    public static final String  CATCH_ALL_ASSOCIATION;
    /** Regular expression used to match executable file names. */
    public static final String  EXE_ASSOCIATION;
    /** Whether or not the system can or needs to run executable files as themselves. */
    public static final boolean RUN_EXECUTABLES;

    /** Windows file manager name. */
    private static final String WINDOWS_FILE_MANAGER_NAME   = "Explorer";
    /** MAC OS X file manager name. */
    private static final String MAC_OS_X_FILE_MANAGER_NAME  = "Finder";
    /** KDE file manager name. */
    private static final String KDE_FILE_MANAGER_NAME       = "Konqueror";
    /** Gnome file manager name. */
    private static final String GNOME_FILE_MANAGER_NAME     = "Nautilus";
    /** File opener for Windows 9x OSes. */
    private static final String WINDOWS_9X_FILE_OPENER      = "start \"$f\"";
    /** File opener for Windows NT OSes. */
    private static final String WINDOWS_NT_FILE_OPENER      = "cmd /c start \"\" \"$f\"";
    /** Executable file opener for Windows NT OSes. */
    private static final String WINDOWS_NT_EXE_OPENER       = "cmd /c $f";
    /** Executable file opener for POSIX systems. */
    private static final String POSIX_EXE_OPENER            = "$f";
    /** File opener for MAC OS X OSes. */
    private static final String MAC_OS_X_FILE_OPENER        = "open $f";
    /** File opener for KDE. */
    private static final String KDE_FILE_OPENER             = "kfmclient exec $f";
    /** File opener for Gnome. */
    private static final String GNOME_FILE_OPENER           = "gnome-open $f";
    /** File manager command for MAC OS X OSes. */
    private static final String MAC_OS_X_FILE_MANAGER       = "open -a Finder $f";
    /** URL opener command for KDE. */
    private static final String KDE_URL_OPENER              = "kmfclient openURL $f";
    /** Default Windows 9x shell. */
    private static final String DEFAULT_WINDOWS_9X_SHELL    = "command.com /c";
    /** Default Windows NT shell. */
    private static final String DEFAULT_WINDOWS_NT_SHELL    = "cmd /c";
    /** Default shell for non-windows OSes. */
    private static final String DEFAULT_POSIX_SHELL         = "/bin/sh -l -c";



    // - Default association regexps --------------------------------------------
    // --------------------------------------------------------------------------
    /** Regular expression matching everything. */
    private static final String ALL_FILES_REGEXP           = ".*";
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
            UNIX_DESKTOP              = UNKNOWN_DESKTOP;
            DEFAULT_FILE_MANAGER_NAME = WINDOWS_FILE_MANAGER_NAME;
            CATCH_ALL_ASSOCIATION     = ALL_FILES_REGEXP;

            // Windows 95, 98, Me
            if (osName.startsWith("Windows 95") || osName.startsWith("Windows 98") || osName.startsWith("Windows Me")) {
                OS_FAMILY                    = WINDOWS_9X;
                DEFAULT_FILE_MANAGER_COMMAND = WINDOWS_9X_FILE_OPENER;
                DEFAULT_FILE_OPENER_COMMAND  = WINDOWS_9X_FILE_OPENER;
                DEFAULT_URL_OPENER_COMMAND   = WINDOWS_9X_FILE_OPENER;
                DEFAULT_EXE_OPENER_COMMAND   = null;
                EXE_ASSOCIATION              = null;
                RUN_EXECUTABLES              = false;
            }
            // Windows NT, 2000, XP and up
            else {
                OS_FAMILY                    = WINDOWS_NT;
                DEFAULT_FILE_MANAGER_COMMAND = WINDOWS_NT_FILE_OPENER;
                DEFAULT_FILE_OPENER_COMMAND  = WINDOWS_NT_FILE_OPENER;
                DEFAULT_URL_OPENER_COMMAND   = WINDOWS_NT_FILE_OPENER;
                DEFAULT_EXE_OPENER_COMMAND   = WINDOWS_NT_EXE_OPENER;
                EXE_ASSOCIATION              = WINDOWS_EXE_REGEXP;
                RUN_EXECUTABLES              = false;
            }
        }
        // Mac OS family
        else if(osName.startsWith("Mac OS")) {
            UNIX_DESKTOP = UNKNOWN_DESKTOP;

            // Mac OS 7.x, 8.x or 9.x (doesn't run under Mac OS classic)
            if(osVersion.startsWith("7.")
               || osVersion.startsWith("8.")
               || osVersion.startsWith("9.")) {
                OS_FAMILY = MAC_OS;
                DEFAULT_FILE_MANAGER_NAME    = null;
                DEFAULT_FILE_MANAGER_COMMAND = null;
                DEFAULT_FILE_OPENER_COMMAND  = null;
                DEFAULT_URL_OPENER_COMMAND   = null;
                DEFAULT_EXE_OPENER_COMMAND   = null;
                CATCH_ALL_ASSOCIATION        = null;
                EXE_ASSOCIATION              = null;
                RUN_EXECUTABLES              = false;
            }
            // Mac OS X or up
            else {
                OS_FAMILY                    = MAC_OS_X;
                DEFAULT_FILE_MANAGER_NAME    = MAC_OS_X_FILE_MANAGER_NAME;
                DEFAULT_FILE_MANAGER_COMMAND = MAC_OS_X_FILE_MANAGER;
                DEFAULT_FILE_OPENER_COMMAND  = MAC_OS_X_FILE_OPENER;
                DEFAULT_URL_OPENER_COMMAND   = MAC_OS_X_FILE_OPENER;
                DEFAULT_EXE_OPENER_COMMAND   = null;
                CATCH_ALL_ASSOCIATION        = ALL_FILES_REGEXP;
                EXE_ASSOCIATION              = null;
                RUN_EXECUTABLES              = false;
            }
        }
        // OS/2 family.
        else if(osName.startsWith("OS/2")) {
            OS_FAMILY                    = OS_2;
            UNIX_DESKTOP                 = UNKNOWN_DESKTOP;
            DEFAULT_FILE_MANAGER_NAME    = null;
            DEFAULT_FILE_MANAGER_COMMAND = null;
            DEFAULT_FILE_OPENER_COMMAND  = null;
            DEFAULT_URL_OPENER_COMMAND   = null;
            DEFAULT_EXE_OPENER_COMMAND   = POSIX_EXE_OPENER;
            CATCH_ALL_ASSOCIATION        = null;
            RUN_EXECUTABLES              = true;
            EXE_ASSOCIATION              = (JAVA_VERSION < JAVA_1_6) ? POSIX_EXE_REGEXP : null;
        }
        // Unix, or assimilated.
        else {
            // Linux family
            if(osName.startsWith("Linux"))
                OS_FAMILY = LINUX;
            // Solaris family
            else if(osName.startsWith("Solaris") || osName.startsWith("SunOS"))
                OS_FAMILY = SOLARIS;
            // Any other OS
            else
                OS_FAMILY = OTHER;



            // - UNIX desktop ----------------------------
            // -------------------------------------------
            // At the time of writing, muCommander is only aware of KDE and Gnome.
            // The first step in identifying either of these is to look for specific environment variables.
            // If those cannot be located, we can try and run each system's file opener - if it works, we've
            // identified which system we're running on.

            String gnomeEnvValue;
            String kdeEnvValue;

            // System.getenv() has been deprecated and not usable (throws an exception) under Java 1.3 and 1.4,
            // let's use System.getProperty() instead
            if(JAVA_VERSION <= JAVA_1_4) {
                gnomeEnvValue = System.getProperty(GNOME_ENV_VAR);
                kdeEnvValue   = System.getProperty(KDE_ENV_VAR);
            }
            // System.getenv() has been un-deprecated (reprecated?) under Java 1.5, great!
            else {
                gnomeEnvValue = System.getenv(GNOME_ENV_VAR);
                kdeEnvValue   = System.getenv(KDE_ENV_VAR);
            }

            // Tries to identify Gnome.
            if((gnomeEnvValue!=null && !gnomeEnvValue.trim().equals("")) || couldRun("gnome-open")) {
                UNIX_DESKTOP                 = GNOME_DESKTOP;
                DEFAULT_FILE_MANAGER_NAME    = GNOME_FILE_MANAGER_NAME;
                DEFAULT_FILE_MANAGER_COMMAND = GNOME_FILE_OPENER;
                DEFAULT_FILE_OPENER_COMMAND  = GNOME_FILE_OPENER;
                DEFAULT_URL_OPENER_COMMAND   = GNOME_FILE_OPENER;
                DEFAULT_EXE_OPENER_COMMAND   = POSIX_EXE_OPENER;
                CATCH_ALL_ASSOCIATION        = ALL_FILES_REGEXP;
                RUN_EXECUTABLES              = true;
                EXE_ASSOCIATION              = (JAVA_VERSION < JAVA_1_6) ? POSIX_EXE_REGEXP : null;
            }

            // Tries to identify KDE.
            else if(kdeEnvValue!=null && !kdeEnvValue.trim().equals("") || couldRun("kfmclient")) {
                UNIX_DESKTOP                 = KDE_DESKTOP;
                DEFAULT_FILE_MANAGER_NAME    = KDE_FILE_MANAGER_NAME;
                DEFAULT_FILE_MANAGER_COMMAND = KDE_FILE_OPENER;
                DEFAULT_FILE_OPENER_COMMAND  = KDE_FILE_OPENER;
                DEFAULT_URL_OPENER_COMMAND   = KDE_URL_OPENER;
                DEFAULT_EXE_OPENER_COMMAND   = null;
                CATCH_ALL_ASSOCIATION        = ALL_FILES_REGEXP;
                EXE_ASSOCIATION              = null;
                RUN_EXECUTABLES              = false;
            }

            // Absolutely no clue what we're running.
            else {
                UNIX_DESKTOP                 = UNKNOWN_DESKTOP;
                DEFAULT_FILE_MANAGER_NAME    = null;
                DEFAULT_FILE_MANAGER_COMMAND = null;
                DEFAULT_FILE_OPENER_COMMAND  = null;
                DEFAULT_URL_OPENER_COMMAND   = null;
                DEFAULT_EXE_OPENER_COMMAND   = POSIX_EXE_OPENER;
                CATCH_ALL_ASSOCIATION        = null;
                RUN_EXECUTABLES              = true;
                EXE_ASSOCIATION              = (JAVA_VERSION < JAVA_1_6) ? POSIX_EXE_REGEXP : null;
            }
        }

        // Identifies the default shell command.
        if(OS_FAMILY == WINDOWS_9X)
            DEFAULT_SHELL_COMMAND = DEFAULT_WINDOWS_9X_SHELL;
        else if(OS_FAMILY == WINDOWS_NT)
            DEFAULT_SHELL_COMMAND = DEFAULT_WINDOWS_NT_SHELL;
        else
            DEFAULT_SHELL_COMMAND = DEFAULT_POSIX_SHELL;
    }

    /**
     * Convenience method which returns true if the current OS is Windows-based,
     * that is if the OS family is either {@link #WINDOWS_9X} or {@link #WINDOWS_NT}.
     */
    public static boolean isWindowsFamily() {return OS_FAMILY == WINDOWS_9X || OS_FAMILY == WINDOWS_NT;}

    /**
     * Returns true if the specified command could be executed.
     * @param  cmd command to execute.
     * @return     <code>true</code> if executing the specified command didn't trigger an Exception.
     */
    private static final boolean couldRun(String cmd) {
        try {
            ProcessRunner.execute(cmd);
            return true;
        }
        catch(Exception e) {return false;}
    }



    // - Platform specific operations -------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the name of the registered file manager.
     * @return the name of the registered file manager.
     */
    public static String getFileManagerName() {
        Command command;

        if((command = CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS)) == null)
            return null;
        return command.getDisplayName();
    }

    /**
     * Returns <code>true</code> if the current platform is capable of opening a file or folder in the desktop's
     * default file manager (Finder for Mac OS X, Explorer for Windows...).
     */
    public static boolean canOpenInFileManager() {return CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS) != null;}	

    /**
     * Returns <code>true</code> if the current platform is capable of opening a URL in a new browser window.
     * @return <code>true</code> if the current platform is capable of opening a URL in a new browser window.
     */
    public static boolean canOpenUrl() {return CommandManager.getCommandForAlias(CommandManager.URL_OPENER_ALIAS) != null;}

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
     * Opens the specified URL in the registered web browser.
     */
    public static void openUrl(AbstractFile file) {
        try {
            String protocol;

            // Makes sure the file is a URL.
            protocol = file.getURL().getProtocol();
            if(!protocol.equals(FileProtocols.HTTP) && !protocol.equals(FileProtocols.HTTPS))
                return;

            // Opens the file with the registered URL opener.
            Command command;
            if((command = CommandManager.getCommandForAlias(CommandManager.URL_OPENER_ALIAS)) != null)
                ProcessRunner.execute(command.getTokens(file), file);
        }
        catch(Exception e) {}
    }

    /**
     * Tries to open the specified file in the system's file manager.
     * <p>
     * Developers should make sure the operation is possible before calling this.
     * This can be done through {@link #canOpenInFileManager()}.
     * </p>
     */
    public static void openInFileManager(AbstractFile file) {
        try {
            // Return if file is not a local file
            if(!file.getURL().getProtocol().equals(FileProtocols.FILE))
                return;

            if(!file.isDirectory())
                file = file.getParent();

            Command command;
            if((command = CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS)) != null) {
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

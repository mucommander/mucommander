/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessRunner;
import com.mucommander.runtime.*;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * This class takes care of platform-specific issues, such as getting screen dimensions and issuing commands.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class PlatformManager implements JavaVersions, OsFamilies, OsVersions {

    // - Misc. constants --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Custom user agent for HTTP requests. */
    public static final String USER_AGENT = RuntimeConstants.APP_STRING  + " (Java "+System.getProperty("java.vm.version")
                                            + "; " + System.getProperty("os.name") + " " +
                                            System.getProperty("os.version") + " " + System.getProperty("os.arch") + ")";

    // - Unix desktop -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Unknown desktop. */
    public static final int UNKNOWN_DESKTOP   = 0;
    /** KDE desktop. */
    public static final int KDE_DESKTOP       = 1;
    /** GNOME desktop. */
    public static final int GNOME_DESKTOP     = 2;

    /** Unix desktop muCommander is running on, used only if OS family is LINUX, SOLARIS or OTHER. */
    private static      int unixDesktop;

    /** Environment variable used to determine if GNOME is the desktop currently running. */
    private static final String GNOME_ENV_VAR = "GNOME_DESKTOP_SESSION_ID";
    /** Environment variable used to determine if KDE is the desktop currently running. */
    private static final String KDE_ENV_VAR   = "KDE_FULL_SESSION";



    // - Default commands -------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Name of the system's default file manager. */
    private static      String  defaultFileManagerName;
    /** Command used to start the system's default file manager. */
    private static      String  defaultFileManagerCommand;
    /** Command used to start the system's default file opener. */
    private static      String  defaultFileOpenerCommand;
    /** Command used to start the system's default URL opener. */
    private static      String  defaultUrlOpenerCommand;
    /** Command used to start the system's default executable file opener. */
    private static      String  defaultExeOpenerCommand;
    /** Command used to run the system's default shell. */
    private static      String  defaultShellCommand;



    // - Default file associations ----------------------------------------------
    // --------------------------------------------------------------------------
    /** Regular expression used to match executable file names. */
    private static       String  exeAssociation;
    /** Whether or not the system can or needs to run executable files as themselves. */
    private static       boolean runExecutables;
    /** Whether or not default regular expressions must be case sensitive. */
    private static       boolean defaultRegexpCaseSensitivity;

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
    private static final String KDE_URL_OPENER              = "kfmclient openURL $f";
    /** Default Windows 9x shell. */
    private static final String WINDOWS_9X_SHELL            = "command.com /c";
    /** Default Windows NT shell. */
    private static final String WINDOWS_NT_SHELL            = "cmd /c";
    /** Default shell for non-windows OSes. */
    private static final String POSIX_SHELL                 = "/bin/sh -l -c";



    // - Default association regexps --------------------------------------------
    // --------------------------------------------------------------------------
    /** Regular expression matching everything. */
    private static final String ALL_FILES_REGEXP           = ".*";
    /** Regular expression that tries to match POSIX executable files. */
    private static final String POSIX_EXE_REGEXP           = "[^.]+";
    /** Regular expression that tries to match Windows executable files. */
    private static final String WINDOWS_EXE_REGEXP         = ".*\\.exe";



    // - Misc. fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Folder in which to store the preferences. */
    private static AbstractFile prefFolder;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Finds out all the information it can about the system it'so currenty running.
     */
    static {
        OsFamily osFamily = OsFamily.getCurrent();
        OsVersion osVersion = OsVersion.getCurrent();
        JavaVersion javaVersion = JavaVersion.getCurrent();

        // Windows family
        if(osFamily==WINDOWS) {
            unixDesktop            = UNKNOWN_DESKTOP;
            defaultFileManagerName = WINDOWS_FILE_MANAGER_NAME;

            // Windows 9X: 95, 98, Me
            if (osVersion.compareTo(WINDOWS_NT)<0) {
                defaultFileManagerCommand    = WINDOWS_9X_FILE_OPENER;
                defaultFileOpenerCommand     = WINDOWS_9X_FILE_OPENER;
                defaultUrlOpenerCommand      = WINDOWS_9X_FILE_OPENER;
                defaultExeOpenerCommand      = null;
                exeAssociation               = null;
                runExecutables               = false;
                defaultRegexpCaseSensitivity = false;
            }
            // Windows NT: NT, 2000, XP, 2003, Vista and up
            else {
                defaultFileManagerCommand    = WINDOWS_NT_FILE_OPENER;
                defaultFileOpenerCommand     = WINDOWS_NT_FILE_OPENER;
                defaultUrlOpenerCommand      = WINDOWS_NT_FILE_OPENER;
                defaultExeOpenerCommand      = WINDOWS_NT_EXE_OPENER;
                exeAssociation               = WINDOWS_EXE_REGEXP;
                runExecutables               = false;
                defaultRegexpCaseSensitivity = false;
            }
        }
        // Mac OS X family
        else if(osFamily==MAC_OS_X) {
            unixDesktop = UNKNOWN_DESKTOP;

            defaultFileManagerName       = MAC_OS_X_FILE_MANAGER_NAME;
            defaultFileManagerCommand    = MAC_OS_X_FILE_MANAGER;
            defaultFileOpenerCommand     = MAC_OS_X_FILE_OPENER;
            defaultUrlOpenerCommand      = MAC_OS_X_FILE_OPENER;
            defaultExeOpenerCommand      = null;
            exeAssociation               = null;
            runExecutables               = false;
            defaultRegexpCaseSensitivity = true;
            com.mucommander.file.FileFactory.setTrashProvider(new com.mucommander.file.impl.trash.OSXTrashProvider());
        }
        // OS/2 family.
        else if(osFamily==OS_2) {
            unixDesktop                  = UNKNOWN_DESKTOP;
            defaultFileManagerName       = null;
            defaultFileManagerCommand    = null;
            defaultFileOpenerCommand     = null;
            defaultUrlOpenerCommand      = null;
            defaultExeOpenerCommand      = POSIX_EXE_OPENER;
            runExecutables               = true;
            exeAssociation               = (javaVersion.compareTo(JAVA_1_6)<0) ? POSIX_EXE_REGEXP : null;
            defaultRegexpCaseSensitivity = true;
        }
        // Unix, or assimilated.
        else {
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
            if(javaVersion.compareTo(JAVA_1_4)<=0) {
                gnomeEnvValue = System.getProperty(GNOME_ENV_VAR);
                kdeEnvValue   = System.getProperty(KDE_ENV_VAR);
            }
            // System.getenv() has been un-deprecated (reprecated?) under Java 1.5, great!
            else {
                gnomeEnvValue = System.getenv(GNOME_ENV_VAR);
                kdeEnvValue   = System.getenv(KDE_ENV_VAR);
            }

            // Checks whether the Gnome environment variable is defined.
            if(gnomeEnvValue!=null && !gnomeEnvValue.trim().equals(""))
                setGnomeValues();

            // Checks whether the KDE environment variable is defined.
            else if(kdeEnvValue!=null && !kdeEnvValue.trim().equals(""))
                setKdeValues();

            // In some cases, KDE doesn't set its environment variable. We
            // can work around such cases by checking whether kfmclient is available.
            else if(couldRun("kfmclient"))
                setKdeValues();

            // gnome-open might be available on some systems which are not running Gnome.
            // It's a good fallback, as it will allow muCommander to use files properly, but
            // has the disadvantage that it will create a 'Reveal in Nautilus' item in the
            // right-click menu.
            else if(couldRun("gnome-open"))
                setGnomeValues();

            // Absolutely no clue what we're running.
            else {
                unixDesktop                  = UNKNOWN_DESKTOP;
                defaultFileManagerName       = null;
                defaultFileManagerCommand    = null;
                defaultFileOpenerCommand     = null;
                defaultUrlOpenerCommand      = null;
                defaultExeOpenerCommand      = POSIX_EXE_OPENER;
                runExecutables               = true;
                exeAssociation               = (javaVersion.compareTo(JAVA_1_6)<0) ? POSIX_EXE_REGEXP : null;
                defaultRegexpCaseSensitivity = true;
            }
        }

        // Identifies the default shell command.
        if(osFamily==WINDOWS) {
            if(osVersion.compareTo(WINDOWS_NT)<0)
                defaultShellCommand  = WINDOWS_9X_SHELL;
            else
                defaultShellCommand  = WINDOWS_NT_SHELL;
        }
        else {
            defaultShellCommand  = POSIX_SHELL;
        }
    }

    private static void setGnomeValues() {
        unixDesktop                  = GNOME_DESKTOP;
        defaultFileManagerName       = GNOME_FILE_MANAGER_NAME;
        defaultFileManagerCommand    = GNOME_FILE_OPENER;
        defaultFileOpenerCommand     = GNOME_FILE_OPENER;
        defaultUrlOpenerCommand      = GNOME_FILE_OPENER;
        defaultExeOpenerCommand      = POSIX_EXE_OPENER;
        runExecutables               = true;
        exeAssociation               = (JAVA_1_6.isCurrentLower()) ? POSIX_EXE_REGEXP : null;
        defaultRegexpCaseSensitivity = true;
    }

    private static void setKdeValues() {
        unixDesktop                   = KDE_DESKTOP;
        defaultFileManagerName        = KDE_FILE_MANAGER_NAME;
        defaultFileManagerCommand     = KDE_FILE_OPENER;
        defaultFileOpenerCommand      = KDE_FILE_OPENER;
        defaultUrlOpenerCommand       = KDE_URL_OPENER;
        defaultExeOpenerCommand       = null;
        exeAssociation                = null;
        runExecutables                = false;
        defaultRegexpCaseSensitivity = true;
        com.mucommander.file.FileFactory.setTrashProvider(new com.mucommander.file.impl.trash.KDETrashProvider());
    }

    /**
     * Returns the Desktop environment the current JVM instance is running on, {@link #UNKNOWN_DESKTOP} if unknown.
     * See constant fields for possible values.
     *
     * @return the OS version the current JVM instance is running on.
     */
    public static int getUnixDesktop() {
        return unixDesktop;
    }

    /**
     * Returns the system's default shell command.
     * @return the system's default shell command, or <code>null</code> if not known.
     */
    public static String getDefaultShellCommand() {return defaultShellCommand;}

    /**
     * Returns the name of the system's default file manager.
     * @return the name of the system's default file manager, or <code>null</code> if not known.
     */
    public static String getDefaultFileManagerName() {return defaultFileManagerName;}

    /**
     * Returns the command used to start the system's default file manager.
     * @return the command used to start the system's default file manager, or <code>null</code> if not found.
     */
    public static String getDefaultFileManagerCommand() {return defaultFileManagerCommand;}

    /**
     * Returns the default command used to open files under the current system.
     * @return the default command used to open files under the current system, or <code>null</code> if not found.
     */
    public static String getDefaultFileOpenerCommand() {return defaultFileOpenerCommand;}

    /**
     * Returns the default command used to open URLs under the current system.
     * @return the default command used to open URLs under the current system, or <code>null</code> if not found.
     */
    public static String getDefaultUrlOpenerCommand() {return defaultUrlOpenerCommand;}

    /**
     * Returns the default command used to open executable files under the current system.
     * @return the default command used to open executable files under the current system, or <code>null</code> if not found.
     */
    public static String getDefaultExeOpenerCommand() {return defaultExeOpenerCommand;}

    /**
     * Returns <code>true</code> if executable files must be opened with a different command than regular files.
     * @return <code>true</code> if executable files must be opened with a different command than regular files, <code>false</code> otherwise.
     */
    public static boolean runExecutables() {return runExecutables;}

    /**
     * Returns the regular expression used to match executable files.
     * @return the regular expression used to match executable files, <code>null</code> if not found.
     */
    public static String getExeAssociation() {return exeAssociation;}

    /**
     * Returns <code>true</code> if the current system should use case-sensitive regular expression when matching file names.
     * @return <code>true</code> if the current system should use case-sensitive regular expression when matching file names, <code>false</code> otherwise.
     */
    public static boolean getDefaultRegexpCaseSensitivity() {return defaultRegexpCaseSensitivity;}

    /**
     * Returns true if the specified command could be executed.
     * @param  cmd command to execute.
     * @return     <code>true</code> if executing the specified command didn't trigger an Exception.
     */
    private static boolean couldRun(String cmd) {
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
     * @return <code>true</code> if the current platform is capable of opening file or folders in the default file manager, <code>false</code> otherwise.
     */
    public static boolean canOpenInFileManager() {return CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS) != null;}	

    /**
     * Returns <code>true</code> if the current platform is capable of opening a URL in a new browser window.
     * @return <code>true</code> if the current platform is capable of opening a URL in a new browser window.
     */
    public static boolean canOpenUrl() {return CommandManager.getCommandForAlias(CommandManager.URL_OPENER_ALIAS) != null;}

    /**
     * Attempts to open the specified file through registered command associations.
     * @param  file        file to open.
     * @return             the process in which the file was opened.
     * @throws IOException if an error occured while creating the file opening process.
     */
    public static AbstractProcess open(AbstractFile file) throws IOException {
        if(Debug.ON) Debug.trace("Opening " + file.getAbsolutePath());

        return ProcessRunner.execute(CommandManager.getTokensForFile(file), file);
    }

    /**
     * Opens the specified URL in the registered web browser.
     * @param  file        URL to open.
     * @throws IOException if an error occurs while opening the specified URL.
     */
    public static void openUrl(AbstractFile file) throws IOException {
        String protocol;

        // Makes sure the file is a URL.
        protocol = file.getURL().getProtocol();
        if(!protocol.equals(FileProtocols.HTTP) && !protocol.equals(FileProtocols.HTTPS))
            throw new IllegalArgumentException("Not an HTTP URL: " + file);

        // Opens the file with the registered URL opener.
        Command command;
        if((command = CommandManager.getCommandForAlias(CommandManager.URL_OPENER_ALIAS)) != null)
            ProcessRunner.execute(command.getTokens(file), file);
    }

    /**
     * Tries to open the specified file in the system's file manager.
     * <p>
     * Developers should make sure the operation is possible before calling this.
     * This can be done through {@link #canOpenInFileManager()}.
     * </p>
     * @param file file to open in the system's file manager.
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
        return (modifiers & MouseEvent.BUTTON1_MASK)!=0 && !(MAC_OS_X.isCurrent() && e.isControlDown());
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
        return (modifiers & MouseEvent.BUTTON3_MASK)!=0 || (MAC_OS_X.isCurrent() && (modifiers & MouseEvent.BUTTON1_MASK)!=0 && e.isControlDown());
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
    public static AbstractFile getDefaultPreferencesFolder() {
        File folder;

        // Mac OS X specific folder (~/Library/Preferences/muCommander)
        if(MAC_OS_X.isCurrent())
            folder = new File(System.getProperty("user.home")+"/Library/Preferences/muCommander");
        // For all other platforms, use generic folder (~/.mucommander)
        else
            folder = new File(System.getProperty("user.home"), "/.mucommander");

        // Makes sure the folder exists.
        if(!folder.exists())
            if(!folder.mkdir())
                if(Debug.ON)
                    Debug.trace("Could not create preference folder: " + folder.getAbsolutePath());

        return FileFactory.getFile(folder.getAbsolutePath());
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
     * @see    #setPreferencesFolder(AbstractFile)
     */
    public static AbstractFile getPreferencesFolder() {
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
     * @param  folder      path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     * @see                #setPreferencesFolder(String)
     * @see                #setPreferencesFolder(AbstractFile)
     */
    public static void setPreferencesFolder(File folder) throws IOException {setPreferencesFolder(FileFactory.getFile(folder.getAbsolutePath()));}

    /**
     * Sets the path to the folder in which muCommande rwill look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  path        path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     * @see                #setPreferencesFolder(File)
     * @see                #setPreferencesFolder(AbstractFile)
     */
    public static void setPreferencesFolder(String path) throws IOException {
        AbstractFile folder;

        if((folder = FileFactory.getFile(path)) == null)
            setPreferencesFolder(new File(path));
        else
            setPreferencesFolder(folder);
    }

    /**
     * Sets the path to the folder in which muCommander will look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  folder      path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     * @see                #setPreferencesFolder(String)
     * @see                #setPreferencesFolder(File)
     */
    public static void setPreferencesFolder(AbstractFile folder) throws IOException {
        if(!folder.exists())
            folder.mkdir();
        else if(!folder.isBrowsable())
            folder = folder.getParent();
        prefFolder = folder;
    }
}

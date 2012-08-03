/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.desktop;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.desktop.gnome.ConfiguredGnomeDesktopAdapter;
import com.mucommander.desktop.gnome.GuessedGnomeDesktopAdapter;
import com.mucommander.desktop.kde.ConfiguredKde3DesktopAdapter;
import com.mucommander.desktop.kde.ConfiguredKde4DesktopAdapter;
import com.mucommander.desktop.kde.GuessedKde3DesktopAdapter;
import com.mucommander.desktop.kde.GuessedKde4DesktopAdapter;
import com.mucommander.desktop.openvms.OpenVMSDesktopAdapter;
import com.mucommander.desktop.osx.OSXDesktopAdapter;
import com.mucommander.desktop.windows.Win9xDesktopAdapter;
import com.mucommander.desktop.windows.WinNtDesktopAdapter;
import com.mucommander.desktop.xfce.GuessedXfceDesktopAdapter;

/**
 * @author Nicolas Rinaudo
 */
public class DesktopManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DesktopManager.class);
	
    // - Predefined operation types --------------------------------------
    // -------------------------------------------------------------------
    /**
     * Represents "browse" operations.
     * <p>
     * These operations are used to open URL in a browser.
     * </p>
     * @see #canBrowse()
     * @see #browse(URL)
     */
    public static final String BROWSE = "browse";

    /**
     * Represents "file manager" operations.
     * <p>
     * These operations are used to reveal local files in a file manager.
     * </p>
     * @see #canOpenInFileManager()
     * @see #openInFileManager(File)
     */
    public static final String OPEN_IN_FILE_MANAGER = "openFM";

    /**
     * Represents "open" operations.
     * <p>
     * These operations are used to open local files.
     * </p>
     * @see #canOpen()
     * @see #open(File)
     */
    public static final String OPEN = "open";



    // - Operation priority ----------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Represents system operations.
     * <p>
     * These operations are for internal use only, and cannot be registered through the
     * {@link #registerOperation(String,int,DesktopOperation)} method.
     * </p>
     */
    public static final int SYSTEM_OPERATION = 0;

    /**
     * Non-system operations.
     * <p>
     * These operations are treated with a lower priority than {@link #SYSTEM_OPERATION} ones, but higher
     * than {@link #FALLBACK_OPERATION} ones. They are meant for plugin specific operations and are expected
     * to be fairly reliable.
     * </p>
     */
    public static final int CUSTOM_OPERATION = 1;

    /**
     * Last resort operations.
     * <p>
     * These operations will only ever be used if nothing else is available. They need only be a best-effort solution.
     * </p>
     */
    public static final int FALLBACK_OPERATION = 2;



    // - Class fields ----------------------------------------------------
    // -------------------------------------------------------------------
    /** All available desktop operations. */
    private static Map<String, List<DesktopOperation>>[] operations;
    /** All known desktops. */
    private static Vector<DesktopAdapter>                desktops;
    /** Current desktop. */
    private static DesktopAdapter                        desktop;
    /** Object used to create instances of {@link AbstractTrash}. */
    private static TrashProvider                         trashProvider;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Prevents instanciation of the class.
     */
    private DesktopManager() {}

    /*
     * Static initialisation.
     * Bear in mind that adapters and operations are registered the 'wrong' way around:
     * the earlier they are registered, the lower their priority.
     */
    static {
        // - Adapters initialisation -------------------------------------
        // ---------------------------------------------------------------
        desktops = new Vector<DesktopAdapter>();

        // The default desktop adapter must be registered first, as we only want to use
        // it if nothing else worked.
        registerAdapter(new DefaultDesktopAdapter());

        // Unix desktops:
        // - check for Gnome before KDE, as it seems to be more popular.
        // - check for 'configured' before 'guessed', as guesses are less reliable and more expensive.
        registerAdapter(new GuessedXfceDesktopAdapter());
        registerAdapter(new GuessedKde3DesktopAdapter());
        registerAdapter(new GuessedKde4DesktopAdapter());
        registerAdapter(new GuessedGnomeDesktopAdapter());
        registerAdapter(new ConfiguredKde3DesktopAdapter());
        registerAdapter(new ConfiguredKde4DesktopAdapter());
        registerAdapter(new ConfiguredGnomeDesktopAdapter());

        // Known OS adapters.
        registerAdapter(new OpenVMSDesktopAdapter());
        registerAdapter(new OSXDesktopAdapter());
        registerAdapter(new Win9xDesktopAdapter());
        registerAdapter(new WinNtDesktopAdapter());



        // - Operations initialization -----------------------------------
        // ---------------------------------------------------------------
        operations = new Hashtable[3];

        // Having 1.6 specific operations registered as the lowest priority system
        // ones ensures that:
        // - they are executed after CommandXXX operations (important, since CommandXXX
        //   operations are user configurable).
        // - they are executed before any other operations (if available, they will
        //   provide safer and better integration than any other operation).
        if(JavaVersion.JAVA_1_6.isCurrentOrHigher()) {
            innerRegisterOperation(OPEN,   SYSTEM_OPERATION,  new InternalOpen());
            innerRegisterOperation(BROWSE, SYSTEM_OPERATION,  new InternalBrowse());
        }

        // Registers CommandXXX operations.
        innerRegisterOperation(BROWSE,               SYSTEM_OPERATION,    new CommandBrowse());
        innerRegisterOperation(OPEN_IN_FILE_MANAGER, SYSTEM_OPERATION,    new CommandOpenInFileManager());
        innerRegisterOperation(OPEN,                 SYSTEM_OPERATION,    new CommandOpen(false));

        // The only FALLBACK operation we have at the time of writing is for OPEN,
        // where we can try to run the file as if it was an executable.
        innerRegisterOperation(OPEN, FALLBACK_OPERATION,  new CommandOpen(true));
    }

    /**
     * Initialises desktop management.
     * <p>
     * If <code>install</code> is set to <code>true</code>, this method
     * might result in installing desktop specific data such as bookmarks, keyboard
     * shortcuts...
     * </p>
     * @param install                         whether or not to install desktop specific information.
     * @throws DesktopInitialisationException if an error occured while initialising desktops.
     */
    public static void init(boolean install) throws DesktopInitialisationException {
        DesktopAdapter current;

        // Browses desktop from the last registered to the first, to make sure that
        // custom desktop adapters are used before the default ones.
        for(int i = desktops.size() - 1; i >= 0; i--) {
            current = desktops.elementAt(i);
            if(current.isAvailable()) {
                desktop = current;
                LOGGER.debug("Using desktop: " + desktop);
                desktop.init(install);
                return;
            }
        }
    }

    /**
     * Makes sure that we have a {@link DesktopAdapter} to work with.
     * <p>
     * If the {@link #init(boolean)} method wasn't called, the <code>DesktopManager</code>
     * will find itself in a situation where it doesn't know which desktop it's running on.
     * Calling this method ensures that, if a {@link DesktopAdapter} instance is required,
     * we have at least the {@link DefaultDesktopAdapter} to work with.
     * </p>
     */
    private static void checkInit() {
        if(desktop == null)
            desktop = new DefaultDesktopAdapter();
    }



    // - Desktop adapter registration ------------------------------------
    // -------------------------------------------------------------------
    /**
     * Registers the specified {@link DesktopAdapter}.
     * <p>
     * Note that the later an adapter is registered, the higher its priority. Since all
     * default adapters are registered at initialisation time, any call to this method
     * will result in the new adapter to be checked before them.
     * </p>
     * @param adapter desktop adapter to register.
     */
    public static void registerAdapter(DesktopAdapter adapter) {desktops.add(adapter);}



    // - Operation registration ------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Registers the specified operation for the specified type and priority.
     */
    private static void innerRegisterOperation(String type, int priority, DesktopOperation operation) {
        List<DesktopOperation> container;

        // Makes sure we have a container for operations of the specified priority.
        if(operations[priority] == null)
            operations[priority] = new Hashtable<String, List<DesktopOperation>>();

        // Makes sure we have a container for operations of the specified type.
        if((container = operations[priority].get(type)) == null)
            operations[priority].put(type, container = new Vector<DesktopOperation>());

        // Creates the requested entry.
        container.add(operation);
    }

    public static void registerOperation(String type, int priority, DesktopOperation operation) {
        if(priority != FALLBACK_OPERATION && priority != CUSTOM_OPERATION)
            throw new IllegalArgumentException();
        innerRegisterOperation(type, priority, operation);
    }



    // - Operation support -----------------------------------------------
    // -------------------------------------------------------------------
    private static List<DesktopOperation> getOperations(String type, int priority) {
        if(operations[priority] == null)
            return null;

        return operations[priority].get(type);
    }

    private static DesktopOperation getAvailableOperation(String type, int priority) {
        DesktopOperation       operation;
        List<DesktopOperation> container;

        // If the operation vector is null, no need to look further.
        if((container = getOperations(type, priority)) != null)
            for(int i = container.size() - 1; i >= 0; i--)
                if((operation = container.get(i)).isAvailable())
                    return operation;
        return null;
    }

    private static DesktopOperation getSupportedOperation(String type, int priority, Object[] target) {
        DesktopOperation       operation;
        List<DesktopOperation> container;

        // If the operation vector is null, no need to look further.
        if((container = getOperations(type, priority)) != null)
            for(int i = container.size() - 1; i >= 0; i--)
                if((operation = container.get(i)).canExecute(target))
                    return operation;
        return null;
    }

    private static DesktopOperation getSupportedOperation(String type, Object[] target) {
        DesktopOperation operation;

        if((operation = getSupportedOperation(type, SYSTEM_OPERATION, target)) != null)
            return operation;
        if((operation = getSupportedOperation(type, CUSTOM_OPERATION, target)) != null)
            return operation;
        if((operation = getSupportedOperation(type, FALLBACK_OPERATION, target)) != null)
            return operation;
        return null;
    }

    private static DesktopOperation getAvailableOperation(String type) {
        DesktopOperation operation;

        if((operation = getAvailableOperation(type, SYSTEM_OPERATION)) != null)
            return operation;
        if((operation = getAvailableOperation(type, CUSTOM_OPERATION)) != null)
            return operation;
        if((operation = getAvailableOperation(type, FALLBACK_OPERATION)) != null)
            return operation;
        return null;
    }

    public static boolean isOperationAvailable(String type) {return getAvailableOperation(type) != null;}

    public static boolean isOperationSupported(String type, Object[] target) {return getSupportedOperation(type, target) != null;}

    public static void executeOperation(String type, Object[] target) throws IOException, UnsupportedOperationException {
        DesktopOperation operation;

        if((operation = getSupportedOperation(type, target)) != null)
            operation.execute(target);
        else
            throw new UnsupportedOperationException();
    }

    public static String getOperationName(String type) throws UnsupportedOperationException {
        DesktopOperation operation;

        if((operation = getAvailableOperation(type)) != null)
            return operation.getName();
        throw new UnsupportedOperationException();

   }

    public static String getOperationName(String type, Object[] target) throws UnsupportedOperationException {
        DesktopOperation operation;

        if((operation = getSupportedOperation(type, target)) != null)
            return operation.getName();
        throw new UnsupportedOperationException();
    }



    // - Browser helpers -------------------------------------------------
    // -------------------------------------------------------------------
    public static boolean canBrowse() {return isOperationAvailable(BROWSE);}

    public static boolean canBrowse(URL url) {return isOperationSupported(BROWSE, new Object[] {url});}

    public static boolean canBrowse(String url) {return isOperationSupported(BROWSE, new Object[] {url});}

    public static boolean canBrowse(AbstractFile url) {return isOperationSupported(BROWSE, new Object[] {url});}

    public static void browse(URL url) throws IOException, UnsupportedOperationException {executeOperation(BROWSE, new Object[] {url});}

    public static void browse(String url) throws IOException, UnsupportedOperationException {executeOperation(BROWSE, new Object[] {url});}

    public static void browse(AbstractFile url) throws IOException, UnsupportedOperationException {executeOperation(BROWSE, new Object[] {url});}



    // - File opening helpers --------------------------------------------
    // -------------------------------------------------------------------
    public static boolean canOpen() {return isOperationAvailable(OPEN);}

    public static boolean canOpen(File file) {return isOperationSupported(OPEN, new Object[] {file});}

    public static boolean canOpen(String file) {return isOperationSupported(OPEN, new Object[] {file});}

    public static boolean canOpen(AbstractFile file) {return isOperationSupported(OPEN, new Object[] {file});}

    public static void open(File file) throws IOException, UnsupportedOperationException {executeOperation(OPEN, new Object[] {file});}

    public static void open(String file) throws IOException, UnsupportedOperationException {executeOperation(OPEN, new Object[] {file});}

    public static void open(AbstractFile file) throws IOException, UnsupportedOperationException {executeOperation(OPEN, new Object[] {file});}



    // - File manager helpers --------------------------------------------
    // -------------------------------------------------------------------
    public static boolean canOpenInFileManager() {return isOperationAvailable(OPEN_IN_FILE_MANAGER);}

    public static boolean canOpenInFileManager(File file) {return isOperationSupported(OPEN_IN_FILE_MANAGER, new Object[] {file});}

    public static boolean canOpenInFileManager(String file) {return isOperationSupported(OPEN_IN_FILE_MANAGER, new Object[] {file});}

    public static boolean canOpenInFileManager(AbstractFile file) {return isOperationSupported(OPEN_IN_FILE_MANAGER, new Object[] {file});}

    public static void openInFileManager(File file) throws IOException, UnsupportedOperationException {executeOperation(OPEN_IN_FILE_MANAGER, new Object[] {file});}

    public static void openInFileManager(String file) throws IOException, UnsupportedOperationException {executeOperation(OPEN_IN_FILE_MANAGER, new Object[] {file});}

    public static void openInFileManager(AbstractFile file) throws IOException, UnsupportedOperationException {executeOperation(OPEN_IN_FILE_MANAGER, new Object[] {file});}

    private static String getFileManagerName(DesktopOperation operation) throws UnsupportedOperationException {
        if(operation == null)
            throw new UnsupportedOperationException();
        return operation.getName();
    }

    public static String getFileManagerName() throws UnsupportedOperationException {return getFileManagerName(getAvailableOperation(OPEN_IN_FILE_MANAGER));}

    public static String getFileManagerName(File file) throws UnsupportedOperationException {
        return getFileManagerName(getSupportedOperation(OPEN_IN_FILE_MANAGER, new Object[] {file}));
    }

    public static String getFileManagerName(String file) throws UnsupportedOperationException {
        return getFileManagerName(getSupportedOperation(OPEN_IN_FILE_MANAGER, new Object[] {file}));
    }

    public static String getFileManagerName(AbstractFile file) throws UnsupportedOperationException {
        return getFileManagerName(getSupportedOperation(OPEN_IN_FILE_MANAGER, new Object[] {file}));
    }



    // - Trash management ------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Returns an instance of the {@link com.mucommander.desktop.AbstractTrash} implementation that can be used on the current platform.
     * @return an instance of the AbstractTrash implementation that can be used on the current platform, or <code>null</code> if none is available.
     */
    public static AbstractTrash getTrash() {
        TrashProvider provider;

        if((provider = getTrashProvider()) == null)
            return null;

        return provider.getTrash();
    }

    /**
     * Returns the object used to create instances of {@link com.mucommander.desktop.AbstractTrash}.
     * @return the object used to create instances of {@link AbstractTrash} if any, <code>null</code> otherwise.
     */
    public static TrashProvider getTrashProvider() {
        return trashProvider;
    }

    /**
     * Sets the object that is used to create instances of {@link com.mucommander.desktop.AbstractTrash}.
     * @param provider object that will be used to create instances of {@link com.mucommander.desktop.AbstractTrash}.
     */
    public static void setTrashProvider(TrashProvider provider) {trashProvider = provider;}


    // - Mouse management ------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Checks whether the specified <code>MouseEvent</code> is a left-click for this destop.
     * <p>
     * There are some cases where Java doesn't detect mouse events properly - for example,
     * <i>CONTROL + LEFT CLICK</i> is a <i>RIGHT CLICK</i> under Mac OS X.<br>
     * The goal of this method is to allow desktop to check for such non-standard behaviours.
     * </p>
     * @param  e event to check.
     * @return   <code>true</code> if the specified event is a left-click for this desktop, <code>false</code> otherwise.
     * @see      #isRightMouseButton(MouseEvent)
     * @see      #isMiddleMouseButton(MouseEvent)
     */
    public static boolean isLeftMouseButton(MouseEvent e) {
        checkInit();
        return desktop.isLeftMouseButton(e);
    }

    /**
     * Checks whether the specified <code>MouseEvent</code> is a left-click for this destop.
     * <p>
     * There are some cases where Java doesn't detect mouse events properly - for example,
     * <i>CONTROL + LEFT CLICK</i> is a <i>RIGHT CLICK</i> under Mac OS X.<br>
     * The goal of this method is to allow desktop to check for such non-standard behaviours.
     * </p>
     * @param  e event to check.
     * @return   <code>true</code> if the specified event is a left-click for this desktop, <code>false</code> otherwise.
     * @see      #isMiddleMouseButton(MouseEvent)
     * @see      #isLeftMouseButton(MouseEvent)
     */
    public static boolean isRightMouseButton(MouseEvent e) {
        checkInit();
        return desktop.isRightMouseButton(e);
    }

    /**
     * Checks whether the specified <code>MouseEvent</code> is a left-click for this destop.
     * <p>
     * There are some cases where Java doesn't detect mouse events properly - for example,
     * <i>CONTROL + LEFT CLICK</i> is a <i>RIGHT CLICK</i> under Mac OS X.<br>
     * The goal of this method is to allow desktop to check for such non-standard behaviours.
     * </p>
     * @param  e event to check.
     * @return   <code>true</code> if the specified event is a left-click for this desktop, <code>false</code> otherwise.
     * @see      #isRightMouseButton(MouseEvent)
     * @see      #isLeftMouseButton(MouseEvent)
     */
    public static boolean isMiddleMouseButton(MouseEvent e) {
        checkInit();
        return desktop.isMiddleMouseButton(e);
    }

    /**
     * Returns the maximum interval in milliseconds between mouse clicks for them to be considered as 'multi-clicks'
     * (e.g. double-clicks). The returned value should reflects the desktop's multi-click (or double-click) interval,
     * which may or may not correspond to the one Java uses for double-clicks.
     * @return the maximum interval in milliseconds between mouse clicks for them to be considered as 'multi-clicks'.
     */
    public static int getMultiClickInterval() {
        checkInit();
        return desktop.getMultiClickInterval();
    }


    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Returns the command used to start shell processes.
     * <p>
     * The returned command must set the shell in its 'run script' mode.
     * For example, for bash, the returned command should be <code>/bin/bash -l -c"</code>.
     * </p>
     * @return the command used to start shell processes.
     */
    public static String getDefaultShell() {
        checkInit();
        return desktop.getDefaultShell();
    }

    /**
     * Returns <code>true</code> if the given file is an application file. What an application file actually is
     * is system-dependent and can take various forms.
     * It can be a simple executable file, as in the case of Windows <code>.exe</code> files, or a directory
     * containing an executable and various meta-information files, like Mac OS X's <code>.app</code> files.
     *
     * @param file the file to test
     * @return <code>true</code> if the given file is an application file
     */
    public static boolean isApplication(AbstractFile file) {
        return desktop.isApplication(file);
    }
}

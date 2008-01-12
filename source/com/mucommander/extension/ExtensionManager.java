/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.extension;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileClassLoader;
import com.mucommander.file.FileFactory;
import com.mucommander.file.filter.ExtensionFilenameFilter;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Manages muCommander's extensions.
 * <p>
 * Extensions must be stored in {@link #getExtensionsFolder()} in order for this class to be aware of them.
 * Moreover, the method {@link #addExtensionsToClasspath()} must have been called before extensions can be used.
 * </p>
 * <p>
 * Extensions are loaded through a custom <code>ClassLoader</code>. The optimal situation is for that <code>ClassLoader</code>
 * to be the system one, which can only be achieved through setting the <code>java.system.class.loader</code> system property
 * to <code>com.mucommander.file.AbstractFileClassLoader</code> at boot time.<br>
 * However, if for some reason such is not the case, we'll use a separate instance of that class. This will work in most cases, but
 * might cause conflicts under rare circumstances. Extension writers are advised to load resources through the <code>ClassLoader</code>
 * returned by {@link #getClassLoader()}, as not doing so might result in using the bootstrap classloader which doesn't have access to
 * resources found in {@link #getExtensionsFolder()}.
 * </p>
 * <p>
 * This class can also be used to load Swing look and feel from JAR files that aren't in the system's classpath. In order to achieve this,
 * application writers must:
 * <ul>
 *   <li>
 *     Call <code>UIManager.getDefaults().put("ClassLoader", ExtensionManager.getClassLoader());</code> when initialising their application.
 *     This will force Swing to use our custom classloader when loading Look&Feels.
 *   </li>
 *   <li>
 *     Call <code>UIManager.setLookAndFeel((LookAndFeel)Class.forName(lnfName, true, ExtensionManager.getClassLoader()).newInstance());</code>
 *     to set a new look and feel. This will ensure that all classes and resources are available when initialising the Look&Feel.
 *   </li>
 * </ul>
 * Unfortunately, this is not always sufficient. Some Look&Feels suffer from a peculiar behaviour in Swing that might cause resources to be loaded
 * through the system class loader rather than the one specified at initialisation time. This happens with Look&Feels that extend system ones, such
 * as <code>Quaqua</code>. The only way to get these to load properly is to make sure the system classloader is an instance of
 * {@link com.mucommander.file.AbstractFileClassLoader}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ExtensionManager {
    // - Class fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** ClassLoader used to load all extensions. */
    private static AbstractFileClassLoader loader;



    // - Extensions folder ------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Path to the extensions folder. */
    private static       AbstractFile extensionsFolder;
    /** Default name of the extensions folder. */
    public  static final String       DEFAULT_EXTENSIONS_FOLDER_NAME = "extensions";



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    static {
        ClassLoader temp;

        // Initialises the extension class loader.
        // If the system classloader is an instance of AbstractFileClassLoader, use it.
        if((temp = ClassLoader.getSystemClassLoader()) instanceof AbstractFileClassLoader)
            loader = (AbstractFileClassLoader)temp;

        // Otherwise, use a new instance of AbstractFileClassLoader.
        else
            loader = new AbstractFileClassLoader();
    }

    /**
     * Prevents instanciations of this class.
     */
    private ExtensionManager() {}



    // - Extension folder access ------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Sets the path to the folder in which all extensions are stored.
     * <p>
     * If the specified path is not browsable (i.e. a folder or any file that muCommander can treat as such), its parent
     * will be used instead.
     * </p>
     * @param  folder      path to the folder in which extensions are stored.
     * @throws IOException if the specified folder or the specified file's parent couldn't be accessed.
     * @see                #setExtensionsFolder(AbstractFile)
     * @see                #setExtensionsFolder(String)
     * @see                #getExtensionsFolder()
     */
    public static void setExtensionsFolder(File folder) throws IOException {setExtensionsFolder(FileFactory.getFile(folder.getAbsolutePath()));}

    /**
     * Sets the path to the folder in which all extensions are stored.
     * <p>
     * If the specified path is not browsable (i.e. a folder or any file that muCommander can treat as such), its parent
     * will be used instead.
     * </p>
     * @param  folder      path to the folder in which extensions are stored.
     * @throws IOException if the specified folder or the specified file's parent couldn't be accessed.
     * @see                #setExtensionsFolder(File)
     * @see                #setExtensionsFolder(String)
     * @see                #getExtensionsFolder()
     */
    public static void setExtensionsFolder(AbstractFile folder) throws IOException {
        // If the folder doesn't exist, create it.
        if(!folder.exists())
            folder.mkdir();

        // If it's not a browsable file, use its parent.
        else if(!folder.isBrowsable())
            folder = folder.getParent();

        extensionsFolder = folder;
    }

    /**
     * Sets the path to the folder in which all extensions are stored.
     * <p>
     * If the specified path is not browsable (i.e. a folder or any file that muCommander can treat as such), its parent
     * will be used instead.
     * </p>
     * @param  path        path to the folder in which extensions are stored.
     * @throws IOException if the specified folder or the specified file's parent couldn't be accessed.
     * @see                #setExtensionsFolder(File)
     * @see                #setExtensionsFolder(String)
     * @see                #getExtensionsFolder()
     */
    public static void setExtensionsFolder(String path) throws IOException {
        AbstractFile folder;

        if((folder = FileFactory.getFile(path)) == null)
            setExtensionsFolder(new File(path));
        else
            setExtensionsFolder(folder);
    }

    /**
     * Returns the path to the default extensions folder.
     * <p>
     * The default path is:
     * <pre>
     * {@link PlatformManager#getPreferencesFolder()}.{@link AbstractFile#getChild(String) getChild}({@link #DEFAULT_EXTENSIONS_FOLDER_NAME});
     * </pre>
     * </p>
     * @return             the path to the default extensions folder.
     * @throws IOException if there was an error retrieving the default extensions folder.
     */
    private static AbstractFile getDefaultExtensionsFolder() throws IOException {
        AbstractFile folder;

        folder = PlatformManager.getPreferencesFolder().getChild(DEFAULT_EXTENSIONS_FOLDER_NAME);

        // Makes sure the folder exists.
        if(!folder.exists())
            folder.mkdir();

        return folder;
    }

    /**
     * Returns the folder in which all extensions are stored.
     * @return             the folder in which all extensions are stored.
     * @throws IOException if an error occured while locating the default extensions folder.
     * @see                #setExtensionsFolder(AbstractFile)
     */
    public static AbstractFile getExtensionsFolder() throws IOException {
        // If the extensions folder has been set, use it.
        if(extensionsFolder != null)
            return extensionsFolder;

        return getDefaultExtensionsFolder();
    }



    // - Classpath querying -----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the specified file is in the extension's classloader path.
     * @param  file file whose presence in the extensions path will be checked.
     * @return      <code>true</code> if the specified file is in the extension's classloader path, <code>false</code> otherwise.
     */
    public static boolean isInExtensionsPath(AbstractFile file) {return loader.contains(file);}

    /**
     * Returns <code>true</code> if the specified file is in the system classpath.
     * @param  file file whose presence in the system classpath will be checked.
     * @return      <code>true</code> if the specified file is in the system classpath, <code>false</code> otherwise.
     */
    public static boolean isInClasspath(AbstractFile file) {
        StringTokenizer parser;
        String          path;

        path   = file.getAbsolutePath();
        parser = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
        while(parser.hasMoreTokens())
            if(parser.nextToken().equals(path))
                return true;
        return false;
    }

    /**
     * Returns <code>true</code> if the specified file is either in the extension or system classpath.
     * <p>
     * This is a convenience method and is equivalent to calling:
     * <code>{@link #isInClasspath(AbstractFile) isInClasspath}(file) || {@link #isInExtensionsPath(AbstractFile) isInExtensionsPath}(file)</code>.
     * </p>
     * @param file file whose availability will be checked.
     * @return <code>true</code> if the specified file is either in the extension or system classpath, <code>false</code> otherwise.
     */
    public static boolean isAvailable(AbstractFile file) {return isInClasspath(file) || isInExtensionsPath(file);}



    // - Classpath extension ----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Imports the specified file in muCommander's libraries.
     * @param file  path to the library to import.
     * @param  force       wether to overwrite eventual existing libraries of the same name.
     * @return             <code>true</code> if the operation was a success,
     *                     <code>false</code> if a library of the same name already exists and
     *                     <code>force</code> is set to <code>false</code>.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean importLibrary(AbstractFile file, boolean force) throws IOException {
        AbstractFile dest;

        // If the file is already in the extensions or classpath,
        // there's nothing to do.
        if(isAvailable(file))
            return true;

        // If the destination file already exists, either delete it
        // if force is set to true or just return false.
        dest = getExtensionsFolder().getDirectChild(file.getName());
        if(dest.exists()) {
            if(!force)
                return false;
            dest.delete();
        }

        // Copies the library and adds it to the extensions classpath.
        file.copyTo(dest);
        addToClassPath(dest);
        return true;
    }

    /**
     * Adds the specified file to the extension's classpath.
     * @param file file to add to the classpath.
     */
    public static void addToClassPath(AbstractFile file) {loader.addFile(file);}

    /**
     * Adds all known extensions to the current classpath.
     * <p>
     * This method will create the following new classpath entries:
     * <ul>
     *   <li>{@link #getExtensionsFolder()}</li>.
     *   <li>All <code>JAR</code> files in {@link #getExtensionsFolder()}.</li>
     * </ul>
     * </p>
     * @throws IOException if the extensions folder is not accessible.
     */
    public static void addExtensionsToClasspath() throws IOException {
        AbstractFile[] files;

        // Adds the extensions folder to the classpath.
        addToClassPath(getExtensionsFolder());

        // Adds all JAR files contained by the extensions folder to the classpath.
        files = getExtensionsFolder().ls(new ExtensionFilenameFilter(".jar"));
        for(int i = 0; i < files.length; i++)
            addToClassPath(files[i]);
    }

    /**
     * Returns the <code>ClassLoader</code> used to load all extensions.
     * @return the <code>ClassLoader</code> used to load all extensions.
     */
    public static ClassLoader getClassLoader() {return loader;}
}

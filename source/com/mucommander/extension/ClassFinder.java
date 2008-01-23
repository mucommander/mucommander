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

package com.mucommander.extension;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileClassLoader;
import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.filter.OrFileFilter;

import java.io.IOException;
import java.util.Vector;

/**
 * Finds specific classes within a browsable file.
 * <p>
 * This class will explore the content of a browsable {@link com.mucommander.file.AbstractFile} and match
 * all discovered classes to a {@link ClassFilter}.
 * </p>
 * <p>
 * In order for classes to be analysed, they need to be loaded. This can be achieved in two ways:
 * <ul>
 *   <li>By using a custom class loader through {@link #find(AbstractFile,ClassFilter,ClassLoader)}.</li>
 *   <li>
 *     By using an {@link com.mucommander.file.AbstractFileClassLoader}
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
public class ClassFinder {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** ClassLoader used to load classes from explored files. */
    private ClassLoader  loader;
    /** Contains all the classes that have been found. */
    private Vector       classes;
    /** Used to filter out files that are neither classes nor directories. */
    private OrFileFilter filter;
    /** Used to filter out unwanted classes. */
    private ClassFilter  classFilter;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>ClassFinder</code>.
     */
    public ClassFinder() {
        filter = new OrFileFilter();
        filter.addFileFilter(new ExtensionFilenameFilter(".class"));
        filter.addFileFilter(new AttributeFileFilter(AttributeFileFilter.DIRECTORY));
    }



    // - File exploring ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Explores the specified file for classes that match {@link #classFilter}.
     * @param  currentPackage package we're currently exploring (with a trailing '.').
     * @param  currentFile    file we're currently exploring.
     * @throws IOException    if an error occurs while exploring <code>currentFile</code>.
     */
    private void find(String currentPackage, AbstractFile currentFile) throws IOException {
        AbstractFile[] files;        // All subfolders or child class files of currentFile.
        Class          currentClass; // Buffer for the current class.

        // Analyses all subdirectories and class files.
        files = currentFile.ls(filter);
        for(int i = 0; i < files.length; i++) {
            // Explores subdirectories recursively.
            if(files[i].isDirectory())
                find(currentPackage + files[i].getName() + '.', files[i]);

            // Passes each class through the class filter.
            // Errors are treated as 'this class is not wanted'.
            else {
                try {
                    if(classFilter.accept(currentClass = Class.forName(currentPackage + files[i].getNameWithoutExtension(), false, loader)))
                        classes.add(currentClass.getName());
                }
                catch(Throwable e) {}
            }
        }
    }



    // - Public code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Explores the content of the specified file and looks for classes that match the specified class filter.
     * <p>
     * The <code>browsable</code> argument must be browsable as defined by {@link com.mucommander.file.AbstractFile#isBrowsable()}.
     * If such is not the case, the returned vector will be empty.
     * </p>
     * @param  browsable   file in which to look for classes.
     * @param  classFilter how to decide which classes should be kept.
     * @param  classLoader used to load each class found in <code>browsable</code>.
     * @return             a vector containing the names of all the classes that were found and matched <code>classFilter</code>.
     * @throws IOException if an error occurs while exploring <code>browsable</code>.
     * @see                #find(AbstractFile,ClassFilter)
     */
    public Vector find(AbstractFile browsable, ClassFilter classFilter, ClassLoader classLoader) throws IOException {
        // Ignore non-browsable files.
        if(!browsable.isBrowsable())
            return new Vector();

        // Initialises exploring.
        classes          = new Vector();
        loader           = classLoader;
        this.classFilter = classFilter;

        // Looks for all matched classes in browsable.
        find("", browsable);

        return classes;
    }

    /**
     * Explores the content of the specified file and looks for classes that match the specified class filter.
     * <p>
     * This is a convenience method and is strictly equivalent to calling {@link #find(AbstractFile,ClassFilter,ClassLoader)}
     * with a class loader argument initialised with the following code:
     * <pre>
     * AbstractFileClassLoader loader;
     *
     * loader = new AbstractFileClassLoader();
     * loader.addFile(browsable);
     * </pre>
     * </p>
     * @param  browsable   file in which to look for classes.
     * @param  classFilter how to decide which classes should be kept.
     * @return             a vector containing the names of all the classes that were found and matched <code>classFilter</code>.
     * @throws IOException if an error occurs while exploring <code>browsable</code>.
     */
    public Vector find(AbstractFile browsable, ClassFilter classFilter) throws IOException {
        AbstractFileClassLoader classLoader; // Default class loader.

        // Initialises the default class loader.
        classLoader = new AbstractFileClassLoader();
        classLoader.addFile(browsable);

        // Explores browsable.
        return find(browsable, classFilter, classLoader);
    }
}

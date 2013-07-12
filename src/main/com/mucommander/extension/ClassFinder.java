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

package com.mucommander.extension;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileClassLoader;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.OrFileFilter;

/**
 * Finds specific classes within a browsable file.
 * <p>
 * This class will explore the content of a browsable {@link com.mucommander.commons.file.AbstractFile} and match
 * all discovered classes to a {@link ClassFilter}.
 * </p>
 * <p>
 * In order for classes to be analyzed, they need to be loaded. This can be achieved in two ways:
 * <ul>
 *   <li>By using a custom class loader through {@link #find(AbstractFile,ClassFilter,ClassLoader)}.</li>
 *   <li>
 *     By using an {@link com.mucommander.commons.file.AbstractFileClassLoader}
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
public class ClassFinder {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** ClassLoader used to load classes from explored files. */
    private ClassLoader  loader;
    /** Used to filter out files that are neither classes nor directories. */
    private OrFileFilter filter;
    /** Used to filter out unwanted classes. */
    private ClassFilter  classFilter;


    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>ClassFinder</code>.
     */
    public ClassFinder() {
        filter = new OrFileFilter(
            new ExtensionFilenameFilter(".class"),
            new AttributeFileFilter(FileAttribute.DIRECTORY)
        );
    }



    // - File exploring ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Explores the specified file for classes that match {@link #classFilter}.
     * @param  currentPackage package we're currently exploring (with a trailing '.').
     * @param  currentFile    file we're currently exploring.
     * @return a vector containing all the classes that were found and matched <code>classFilter</code>.
     * @throws IOException    if an error occurs while exploring <code>currentFile</code>.
     */
    private List<Class<?>> find(String currentPackage, AbstractFile currentFile) throws IOException {
        AbstractFile[]   files;        // All subfolders or child class files of currentFile.
        Class<?>         currentClass; // Buffer for the current class.
        List<Class<?>>   result = new Vector<Class<?>>();
        
        // Analyses all subdirectories and class files.
        files = currentFile.ls(filter);
        for (AbstractFile file : files) {
            // Explores subdirectories recursively.
            if (file.isDirectory())
                result.addAll(find(currentPackage + file.getName() + '.', file));

                // Passes each class through the class filter.
                // Errors are treated as 'this class is not wanted'.
            else {
                try {
                    if (classFilter.accept(currentClass = Class.forName(currentPackage + file.getNameWithoutExtension(), false, loader)))
                        result.add(currentClass);
                }
                catch (Throwable e) {
                }
            }
        }
        return result;
    }



    // - Public code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Explores the content of the specified file and looks for classes that match the specified class filter.
     * <p>
     * The <code>browsable</code> argument must be browsable as defined by {@link com.mucommander.commons.file.AbstractFile#isBrowsable()}.
     * If such is not the case, the returned vector will be empty.
     * </p>
     * @param  browsable   file in which to look for classes.
     * @param  classFilter how to decide which classes should be kept.
     * @param  classLoader used to load each class found in <code>browsable</code>.
     * @return             a vector containing all the classes that were found and matched <code>classFilter</code>.
     * @throws IOException if an error occurs while exploring <code>browsable</code>.
     * @see                #find(AbstractFile,ClassFilter)
     */
    public List<Class<?>> find(AbstractFile browsable, ClassFilter classFilter, ClassLoader classLoader) throws IOException {
        // Ignore non-browsable files.
        if(!browsable.isBrowsable())
            return new Vector<Class<?>>();

        // Initializes exploring.
        loader           = classLoader;
        this.classFilter = classFilter;

        // Looks for all matched classes in browsable.        
        return find("", browsable);
    }

    /**
     * Explores the content of the specified file and looks for classes that match the specified class filter.
     * <p>
     * This is a convenience method and is strictly equivalent to calling {@link #find(AbstractFile,ClassFilter,ClassLoader)}
     * with a class loader argument initialized with the following code:
     * <pre>
     * AbstractFileClassLoader loader;
     *
     * loader = new AbstractFileClassLoader();
     * loader.addFile(browsable);
     * </pre>
     * </p>
     * @param  browsable   file in which to look for classes.
     * @param  classFilter how to decide which classes should be kept.
     * @return             a vector containing all the classes that were found and matched <code>classFilter</code>.
     * @throws IOException if an error occurs while exploring <code>browsable</code>.
     */
    public List<Class<?>> find(AbstractFile browsable, ClassFilter classFilter) throws IOException {
        AbstractFileClassLoader classLoader; // Default class loader.

        // Initializes the default class loader.
        classLoader = new AbstractFileClassLoader();
        classLoader.addFile(browsable);

        // Explores browsable.
        return find(browsable, classFilter, classLoader);
    }
}

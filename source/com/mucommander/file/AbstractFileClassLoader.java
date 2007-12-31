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

package com.mucommander.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * <code>ClassLoader</code> implementation capable of loading classes from instances of {@link AbstractFile}.
 * <p>
 * It's possible to modify this loader's classpath at runtime through the {@link #addFile(AbstractFile)} method.
 * </p>
 * @author Nicolas Rinaudo
 */
public class AbstractFileClassLoader extends ClassLoader {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** All abstract files in which to look for classes and resources. */
    private Vector files;



    // - Initialisation -------------------------------------------------------
    // ------------------------------------------------------------------------
    /**
     * Creates a new <code>AbstractFileClassLoader</code>.
     * @param parent parent of the class loader.
     */
    public AbstractFileClassLoader(ClassLoader parent) {
        super(parent);
        files = new Vector();
    }

    /**
     * Creates a new <code>AbstractFileClassLoader</code> that uses the system classloader as a parent.
     */
    public AbstractFileClassLoader() {this(ClassLoader.getSystemClassLoader());}



    // - File list access ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Adds the specified <code>file</code> to the class loader's classpath.
     * <p>
     * Note that the file will <b>not</b> be added if it's already in the classpath.
     * </p>
     * @param  file                     file to add the class loader's classpath.
     * @throws IllegalArgumentException if <code>file</code> is not browsable.
     */
    public void addFile(AbstractFile file) {
        // Makes sure the specified file is browsable.
        if(!file.isBrowsable())
            throw new IllegalArgumentException();

        // Only adds the file if it's not already there.
        if(!contains(file))
            files.add(file);
    }

    /**
     * Returns an iterator on all files in this loader's classpath.
     * @return an iterator on all files in this loader's classpath.
     */
    public Iterator files() {return files.iterator();}

    /**
     * Returns <code>true</code> if this loader's classpath already contains the specified file.
     * @param  file file to look for.
     * @return      <code>true</code> if this loader's classpath already contains the specified file.
     */
    public boolean contains(AbstractFile file) {return files.contains(file);}



    // - Resource access -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Tries to locate the specified resource and returns an AbstractFile instance on it.
     * @param  name name of the resource to locate.
     * @return      an {@link AbstractFile} instance describing the requested resource if found, <code>null</code> otherwise.
     */
    private AbstractFile findResourceAsFile(String name) {
        Iterator     iterator; // Iterator on all classpath elements.
        AbstractFile file;     // Current file.

        iterator = files.iterator();
        while(iterator.hasNext()) {
            try {
                // If the requested resource could be found, returns it.
                if((file = ((AbstractFile)iterator.next()).getChild(name)).exists())
                    return file;
            }
            // Treats error as a simple 'resource not found' case and keeps looking for
            // one with the correct name that will load.
            catch(IOException e) {}
        }

        // The requested resource wasn't found.
        return null;
    }

    /**
     * Returns an input stream on the requested resource.
     * @param  name name of the resource to open.
     * @return      an input stream on the requested resource, <code>null</code> if not found.
     */
    public InputStream getResourceAsStream(String name) {
        AbstractFile file; // File representing the resource.
        InputStream  in;   // Input stream on the resource.

        // Tries the parent first, to respect the delegation model.
        if((in = getParent().getResourceAsStream(name)) != null)
            return in;

        // Tries to locate the resource in the extended classpath if it wasn't found
        // in the parent.
        if((file = findResourceAsFile(name)) != null) {
            try {return file.getInputStream();}
            catch(Exception e) {}
        }

        // Couldn't find the resource.
        return null;
    }

    /**
     * Tries to find the requested resource.
     * @param  name name of the resource to locate.
     * @return      the URL of the requested resource if found, <code>null</code> otherwise.
     */
    protected URL findResource(String name) {
        AbstractFile file; // Path to the requested resource.

        // Tries to find the resource.
        if((file = findResourceAsFile(name)) == null)
            return null;

        // Tries to retrieve an URL on the resource.
        try {return file.getJavaNetURL();}
        catch(Exception e) {return null;}
    }

    /**
     * Tries to find all the resources with the specified name.
     * @param  name of the resources to find.
     * @return      an enumeration containing the URLs of all the resources that match <code>name</code>.
     */
    protected Enumeration findResources(String name) {
        Iterator     iterator;   // Iterator on all available JAR files.
        AbstractFile file;       // AbstractFile describing each match.
        Vector       resources;  // All resources that match 'name'.

        // Initialisation.
        iterator  = files.iterator();
        resources = new Vector();

        // Goes through all files in the classpath to find the resource.
        while(iterator.hasNext()) {
            try {
                if((file = ((AbstractFile)iterator.next()).getChild(name)).exists())
                    resources.add(file.getJavaNetURL());
            }
            catch(IOException e) {}
        }
        return resources.elements();
    }

    /**
     * Returns the absolute path of the requested library.
     * @param name name of the library to load.
     * @return the absolute path of the requested library if found, <code>null</code> otheriwse.
     */
    protected String findLibrary(String name) {
        AbstractFile file; // Path of the requested library.

        // Tries to find the requested library.
        if((file = findResourceAsFile(name)) == null)
            return null;

        // Retrieves its absolute path.
        return file.getAbsolutePath();
    }



    // - Class loading ---------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Loads the class defined by the specified name and path.
     * @param  name        name of the class to load.
     * @param  file        file containing the class' bytecode.
     * @throws IOException if an error occurs.
     */
    private Class loadClass(String name, AbstractFile file) throws IOException {
        byte[]      buffer; // Buffer for the class' bytecode.
        int         offset; // Current offset in buffer.
        int         read;   // Number of bytes read in the current loop.
        InputStream in;     // Stream on the class' bytecode.

        // Initialisation.
        buffer = new byte[(int)file.getSize()];
        offset = 0;
        in     = null;

        try {
            // Loads the content of file in buffer.
            in = file.getInputStream();
            while(offset != buffer.length)
                offset += in.read(buffer, offset, buffer.length - offset);

            // Loads the class.
            return defineClass(name, buffer, 0, buffer.length);
        }

        // Frees resources.
        finally {
            if(in != null)
                in.close();
        }                
    }

    /**
     * Tries to find and load the specified class.
     * @param name                    fully qualified name of the class to load.
     * @return                        the requested <code>Class</code> if found, <code>null</code> otherwise.
     * @throws ClassNotFoundException if the requested class was not found.
     */
    protected synchronized Class findClass(String name) throws ClassNotFoundException {
        AbstractFile file; // File containing the class' bytecode.
        Class        c;

        // Tries to locate the specified class and, if found, load it.
        if((file = findResourceAsFile(name.replace('.', '/') + ".class")) != null) {
            try {return loadClass(name, file);}
            catch(Exception e) {}
        }
        throw new ClassNotFoundException(name);
    }
}

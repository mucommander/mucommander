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

import com.mucommander.file.filter.FilenameFilter;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;

/**
 * ArchiveFormatMapping maps a {@link FilenameFilter} characterizing the archive format onto an {@link AbstractArchiveFile}
 * class. This class can be used with {@link FileFactory} to register and unregister archive file formats at runtime.
 *
 * @see FileFactory
 * @see AbstractArchiveFile
 * @see FilenameFilter
 * @author Maxence Bernard
 */
public class ArchiveFormatMapping {

    // This fields have package access to allow FileFactory to access them directly, a little faster than using the
    // accessor methods

    /** the Class associated with the archive filter */
    Class providerClass;
    /** the provider class' constructor that is used to create new archive file instances */
    Constructor providerConstructor;
    /** the archive filter associated with the provider class */
    FilenameFilter filenameFilter;

    /**
     * Creates a new ArchiveFormatMapping that associates the archive format characterized by the given FilenameFilter
     * to the given {@link AbstractArchiveFile} class.
     *
     * <p>The class denoted by the specified Class instance must satisfy two conditions:
     * <ul>
     *  <li>it must extend AbstractArchiveFile
     *  <li>it must provide a constructor with the {@link AbstractArchiveFile#AbstractArchiveFile(AbstractFile)} signature
     * </ul>
     * If any of those 2 conditions are not satisfied, an exception will be thrown.
     *
     * @param abstractArchiveFileClass a Class instance denoting a class which extends {@link AbstractArchiveFile} and
     * which has a constructor with the {@link AbstractArchiveFile#AbstractArchiveFile(AbstractFile)} signature
     * @param filenameFilter a FilenameFilter that characterizes the archive format to associate with the specified
     * AbstractArchiveFile class
     * @throws IntrospectionException if the given Class does not extend {@link AbstractArchiveFile}
     * @throws NoSuchMethodException if the given Class does not provide a constructor with the {@link AbstractArchiveFile#AbstractArchiveFile(AbstractFile)} signature
     * @throws SecurityException if access to the constructor is denied
     */
    public ArchiveFormatMapping(Class abstractArchiveFileClass, FilenameFilter filenameFilter) throws IntrospectionException, NoSuchMethodException, SecurityException {
        this.providerClass = abstractArchiveFileClass;

        if(!AbstractArchiveFile.class.isAssignableFrom(abstractArchiveFileClass))
            throw new IntrospectionException(abstractArchiveFileClass.getName()+" does not extend "+AbstractArchiveFile.class.getName());

        this.providerConstructor = abstractArchiveFileClass.getConstructor(new Class[]{AbstractFile.class});

        this.filenameFilter = filenameFilter;
    }

    /**
     * Returns the FilenameFilter that characterizes the archive format associated with the provider class. 
     */
    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }

    /**
     * Returns the Class denoting the {@link AbstractArchiveFile} class associated with the archive FilenameFilter.
     */
    public Class getProviderClass() {
        return providerClass;
    }

    /**
     * Returns the provider class' constructor that is used to create new archive file instances.
     */
    public Constructor getProviderConstructor() {
        return providerConstructor;
    }

    /**
     * Returns <code>true</code> if the given Object is a ArchiveFormatMapping instance with the same FilenameFilter
     * and AbstractArchiveFile classes.
     */
    public boolean equals(Object o) {
        if(!(o instanceof ArchiveFormatMapping))
            return false;

        ArchiveFormatMapping afm = (ArchiveFormatMapping)o;
        return afm.providerClass.equals(providerClass) && afm.filenameFilter.getClass().equals(filenameFilter.getClass());
    }
}

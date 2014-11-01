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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.impl.local.SpecialWindowsLocation;

import java.io.File;
import java.io.IOException;

/**
 * {@link DesktopOperation} implementation meant for actions that involve local files.
 * <p>
 * Instead of having to deal with the {@link DesktopOperation#canExecute(Object[])}
 * and {@link DesktopOperation#execute(Object[])}, instances of <code>LocalFileOperation</code>
 * can use {@link #canExecute(AbstractFile)} and {@link #execute(AbstractFile)} and ignore the complexity of
 * the desktop API's genericity.
 * </p>
 * @author Nicolas Rinaudo
 */
public abstract class LocalFileOperation implements DesktopOperation {
    // - DesktopOperation methods ----------------------------------------
    // -------------------------------------------------------------------
    public abstract String getName();
    public abstract boolean isAvailable();



    // - Wrappers --------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Executes the operation on the specified file.
     * @param  file                          file on which to execute the operation.
     * @throws IOException                   if an error occurs.
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    public abstract void execute(AbstractFile file) throws IOException, UnsupportedOperationException;

    /**
     * Checks whether the operation knows how to deal with the specified file.
     * <p>
     * By default, this method returns {@link #isAvailable()}. However, some implementations
     * might want to overwrite it. For example, a <code>LocalFileOperation</code> that only works
     * on XML files would override this method to only return <code>true</code> if the specified
     * file is an XML one.
     * </p>
     * @param  file file to check against.
     * @return      <code>true</code> if the operation is supported for the specified file, <code>false</code> otherwise.
     */
    public boolean canExecute(AbstractFile file) {return isAvailable();}



    // - DesktopOperation implementation ---------------------------------
    // -------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the operation is supported for the specified parameters.
     * <p>
     * By default, this method will call {@link #extractTarget(Object[])} on the specified parameters
     * and pass the resulting {@link AbstractFile} instance to {@link #canExecute(AbstractFile)}.
     * </p>
     * <p>
     * This behaviour can be overriden by implementations, although most cases can be handled through
     * {@link #canExecute(AbstractFile)} instead.
     * </p>
     * @param  target operation parameters.
     * @return        <code>true</code> if the operation is supported for the specified parameters, <code>false</code> otherwise.
     * @see           #canExecute(AbstractFile)
     * @see           #extractTarget(Object[])
     */
    public boolean canExecute(Object[] target) {
        AbstractFile file;

        if((file = extractTarget(target)) != null)
            return canExecute(file);
        return false;
    }

    /**
     * Analyses the specified parameters and delegates the operation execution to {@link #execute(AbstractFile)}.
     * <p>
     * This method is a wrapper for {@link #extractTarget(Object[])} and {@link #execute(AbstractFile)}. Most
     * implementations should ignore it.
     * </p>
     * @param  target                        parameters of the operation.
     * @throws IOException                   if an error occurs.
     * @throws UnsupportedOperationException if the operation is not supported.
     * @see                                  #execute(AbstractFile)
     * @see                                  #extractTarget(Object[])
     */
    public void execute(Object[] target) throws IOException, UnsupportedOperationException {
        AbstractFile file;

        // Makes sure we received the right kind of parameters.
        if((file = extractTarget(target)) == null)
            throw new UnsupportedOperationException();

        // Execute the operation.
        execute(file);
    }



    // - Parameter analysis ----------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Analyses the specified parameters and returns them in a form that can be used.
     * <p>
     * By default, this method will return <code>null</code> unless <code>target</code>:
     * <ul>
     *   <li>has a length of 1.</li>
     *   <li>
     *     contains an instance of either <code>java.io.File</code>, {@link com.mucommander.commons.file.impl.local.LocalFile}, <code>String</code>
     *     or {@link com.mucommander.commons.file.impl.local.SpecialWindowsLocation}.
     *   </li>
     * </ul>
     * </p>
     * <p>
     * This behaviour can be overridden by implementations to fit their own needs, although it's probably not a great idea.
     * </p>
     * @param  target operation parameters.
     * @return        <code>null</code> if the parameters are not legal, a {@link com.mucommander.commons.file.AbstractFile} instance instead.
     */
    protected AbstractFile extractTarget(Object[] target) {
        // We only deal with arrays containing 1 element.
        if(target.length != 1)
            return null;

        // If we find an instance of java.io.File, we can stop here.
        if(target[0] instanceof File)
            return FileFactory.getFile(((File)target[0]).getAbsolutePath());

        if(target[0] instanceof SpecialWindowsLocation)
            return (AbstractFile)target[0];

        // Deals with instances of LocalFile: raw instances or wrapped in another AbstractFile container (e.g. archive files)
        if(target[0] instanceof AbstractFile && ((AbstractFile)target[0]).hasAncestor(LocalFile.class))
            return (AbstractFile)target[0];

        // Deals with instances of String.
        if(target[0] instanceof String)
            return FileFactory.getFile((String)target[0]);

        // Illegal parameters.
        return null;
    }
}

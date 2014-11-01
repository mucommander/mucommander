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
import com.mucommander.commons.file.impl.http.HTTPFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@link DesktopOperation} implementation meant for actions that involve <code>java.net.URL</code>.
 * <p>
 * Instead of having to deal with the {@link DesktopOperation#canExecute(Object[])}
 * and {@link DesktopOperation#execute(Object[])}, instances of <code>LocalFileOperation</code>
 * can use {@link #canExecute(URL)} and {@link #execute(URL)} and ignore the complexity of
 * the desktop API's genericity.
 * </p>
 * @author Nicolas Rinaudo
 */
public abstract class UrlOperation implements DesktopOperation {
    // - DesktopOperation methods ----------------------------------------
    // -------------------------------------------------------------------
    public abstract String getName();
    public abstract boolean isAvailable();



    // - Wrappers --------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Executes the operation on the specified URL.
     * @param  url                           URL on which to execute the operation.
     * @throws IOException                   if an error occurs.
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    public abstract void execute(URL url) throws IOException;

    /**
     * Checks whether the operation knows how to deal with the specified URL.
     * <p>
     * By default, this method returns {@link #isAvailable()}. However, some implementations
     * might want to overwrite it. For example, a <code>UrlOperation</code> that only works
     * on HTTPS URLs would override this method to only return <code>true</code> if the specified
     * URL is an HTTPS one.
     * </p>
     * @param  url url to check against.
     * @return     <code>true</code> if the operation is supported for the specified file, <code>false</code> otherwise.
     */
    public boolean canExecute(URL url) {return isAvailable();}



    // - DesktopOperation implementation ---------------------------------
    // -------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the operation is supported for the specified parameters.
     * <p>
     * By default, this method will call {@link #extractTarget(Object[])} on the specified parameters
     * and pass the resulting <code>java.net.URL</code> instance to {@link #canExecute(URL)}.
     * </p>
     * <p>
     * This behaviour can be overriden by implementations, although most cases can be handled through
     * {@link #canExecute(URL)} instead.
     * </p>
     * @param  target operation parameters.
     * @return        <code>true</code> if the operation is supported for the specified parameters, <code>false</code> otherwise.
     * @see           #canExecute(URL)
     * @see           #extractTarget(Object[])
     */
    public boolean canExecute(Object[] target) {
        URL url;

        if((url = extractTarget(target)) != null)
            return canExecute(url);
        return false;
    }

    /**
     * Analyses the specified parameters and delegates the operation execution to {@link #execute(URL)}.
     * <p>
     * This method is a wrapper for {@link #extractTarget(Object[])} and {@link #execute(URL)}. Most
     * implementations should ignore it.
     * </p>
     * @param  target                        parameters of the operation.
     * @throws IOException                   if an error occurs.
     * @throws UnsupportedOperationException if the operation is not supported.
     * @see                                  #execute(URL)
     * @see                                  #extractTarget(Object[])
     */
    public void execute(Object[] target) throws IOException, UnsupportedOperationException {
        URL url;

        if((url = extractTarget(target)) == null)
            throw new UnsupportedOperationException();
        execute(url);
    }



    // - Parameter analysis ----------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Analyses the specified parameters and returns them in a form that can be used.
     * <p>
     * By default, this method will return <code>null</code> unless <code>target</code>:
     * <ul>
     *   <li>has a length of 1.</li>
     *   <li>contains an instance of either <code>java.io.File</code>,{@link com.mucommander.commons.file.impl.local.LocalFile} or <code>String</code>.</li>
     * </ul>
     * </p>
     * <p>
     * This behaviour can be overridden by implementations to fit their own needs, although it's probably not a great idea.
     * </p>
     * @param  target operation parameters.
     * @return        <code>null</code> if the parameters are not legal, a <code>java.io.File</code> instance instead.
     */
    protected URL extractTarget(Object[] target) {
        // We only deal with arrays containing 1 element.
        if(target.length != 1)
            return null;

        // If we find an instance of java.net.URL, we can stop here.
        if(target[0] instanceof URL)
            return (URL)target[0];

        // Deals with instances of HTTPFile.
        if(target[0] instanceof HTTPFile)
            return (URL)((AbstractFile)target[0]).getUnderlyingFileObject();

        // Deals with instances of String.
        if(target[0] instanceof String) {
            try {return new URL((String)target[0]);}
            catch(MalformedURLException e) {return null;}
        }

        // Illegal parameters.
        return null;
    }
}

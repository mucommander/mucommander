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

import java.io.IOException;

/**
 * Contract for basic desktop operations.
 * <p>
 * A desktop operation is an system dependent operation tied to the desktop, such as opening a file
 * or launching a web browser on a specified URL.
 * </p>
 * <p>
 * They are meant to be as extensible as possible. This, however, comes with a cost:
 * they can prove to be quite complex to understand and use. Before writing an implementation of
 * this interface, application developers should make sure they understand the following points:
 * <ul>
 *   <li>Generic execution</li>
 *   <li><i>Available</i> Vs <i>Supported</i></li>
 * </ul>
 * </p>
 * <h3>Generic execution</h3>
 * <p>
 * Desktop operations receive an <code>Object[]</code> as parameter to their {@link #execute(Object[])} method.
 * This allows the API to be rather flexible, if a bit more obscure.<br>
 * The basic unwritten contract that all operations must respect is that, for a given operation type, the same
 * parameters classes must be accepted. There is no way to enforce that and keep the flexibility, which means that the
 * responsibility for this lies on the application developer.
 * </p>
 * <p>
 * At the time of writing, the API uses two different types of operations: {@link DesktopManager#BROWSE URL browsing}
 *  and {@link DesktopManager#OPEN local file opening}.
 * Adapters have been provided for these: {@link UrlOperation} and {@link LocalFileOperation}.
 * </p>
 * <h3><i>Available</i> Vs <i>Supported</i></h3>
 * <p>
 * An operation is said to available if it will accept any parameter that matches its contract. For example,
 * an operation that works on local files will be available if it accepts any <code>java.io.File</code>, <code>String</code>
 * or {@link com.mucommander.commons.file.impl.local.LocalFile} parameter.
 * </p>
 * <p>
 * An operation is said to be supported for a specific parameter subset it will accept any parameter that
 * match that subset.
 * </p>
 * <p>
 * An <i>available</i> operation is always supported. However, it's entirely possible for an operation to be supported
 * for some parameters but not others, and thus not to be available.<br>
 * For example, an operation that deals with XML files only will be supported for any parameter that describes a local XML file,
 * but won't be available as it will refuse plain text files.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface DesktopOperation {
    /**
     * Returns the operation's name.
     * <p>
     * The returned value might be displayed to the user. It should thus be made as
     * human readable as possible and, if possible, localised.
     * </p>
     * @return the operation's name.
     */
    public String getName();

    /**
     * Checks whether the operation is available.
     * <p>
     * An operation is said to be available if and only if any call to
     * {@link #canExecute(Object[])} with parameters that match its constraints
     * will return <code>true</code>.
     * </p>
     * <p>
     * For example, an operation of type {@link DesktopManager#BROWSE} that accepts
     * any and all HTTP URLs is available. However, an operation of type
     * {@link DesktopManager#OPEN} that only accepts XML files isn't.
     * </p>
     * @return <code>true</code> if the operation is available, <code>false</code> otherwise.
     * @see    #canExecute(Object[])
     */
    public boolean isAvailable();

    /**
     * Checks whether an operation is supported for the specified parameters.
     * <p>
     * If the operation is {@link #isAvailable() available}, then this method must always
     * return <code>true</code>.
     * </p>
     * <p>
     * If the operation isn't available, but the specified parameters match a subset of legal
     * parameters that it knows how to deal with, this method must return <code>true</code>.
     * </p>
     * <p>
     * In any other case, this method must return <code>false</code>.
     * </p>
     * <p>
     * For example, a {@link DesktopManager#OPEN} operation that only accept XML files will return:
     * <ul>
     *   <li>
     *     <code>true</code> if the parameter array contains a single instance of either <code>java.io.File</code>,
     *     <code>String</code> or {@link com.mucommander.commons.file.impl.local.LocalFile} and that instance describes the
     *     path to a valid XML file.
     *   </li>
     *   <li>
     *     <code>false</code> if the specified parameters are not valid or if they do not describe the path to
     *     a valid XML file.
     *   </li>
     * </ul>
     * </p
     * @param  target parameters to check.
     * @return        <code>true</code> if the operation can be executed with the specified parameters, <code>false</code> otherwise.
     */
    public boolean canExecute(Object[] target);

    /**
     * Executes the operation on the specified parameters.
     * <p>
     * There is no guarantee that this method is available for the specified parameters. This must be checked
     * through {@link #canExecute(Object[])}.
     * </p>
     * @param  target                        parameters on which to execute the operation.
     * @throws IOException                   if an error occurs.
     * @throws UnsupportedOperationException if the operation is not supported for the specified parameters.
     */
    public void execute(Object[] target) throws IOException, UnsupportedOperationException;
}

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
import com.mucommander.commons.file.filter.FileFilter;

import java.awt.event.MouseEvent;

/**
 * Contract for classes that provide desktop integration features.
 * <p>
 * There are two main steps to writing a desktop adapter:
 * <ul>
 *   <li>Desktop detection</li>
 *   <li>Desktop initialisation</li>
 * </ul>
 * </p>
 * <h3>Desktop detection</h3>
 * <p>
 * This is achieved through the {@link #isAvailable()} method. While it has a fairly
 * simple contract, this method can prove quite difficult to implement properly.<br>
 * The <code>com.mucommander.commons.runtime</code> package provides helpfull classes for this,
 * but application developers might end up having to try to run commands to see if they work
 * (this can be done through the {@link com.mucommander.process.ProcessRunner} class), query
 * environment variables, ...
 * </p>
 * <h3>Desktop initialisation</h3>
 * <p>
 * This is achieved through the {@link #init(boolean)} method. Application developers are
 * expected to register all of their desktop specific tools there: {@link DesktopOperation desktop operations},
 * {@link com.mucommander.command.Command commands},
 * {@link com.mucommander.command.CommandManager#registerDefaultAssociation(String, FileFilter) associations}...<br>
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public interface DesktopAdapter {
    // - Detection / Initialisation --------------------------------------
    // -------------------------------------------------------------------
    /**
     * Checks whether or not the desktop is available on the current platform.
     * @return <code>true</code> if the desktop is available on the current platform, <code>false</code> otherwise.
     */
    public boolean isAvailable();

    /**
     * Initialises this desktop.
     * <p>
     * This method is called when an instance of <code>DesktopAdapter</code> has been chosen as the
     * best fit for the current system.<br>
     * This gives the instance an opportunity to set itself up -
     * default {@link com.mucommander.command.Command} and {@link com.mucommander.ui.action.MuAction} registration, 
     * trash management...
     * </p>
     * <p>
     * If the <code>install</code> parameter is set to <code>true</code>, this is the first time the
     * application has been started. The desktop instance should use this opportunity to install platform
     * dependant things such as {@link com.mucommander.bookmark.Bookmark} or {@link com.mucommander.ui.action.ActionKeymap}.
     * </p>
     * @param  install                        <code>true</code> if this is the application's first boot, <code>false</code> otherwise.
     * @throws DesktopInitialisationException if any error occurs.
     */
    public void init(boolean install) throws DesktopInitialisationException;



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
    public boolean isLeftMouseButton(MouseEvent e);

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
    public boolean isRightMouseButton(MouseEvent e);

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
    public boolean isMiddleMouseButton(MouseEvent e);

    /**
     * Returns the maximum interval in milliseconds between mouse clicks for them to be considered as 'multi-clicks'
     * (e.g. double-clicks). The returned value should reflects the desktop's multi-click (or double-click) interval,
     * which may or may not correspond to the one Java uses for double-clicks.
     * @return the maximum interval in milliseconds between mouse clicks for them to be considered as 'multi-clicks'.
     */
    public int getMultiClickInterval();


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
    public String getDefaultShell();

    /**
     * Returns <code>true</code> if the given file is an application file. What an application file actually is
     * is system-dependent and can take various forms.
     * It can be a simple executable file, as in the case of Windows <code>.exe</code> files, or a directory 
     * containing an executable and various meta-information files, like Mac OS X's <code>.app</code> files.
     *
     * @param file the file to test
     * @return <code>true</code> if the given file is an application file
     */
    public boolean isApplication(AbstractFile file);
}

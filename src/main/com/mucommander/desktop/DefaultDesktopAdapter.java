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

import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;

/**
 * Provides a default implementation of the {@link DesktopAdapter} interface.
 * <p>
 * This implementation is meant to help application developers by providing standard
 * implementations of all {@link DesktopAdapter} methods, letting subclasses concentrate
 * on what's important rather than mundane.
 * </p>
 * <p>
 * Moreover, an instance of <code>DefaultDesktopAdapter</code> will be used by the
 * {@link DesktopManager} if no valid desktop could be identifier.
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class DefaultDesktopAdapter implements DesktopAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDesktopAdapter.class);
	
    /** Default multi-click interval when the desktop property cannot be retrieved. */
    public final static int DEFAULT_MULTICLICK_INTERVAL = 500;

    /** Multi-click interval, cached to avoid polling the value every time {@link #getMultiClickInterval()} is called */
    private static int multiClickInterval;

    static {
        try {
            Integer value = ((Integer)Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval"));
            if(value==null)
                multiClickInterval = DEFAULT_MULTICLICK_INTERVAL;
            else
                multiClickInterval = value;
        }
        catch(Exception e) {
            LOGGER.debug("Error while retrieving multi-click interval value desktop property", e);

            multiClickInterval = DEFAULT_MULTICLICK_INTERVAL;
        }
    }

    public String toString() {return "Default Desktop";}

    /**
     * Returns <code>true</code>.
     * @return <code>true</code>.
     */
    public boolean isAvailable() {return true;}

    /**
     * Initialises this desktop.
     * <p>
     * This method is empty. See {@link DesktopAdapter#init(boolean)} for information on
     * how to override it.
     * </p>
     * @param  install                        <code>true</code> if this is the application's first boot, <code>false</code> otherwise.
     * @throws DesktopInitialisationException if any error occurs.
     */
    public void init(boolean install) throws DesktopInitialisationException {
    }

    /**
     * Returns <code>true</code> if the specified mouse event describes a left click.
     * <p>
     * This method will return <code>true</code> if <code>(e.getModifiers() & MouseEvent.BUTTON1_MASK)</code>
     * doesn't equal 0.
     * </p>
     * @param  e event to check.
     * @return   <code>true</code> if the specified event is a left-click, <code>false</code> otherwise.
     * @see      #isRightMouseButton(MouseEvent)
     * @see      #isMiddleMouseButton(MouseEvent)
     */
    public boolean isLeftMouseButton(MouseEvent e) {return (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0;}

    /**
     * Returns <code>true</code> if the specified mouse event describes a middle click.
     * <p>
     * This method will return <code>true</code> if <code>(e.getModifiers() & MouseEvent.BUTTON3_MASK)</code>
     * doesn't equal 0.
     * </p>
     * @param  e event to check.
     * @return   <code>true</code> if the specified event is a middle-click, <code>false</code> otherwise.
     * @see      #isRightMouseButton(MouseEvent)
     * @see      #isLeftMouseButton(MouseEvent)
     */
    public boolean isRightMouseButton(MouseEvent e) {return (e.getModifiers() & MouseEvent.BUTTON3_MASK) !=0;}

    /**
     * Returns <code>true</code> if the specified mouse event describes a right click.
     * <p>
     * This method will return <code>true</code> if <code>(e.getModifiers() & MouseEvent.BUTTON2_MASK)</code>
     * doesn't equal 0.
     * </p>
     * @param  e event to check.
     * @return   <code>true</code> if the specified event is a right-click, <code>false</code> otherwise.
     * @see      #isLeftMouseButton(MouseEvent)
     * @see      #isMiddleMouseButton(MouseEvent)
     */
    public boolean isMiddleMouseButton(MouseEvent e) {return (e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0;}

    /**
     * Returns the value of the <code>"awt.multiClickInterval"</code> desktop property that AWT/Swing uses internally
     * for generating the {@link MouseEvent#getClickCount() click count} returned by <code>MouseListener</code>
     * mouse events. If the property is not set, {@link #DEFAULT_MULTICLICK_INTERVAL} is returned.
     * @see    MouseEvent#getClickCount()
     * @see    java.awt.Toolkit#getDesktopProperty(String) 
     * @return the value of the <code>"awt.multiClickInterval"</code> desktop property that AWT/Swing uses internally
     * for generating the {@link MouseEvent#getClickCount() click count} returned by <code>MouseListener</code>
     * mouse events
     */
    public int getMultiClickInterval() {
        return multiClickInterval;
    }

    /**
     * Returns <code>/bin/sh -l -c"</code>.
     * @return <code>/bin/sh -l -c"</code>.
     */
    public String getDefaultShell() {return "/bin/sh -l -c";}

    /**
     * Always returns <code>false</code>.
     * @return <code>false</code>, always.
     */
    public boolean isApplication(AbstractFile file) {
        return false;
    }
}

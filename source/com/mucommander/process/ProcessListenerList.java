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

package com.mucommander.process;

import java.util.Vector;
import java.util.Iterator;

/**
 * Convenience class used to have more than one listener on any given process.
 * @author Nicolas Rinaudo
 */
public class ProcessListenerList implements ProcessListener {
    // - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** All registered listeners. */
    private Vector listeners;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new process listener list.
     */
    public ProcessListenerList() {listeners = new Vector();}



    // - Listener registration -----------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Adds the specified listener to the list of listeners.
     * @param listener process listener to add.
     */
    public void add(ProcessListener listener) {listeners.add(listener);}

    /**
     * Removes the specified listener from the list of listeners.
     * @param listener process listener to remove.
     */
    public void remove(ProcessListener listener) {listeners.remove(listener);}



    // - Listener code -------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Propagates the <i>process died</i> event to all registered listeners.
     */
    public void processDied(int returnValue) {
        Iterator iterator;

        iterator = listeners.iterator();
        while(iterator.hasNext())
            ((ProcessListener)iterator.next()).processDied(returnValue);
    }

    /**
     * Propagates the <i>process output</i> event to all registered listeners.
     */
    public void processOutput(byte[] buffer, int offset, int length) {
        Iterator iterator;

        iterator = listeners.iterator();
        while(iterator.hasNext())
            ((ProcessListener)iterator.next()).processOutput(buffer, offset, length);
    }

    /**
     * Propagates the <i>process output</i> event to all registered listeners.
     */
    public void processOutput(String output) {
        Iterator iterator;

        iterator = listeners.iterator();
        while(iterator.hasNext())
            ((ProcessListener)iterator.next()).processOutput(output);
    }
}

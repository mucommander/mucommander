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

package com.mucommander.file.impl;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;

import java.io.IOException;
import java.util.Random;

/**
 * DebugFile is a {@link ProxyFile} to be used for debugging purposes. It allows to track the calls made to
 * {@link com.mucommander.file.AbstractFile} methods that are commonly IO-bound, by printing debug information to the
 * standard output when these are called. It also allows to slow those methods down to simulate a slow filesytem.
 *
 * @see Debug
 * @author Maxence Bernard
 */
public class DebugFile extends ProxyFile {

    /** Trace level for the Debug class */
    private int traceLevel;

    /** Maximum latency in milliseconds */
    private int maxLatency;

    /** Used to randomize the latency for each calls to slowed-down methods */
    private static Random random = new Random();

    
    /**
     * Creates a DebugFile that proxies the calls made to the given AbstractFile's methods. The trace level and maximum
     * latency are both set to 0.
     *
     * @param file the AbstractFile to proxy and debug
     */
    public DebugFile(AbstractFile file) {
        this(file, 0, 0);
    }

    /**
     * Creates a DebugFile that proxies the calls made to the given AbstractFile, and prints debug information about
     * calls made to methods that are commonly IO-bound, and slows those methods down by adding some latency.
     *
     * @param file the AbstractFile to proxy and debug
     * @param traceLevel the trace level, see {@link Debug} for more information
     * @param maxLatency the maximum amount of latency in milliseconds
     */
    public DebugFile(AbstractFile file, int traceLevel, int maxLatency) {
        super(file);
        
        this.traceLevel = traceLevel;
        this.maxLatency = maxLatency;
    }


    /**
     * Sets the trace level that will be printed to the standard output for each call to IO-bound AbstractFile methods
     * (i.e. those that are overridden by this class).
     *
     * @param traceLevel the trace level, see {@link Debug} for more information.
     */
    public void setTraceLevel(int traceLevel) {
        this.traceLevel = traceLevel;
    }

    /**
     * Sets the the maximum amount of latency in milliseconds to add to calls made to IO-bound AbstractFile methods
     * (i.e. those that are overridden by this class). The latency is randomized for each method call and uniformly 
     * distributed, the specified value serving as the maximum.
     *
     * @param maxLatency the maximum amount of latency in milliseconds to add to IO-bound AbstractFile method calls
     * (those overridden by this class).
     */
    public void setMaxLatency(int maxLatency) {
        this.maxLatency = maxLatency;
    }


    /**
     * Sleeps a random number of milliseconds, up to {@link #maxLatency}.
     */
    private void lag() {
        if(maxLatency>0) {
            try {
                Thread.sleep(random.nextInt(maxLatency));
            }
            catch(InterruptedException e) {}
        }
    }

    /**
     * Returns the debug string printed for all calls made to the AbstractFile methods overridden by this class.
     */
    private String getDebugString() {
        return "called on "+super.getAbsolutePath()+" ("+file.getClass().getName()+")";
    }


    /////////////////////////////////////////////////////
    // Overridden methods (traced/slowed down methods) //
    /////////////////////////////////////////////////////

    public long getDate() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getDate();
    }

    public long getSize() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getSize();
    }

    public boolean exists() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.exists();
    }

    public boolean isDirectory() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.isDirectory();
    }

    public boolean isSymlink() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.isSymlink();
    }

    public long getFreeSpace() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getFreeSpace();
    }

    public long getTotalSpace() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getTotalSpace();
    }

    public String getName() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getName();
    }

    public String getExtension() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getExtension();
    }

    public String getAbsolutePath() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getAbsolutePath();
    }

    public String getCanonicalPath() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getCanonicalPath();
    }

    public AbstractFile getCanonicalFile() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getCanonicalFile();
    }

    public boolean isBrowsable() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.isBrowsable();
    }

    public boolean isHidden() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.isHidden();
    }

    public int getPermissions() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getPermissions();
    }

    public String getOwner() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getOwner();
    }

    public String getGroup() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getGroup();
    }

    public AbstractFile getRoot() throws IOException {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getRoot();
    }

    public boolean isRoot() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.isRoot();
    }

    public boolean equals(Object f) {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.equals(f);
    }

    public String toString() {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.toString();
    }

    public AbstractFile getParent() throws IOException {
        if(Debug.ON) Debug.trace(getDebugString(), traceLevel);
        lag();

        return super.getParent();
    }
}

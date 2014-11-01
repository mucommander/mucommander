/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

/**
 * DebugFile is a {@link ProxyFile} to be used for debugging purposes. It allows to track the calls made to
 * {@link com.mucommander.commons.file.AbstractFile} methods that are commonly I/O-bound, by logging calls to each of those
 * methods. It also allows to slow those methods down to simulate a slow filesytem.
 *
 * @author Maxence Bernard
 */
public class DebugFile extends ProxyFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugFile.class);

    /** Maximum latency in milliseconds */
    private int maxLatency;

    /** Used to randomize the latency for each calls to slowed-down methods */
    private static Random random = new Random();

    
    /**
     * Creates a DebugFile that proxies the calls made to the given AbstractFile's methods, with no latency.
     *
     * @param file the AbstractFile to proxy and debug
     */
    public DebugFile(AbstractFile file) {
        this(file, 0);
    }

    /**
     * Creates a DebugFile that proxies the calls made to the given AbstractFile and slows those methods down by
     * simulating latency by making I/O bound methods wait.
     *
     * @param file the AbstractFile to proxy and debug
     * @param maxLatency the maximum amount of latency in milliseconds
     */
    public DebugFile(AbstractFile file, int maxLatency) {
        super(file);
        
        this.maxLatency = maxLatency;
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

    @Override
    public long getDate() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getDate();
    }

    @Override
    public long getSize() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getSize();
    }

    @Override
    public boolean exists() {
        LOGGER.trace(getDebugString());
        lag();

        return super.exists();
    }

    @Override
    public boolean isDirectory() {
        LOGGER.trace(getDebugString());
        lag();

        return super.isDirectory();
    }

    @Override
    public boolean isSymlink() {
        LOGGER.trace(getDebugString());
        lag();

        return super.isSymlink();
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        LOGGER.trace(getDebugString());
        lag();

        return super.getFreeSpace();
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        LOGGER.trace(getDebugString());
        lag();

        return super.getTotalSpace();
    }

    @Override
    public String getName() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getName();
    }

    @Override
    public String getExtension() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getExtension();
    }

    @Override
    public String getAbsolutePath() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getAbsolutePath();
    }

    @Override
    public String getCanonicalPath() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getCanonicalPath();
    }

    @Override
    public AbstractFile getCanonicalFile() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getCanonicalFile();
    }

    @Override
    public boolean isArchive() {
        LOGGER.trace(getDebugString());
        lag();

        return super.isArchive();
    }

    @Override
    public boolean isHidden() {
        LOGGER.trace(getDebugString());
        lag();

        return super.isHidden();
    }

    @Override
    public FilePermissions getPermissions() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getPermissions();
    }

    @Override
    public String getOwner() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getOwner();
    }

    @Override
    public String getGroup() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getGroup();
    }

    @Override
    public AbstractFile getRoot() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getRoot();
    }

    @Override
    public boolean isRoot() {
        LOGGER.trace(getDebugString());
        lag();

        return super.isRoot();
    }

    @Override
    public boolean equalsCanonical(Object f) {
        LOGGER.trace(getDebugString());
        lag();

        return super.equals(f);
    }

    public String toString() {
        LOGGER.trace(getDebugString());
        lag();

        return super.toString();
    }

    @Override
    public AbstractFile getParent() {
        LOGGER.trace(getDebugString());
        lag();

        return super.getParent();
    }

}

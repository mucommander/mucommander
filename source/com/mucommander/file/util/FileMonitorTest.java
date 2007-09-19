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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.io.RandomAccessOutputStream;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * A test case for the {@link com.mucommander.file.util.FileMonitor} class.
 *
 * @author Maxence Bernard
 */
public class FileMonitorTest extends TestCase implements FileMonitorConstants {

    /** Temporary file used by the current test */
    private AbstractFile file;
    /** FileMonitor used by the current test */
    private FileMonitor fileMonitor;
    /** FileChangeTracker used by the current test */
    private FileChangeTracker fileChangeTracker;

    /** Poll period used by the FileMonitor (in milliseconds) */
    private final static int POLL_PERIOD = 10;

    /** Number of milliseconds to wait for an attribute change before timing out */
    private final static int TIMEOUT = 5000;

    
    /**
     * Validates that FileMonitor properly reports {@link FileMonitor#DATE_ATTRIBUTE} changes when a file's date changes.
     *
     * @throws IOException should not normally happen
     */
    public void testDateAttribute() throws IOException {
        setUp(DATE_ATTRIBUTE);

        if(!file.changeDate(file.getDate()-2000))
            throw new IOException();

        assertTrue(hasAttributeChanged(DATE_ATTRIBUTE));

        if(!file.changeDate(file.getDate()+2000))
            throw new IOException();

        assertTrue(hasAttributeChanged(DATE_ATTRIBUTE));
    }

    /**
     * Validates that FileMonitor properly reports {@link FileMonitor#SIZE_ATTRIBUTE} changes when a file's size changes.
     *
     * @throws IOException should not normally happen
     */
    public void testSizeAttribute() throws IOException {
        setUp(SIZE_ATTRIBUTE);

        RandomAccessOutputStream raos = file.getRandomAccessOutputStream();
        try {
            raos.setLength(10);

            assertTrue(hasAttributeChanged(SIZE_ATTRIBUTE));

            raos.setLength(0);
        }
        finally {
            if(raos!=null)
                raos.close();
        }
    }

    /**
     * Validates that FileMonitor properly reports {@link FileMonitor#PERMISSIONS_ATTRIBUTE} changes when a file's
     * permissions change.
     *
     * @throws IOException should not normally happen
     */
    public void testPermissionsAttribute() throws IOException {
        setUp(PERMISSIONS_ATTRIBUTE);

        if(!file.setPermission(AbstractFile.USER_ACCESS, AbstractFile.WRITE_PERMISSION, !file.getPermission(AbstractFile.USER_ACCESS, AbstractFile.WRITE_PERMISSION)))
            throw new IOException();

        assertTrue(hasAttributeChanged(PERMISSIONS_ATTRIBUTE));
    }

    /**
     * Validates that FileMonitor properly reports {@link FileMonitor#IS_DIRECTORY_ATTRIBUTE} changes when a file
     * becomes a directory and vice-versa.
     *
     * @throws IOException should not normally happen
     */
    public void testIsDirectoryAttribute() throws IOException {
        setUp(IS_DIRECTORY_ATTRIBUTE);

        file.delete();
        file.mkdir();
        assertTrue(hasAttributeChanged(IS_DIRECTORY_ATTRIBUTE));

        file.delete();
        file.mkfile();
        assertTrue(hasAttributeChanged(IS_DIRECTORY_ATTRIBUTE));
    }

    /**
     * Validates that FileMonitor properly reports {@link FileMonitor#EXISTS_ATTRIBUTE} changes when an existing file or
     * directory is deleted, or when a non-existing file or directory is created. 
     *
     * @throws IOException should not normally happen
     */
    public void testExistsAttribute() throws IOException {
        setUp(EXISTS_ATTRIBUTE);

        file.delete();
        assertTrue(hasAttributeChanged(EXISTS_ATTRIBUTE));

        file.mkdir();
        assertTrue(hasAttributeChanged(EXISTS_ATTRIBUTE));

        file.delete();
        assertTrue(hasAttributeChanged(EXISTS_ATTRIBUTE));

        file.mkfile();
        assertTrue(hasAttributeChanged(EXISTS_ATTRIBUTE));
    }

    /**
     * Called after each test, stops monitoring file changes.
     */
    protected void tearDown() {
        fileMonitor.stopMonitoring();
    }


    /////////////////////////////////
    // Support methods and classes //
    /////////////////////////////////

    /**
     * Sets everything up for a test: retrieves a temporary file instance, create the file, waits until the file exists,
     * create a <code>FileMonitor</code>, register a {@link FileChangeTracker} and finally start monitoring file changes.
     *
     * @param attribute the attribute to monitor
     * @throws IOException should not normally happen
     */
    private void setUp(int attribute) throws IOException {
        // Retrieve a temporary AbstractFile instance that will be deleted on VM shutdown
        file = FileFactory.getTemporaryFile(getClass().getName(), true);
        // Create the file
        file.mkfile();

        // Waits until the file truly exists (I/O are usually asynchroneous)
        while(!file.exists()) {
            try { Thread.sleep(POLL_PERIOD); }
            catch(InterruptedException e) {}
        }

        // Create the monitor, change listener and start monitoring file changes
        fileMonitor = new FileMonitor(file, attribute, POLL_PERIOD);

        fileChangeTracker = new FileChangeTracker();
        fileMonitor.addFileChangeListener(fileChangeTracker);

        fileMonitor.startMonitoring();
    }


    /**
     * Returns <code>true</code> if the current <code>FileMonitor</code> reported a change on the specified attribute.
     * This method will wait up to {@link #TIMEOUT} milliseconds for the attribute to change and if it hasn't changed,
     * will return <code>false</code>.
     *
     * @param attribute the attribute to test against
     * @return true if the current <code>FileMonitor</code> reported a change on the specified attribute
     */
    private boolean hasAttributeChanged(int attribute) {
        boolean hasAttributeChanged = false;

        try {
            synchronized(fileChangeTracker) {
                hasAttributeChanged = (attribute&fileChangeTracker.getChangedAttributes())!=0;

                if(!hasAttributeChanged) {
                    // Waits until FileChangeTracker calls notify to report an attribute change; give up after
                    // TIMEOUT milliseconds
                    fileChangeTracker.wait(TIMEOUT);
                }

                hasAttributeChanged = (attribute&fileChangeTracker.getChangedAttributes())!=0;
            }
        }
        catch(InterruptedException e) {}

        // Resets FileChangeTracker to be ready to detect the next attribute change
        fileChangeTracker.reset();

        return hasAttributeChanged;
    }

    /**
     * This {@link FileChangeListener} keeps track of the attributes that changed, as reported by
     * {@link #fileChanged(com.mucommander.file.AbstractFile, int)}.
     */
    private class FileChangeTracker implements FileChangeListener {

        /** Bit mask that describes the attributes that have changed */
        private int changedAttributes;

        /**
         * Returns a bit mask that describes the attributes that have changed.
         *
         * @return a bit mask that describes the attributes that have changed
         */
        private int getChangedAttributes() {
            return changedAttributes;
        }

        /**
         * Resets the changed attributes bit mask to zero.
         */
        private void reset() {
            this.changedAttributes = 0;
        }

        ///////////////////////////////////////
        // FileChangeListener implementation //
        ///////////////////////////////////////

        public void fileChanged(AbstractFile file, int changedAttributes) {
            synchronized(this) {
                this.changedAttributes |= changedAttributes;

                notify();   // Notify that hasAttributeChanged(int) method that an attribute has changed 
            }
        }
    }

}

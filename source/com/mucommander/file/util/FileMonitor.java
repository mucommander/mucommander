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

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;

import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * <code>FileMonitor</code> allows to monitor a file and detect changes in the file's attributes and notify registered
 * {@link FileChangeListener} listeners accordingly.
 *
 * <p>
 * FileMonitor detects attributes changes by polling the file's attributes at a given frequency and comparing their
 * values with the previous ones. If any of the monitored attributes has changed, {@link FileChangeListener#fileChanged(AbstractFile, int)}
 * is called on each of the registered listeners to notify them of the file attributes that have changed.
 * <br>Here's the list of file attributes that can be monitored:
 * <ul>
 *  <li>{@link #DATE_ATTRIBUTE}
 *  <li>{@link #SIZE_ATTRIBUTE}
 *  <li>{@link #PERMISSIONS_ATTRIBUTE}
 *  <li>{@link #IS_DIRECTORY_ATTRIBUTE}
 *  <li>{@link #EXISTS_ATTRIBUTE}
 * </ul>
 * </p>
 *
 * <p>The polling frequency is controlled by the poll period. This parameter determines how often the file's attributes
 * are checked. The lower this period is, the faster changes will be reported to listeners, but also the higher the
 * impact on I/O and CPU. This parameter should be carefully specified to avoid hogging resources excessively.</p>
 *
 * <p>Note that FileMonitor uses file attributes polling because the Java API doesn't currently provide any better way
 * to do detect file changes. If Java ever does provide a callback mechanism for detecting file changes, this class
 * will be modified to take advantage of it. Another possible improvement would be to add JNI hooks for platform-specific
 * filesystem events such as 'inotify' (Linux Kernel), 'kqueue' (BSD, Mac OS X), PAM (Solaris), ...</p>
 *
 * @see FileChangeListener
 * @author Maxence Bernard
 */
public class FileMonitor implements FileMonitorConstants, Runnable {

    /** Monitored file */
    private AbstractFile file;
    /** Monitored attributes */
    private int attributes;
    /** Poll period in milliseconds, i.e. the time to elapse between two file attributes polls */
    private long pollPeriod;

    /** The thread that actually does the file attributes polling and event firing */
    private Thread monitorThread;

    /**
     * True once this monitor is ready to catch file changes, that is when the monitor thread has been started and
     * initial file attributes have been fetched.
     */
    private boolean isInitialized;

    /** Registered FileChangeListener instances, stored as weak references */
    private WeakHashMap listeners = new WeakHashMap();


    /**
     * Creates a new FileMonitor that monitors the given file for changes, using the default attribute set (as defined
     * by {@link #DEFAULT_ATTRIBUTES}) and default poll period (as defined by {@link #DEFAULT_POLL_PERIOD}).
     *
     * <p>See the general constructor {@link #FileMonitor(AbstractFile, int, long)} for more information.
     *
     * @param file the AbstractFile to monitor for changes
     */
    public FileMonitor(AbstractFile file) {
        this(file, DEFAULT_ATTRIBUTES, DEFAULT_POLL_PERIOD);
    }

    /**
     * Creates a new FileMonitor that monitors the given file for changes, using the specified attribute set and
     * default poll period as defined by {@link #DEFAULT_POLL_PERIOD}.
     *
     * <p>See the general constructor {@link #FileMonitor(AbstractFile, int, long)} for more information.
     *
     * @param file the AbstractFile to monitor for changes
     * @param attributes the set of attributes to monitor, see constant fields for a list of possible attributes
     */
    public FileMonitor(AbstractFile file, int attributes) {
        this(file, attributes, DEFAULT_POLL_PERIOD);
    }

    /**
     * Creates a new FileMonitor that monitors the given file for changes, using the specified poll period and
     * default attribute set as defined by {@link #DEFAULT_ATTRIBUTES}).
     *
     * <p>See the general constructor {@link #FileMonitor(AbstractFile, int, long)} for more information.
     *
     * @param file the AbstractFile to monitor for changes
     * @param pollPeriod number of milliseconds between two file attributes polls
     */
    public FileMonitor(AbstractFile file, long pollPeriod) {
        this(file, DEFAULT_ATTRIBUTES, pollPeriod);
    }

    /**
     * Creates a new FileMonitor that monitors the given file for changes, using the specified attribute set
     * and poll period.
     *
     * <p>Note that monitoring will only start after {@link #startMonitoring()} has been called.</p>
     *
     * <p>
     * The following attributes can be monitored:
     * <ul>
     *  <li>{@link #DATE_ATTRIBUTE}
     *  <li>{@link #SIZE_ATTRIBUTE}
     *  <li>{@link #PERMISSIONS_ATTRIBUTE}
     *  <li>{@link #IS_DIRECTORY_ATTRIBUTE}
     *  <li>{@link #EXISTS_ATTRIBUTE}
     * </ul>
     * Several attributes can be specified by combining them with the binary OR operator.
     * </p>
     *
     * <p>
     * The poll period specified in the constructor determines how often the file's attributes will be checked.
     * The lower this period is, the faster changes will be reported to registered listeners, but also the higher the
     * impact on I/O and CPU.
     * <br>Note that the time spent for polling is taken into account for the poll period. For example, if the poll
     * period is 1000ms, and polling the file's attributes took 50ms, the next poll will happen in 950ms.
     * </p>
     *
     * @param file the AbstractFile to monitor for changes
     * @param attributes the set of attributes to monitor, see constant fields for a list of possible attributes
     * @param pollPeriod number of milliseconds between two file attributes polls
     */
    public FileMonitor(AbstractFile file, int attributes, long pollPeriod) {
        this.file = file;
        this.attributes = attributes;
        this.pollPeriod = pollPeriod;
    }


    /**
     * Adds the given {@link FileChangeListener} instance to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeFileChangeListener(FileChangeListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the FileChangeListener to add to the list of registered listeners.
     */
    public void addFileChangeListener(FileChangeListener listener) {
        listeners.put(listener, null);
    }

    /**
     * Removes the given {@link FileChangeListener} instance to the list of registered listeners.
     *
     * @param listener the FileChangeListener to remove from the list of registered listeners.
     */
    public void removeFileChangeListener(FileChangeListener listener) {
        listeners.remove(listener);
    }


    /**
     * Starts monitoring the monitored file in a dedicated thread. Does nothing if monitoring has already been started
     * and not stopped yet. Calling this method after {@link #stopMonitoring()} has been called will resume monitoring.

     * <p>Once started, the monitoring thread will check for changes in the monitored file attributes specified in
     * the constructor, and call registered {@link FileChangeListener} instances whenever a change in one or several
     * attributes has been detected. The poll period specified in the constructor determines how often the file's
     * attributes will be checked.</p>
     *
     * <p>This method waits until the thread is started effectively and the monitor is ready to monitor file changes.
     * This guarantees that all changes made to the monitored file after this method returns will be caught and properly
     * reported to listeners.</p>
     *
     * <p><code>FileMonitor</code> will keep monitoring the file until {@link #stopMonitoring()} is called, even if the
     * monitored file doesn't exist anymore. Thus, it is important not to forget to call {@link #stopMonitoring()} when
     * monitoring is not needed anymore, in order to prevent unnecessary resource hogging.</p>
     */
    public synchronized void startMonitoring() {
        if(monitorThread ==null) {
            monitorThread = new Thread(this);
            monitorThread.start();

            isInitialized = false;
            // Wait until the thread has been started and initial file attributes have been fetched
            while(!isInitialized) {
                try {
                    wait();     // run() will notify when initialization is complete
                }
                catch(InterruptedException e) {}
            }
        }
    }

    /**
     * Stops monitoring the monitored file. Does nothing if monitoring has not yet been started.
     */
    public synchronized void stopMonitoring() {
        monitorThread = null;
    }

    /**
     * Returns <code>true</code> if this FileMonitor is currently monitoring the file.
     *
     * @return true if this FileMonitor is currently monitoring the file.
     */
    public synchronized boolean isMonitoring() {
        return monitorThread!=null;
    }


    /**
     * Notifies all registered FileChangeListener instances that the monitored file has changed, specifying which
     * file attributes have changed.
     *
     * @param changedAttributes the set of attributes that have changed
     */
    private void fireFileChangeEvent(int changedAttributes) {
        if(Debug.ON) Debug.trace("firing an event to registered listeners, changed attributes="+changedAttributes);

        // Iterate on all listeners
        Iterator iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((FileChangeListener)iterator.next()).fileChanged(file, changedAttributes);
    }

    
    /////////////////////////////
    // Runnable implementation //
    /////////////////////////////

    public void run() {
        Thread thisThread = monitorThread;

        long lastDate = (attributes&DATE_ATTRIBUTE)!=0?file.getDate():0;
        long lastSize = (attributes&SIZE_ATTRIBUTE)!=0?file.getSize():0;
        int lastPermissions = (attributes&PERMISSIONS_ATTRIBUTE)!=0?file.getPermissions():0;
        boolean lastIsDirectory = (attributes&IS_DIRECTORY_ATTRIBUTE)!=0 && file.isDirectory();
        boolean lastExists = (attributes&EXISTS_ATTRIBUTE)!=0 && file.exists();

        synchronized(this) {
            // We are now ready to detect file changes, notify the thread that started this thread
            isInitialized = true;
            notify();
        }

        long now;
        int changedAttributes;

        long tempLong;
        int tempInt;
        boolean tempBool;

        while(monitorThread ==thisThread) {
            changedAttributes = 0;
            now = System.currentTimeMillis();

            if((attributes&DATE_ATTRIBUTE)!=0) {
                if((tempLong=file.getDate())!=lastDate) {
                    lastDate = tempLong;
                    changedAttributes |= DATE_ATTRIBUTE;
                }
            }

            if(monitorThread ==thisThread && (attributes&SIZE_ATTRIBUTE)!=0) {
                if((tempLong=file.getSize())!=lastSize) {
                    lastSize = tempLong;
                    changedAttributes |= SIZE_ATTRIBUTE;
                }
            }

            if(monitorThread ==thisThread && (attributes&PERMISSIONS_ATTRIBUTE)!=0) {
                if((tempInt=file.getPermissions())!=lastPermissions) {
                    lastPermissions = tempInt;
                    changedAttributes |= PERMISSIONS_ATTRIBUTE;
                }
            }

            if(monitorThread ==thisThread && (attributes& IS_DIRECTORY_ATTRIBUTE)!=0) {
                if((tempBool=file.isDirectory())!=lastIsDirectory) {
                    lastIsDirectory = tempBool;
                    changedAttributes |= IS_DIRECTORY_ATTRIBUTE;
                }
            }

            if(monitorThread ==thisThread && (attributes&EXISTS_ATTRIBUTE)!=0) {
                if((tempBool=file.exists())!=lastExists) {
                    lastExists = tempBool;
                    changedAttributes |= EXISTS_ATTRIBUTE;
                }
            }

            if(changedAttributes!=0)
                fireFileChangeEvent(changedAttributes);

            // Get some well-deserved rest: sleep for the specified poll period minus the time we spent
            // for this iteration
            try {
                Thread.sleep(Math.max(pollPeriod-(System.currentTimeMillis()-now), 0));
            }
            catch(InterruptedException e) {
            }
        }
    }
}

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

package com.mucommander.file.impl.trash;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractTrash;

import java.util.Vector;

/**
 * QueuedTrash is an {@link AbstractTrash} which moves files to the trash asynchroneously.
 *
 * <p>
 * When {@link #moveToTrash(com.mucommander.file.AbstractFile)} is called, the file is added to a queue.
 * The file is not moved to the trash immediately: the trash will wait a period of {@link #QUEUE_PERIOD} milliseconds
 * for additional files to be added. If files were added during that period, the trash will wait another period and
 * so on. When no more files are added were added during the period, {@link #moveToTrash(java.util.Vector)} is called
 * with the list of queued files to move to the trash.
 * </p>
 *
 * <p>
 * This mechanism allows to group calls to the underlying trash. It is effective when the atomic operation
 * of moving a file to the trash has a high cost and {@link #moveToTrash(com.mucommander.file.AbstractFile)} is called
 * repeatedly. One thing to note is since the move is performed asynchroneously,
 * {@link #moveToTrash(com.mucommander.file.AbstractFile)} returns immediately without waiting for the file to be moved,
 * {@link #waitForPendingOperations()} can be used to wait for the files to have effectively been moved.
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class QueuedTrash extends AbstractTrash {

    /** Contains the files that are waiting to be moved to the trash */
    private final static Vector queuedFiles = new Vector();

    /** Use to synchronize access to the trash */
    protected final static Object moveToTrashLock = new Object();

    /** Thread that performs the actual job of moving files to the trash */
    protected static Thread moveToTrashThread;

    /** Amount of time in millisecondes to wait for additional files before moving them to the trash */
    protected final static int QUEUE_PERIOD = 1000;


    /**
     * Moves the {@link AbstractFile} instances contained in the given <code>Vector</code> to the trash.
     * Returns <code>true</code> if all files were moved successfully.
     *
     * @param queuedFiles a Vector of AbstractFile to move to the trash
     * @return true if all files were moved successfully
     */
    protected abstract boolean moveToTrash(Vector queuedFiles);


    //////////////////////////////////
    // AbstractTrash implementation //
    //////////////////////////////////

    /**
     * Implementation notes: this method adds the given file to the queue of files to be moved to the trash and returns
     * immediately, i.e. without waiting for the file to be moved. The specified file will only be added to the queue if
     * {@link #canMoveToTrash(com.mucommander.file.AbstractFile)} returned <code>true</code> for it. 
     * Since the actual move is performed asynchroneously, this method has no way of
     * knowing if the file was successfully moved to the trash. So this method will return <code>true</code> if the
     * given file has been scheduled to be moved to the trash, but it may end up failing to be moved for whatever reason.
     */
    public boolean moveToTrash(AbstractFile file) {
        if(!canMoveToTrash(file))
            return false;

        synchronized(moveToTrashLock) {
            // Queue the given file
            queuedFiles.add(file);

            // Create a new thread and start it if one isn't already running
            if(moveToTrashThread ==null) {
                moveToTrashThread = new MoveToTrashThread();
                moveToTrashThread.start();
            }
        }

        return true;
    }

    public void waitForPendingOperations() {
        synchronized(moveToTrashLock) {
            if(moveToTrashThread!=null) {
                try {
                    // Wait until moveToTrashThread wakes this thread up
                    moveToTrashLock.wait();
                }
                catch(InterruptedException e) {
                }
            }
        }
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Performs the actual job of moving files to the trash.
     *
     * <p>The thread starts by waiting {@link OSXTrash#QUEUE_PERIOD} milliseconds before moving them to give additional
     * files a chance to be queued and regrouped as a single call to {@link QueuedTrash#moveToTrash(java.util.Vector)}.
     * If more files were queued during that period, the thread will wait an additional {@link OSXTrash# QUEUE_PERIOD},
     * and so on.<p>
     */
    private class MoveToTrashThread extends Thread {

        public void run() {
            // Loops until no files were added during the sleep period
            int queueSize;
            do {
                queueSize = queuedFiles.size();

                try {
                    Thread.sleep(QUEUE_PERIOD);
                }
                catch(InterruptedException e) {}
            }
            while(queueSize!=queuedFiles.size());

            synchronized(moveToTrashLock) {     // Files can't be added to queue while files are moved to trash
                moveToTrash(queuedFiles);

                queuedFiles.clear();
                // Wake up any thread waiting for this thread to be finished
                moveToTrashLock.notify();
                moveToTrashThread = null;
            }
        }
    }
}

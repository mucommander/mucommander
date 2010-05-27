/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.main.tree;

import com.mucommander.AppLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A class that monitors IOThread if it is running or has been blocked.
 * This class maintains a list of tasks to execute and a thread that 
 * executes these tasks. It checks periodically if the IOThread is running.
 * If IOThread has been blocked then it's killed and a new IOThread is 
 * instantiated. Then the next task from the list will be executed.
 * @author Mariusz Jakubowski
 *
 */
public class AbstractIOThreadManager extends Thread {
  
    /** a queue with tasks to execute */
    protected final List<Runnable> queue = Collections.synchronizedList(new ArrayList<Runnable>());

    /** a thread that executes tasks */
    protected IOThread ioThread;
    
    /** a time after i/o thread is marked as blocked */
    protected long blockThreshold;
    

    /**
     * Creates a new monitoring thread.
     * @param name a name of this thread
     * @param blockThreshold a time after an i/o task is marked as blocked [ms]
     */
    public AbstractIOThreadManager(String name, long blockThreshold) {
        super(name);
        this.blockThreshold = blockThreshold;
        ioThread = new IOThread(queue, blockThreshold);
        ioThread.start();
    }

    /**
     * Adds new task to execute. A task is an instance of Runnable interface.
     * A proper exception handling within the Runnable instance have to be implemented.
     * If this task rises an exception, this exception is printed to stderr.
     * @param task a task to be executed
     */
    public void addTask(Runnable task) {
        queue.add(task);
        synchronized(ioThread) {
            ioThread.notify();
        }
    }
    

    
    @Override
    public void run() {
        while (!interrupted()) {
            synchronized (queue) {
                if (ioThread.isBlocked()) {
                    AppLogger.fine("Killing IOThread " + ioThread);
                    ioThread.interrupt();
                    ioThread = new IOThread(queue, blockThreshold);
                    ioThread.start();
                }
            }
            try {
                sleep(blockThreshold);
            } catch (InterruptedException e) {
                break;
            }
        }
        ioThread.interrupt();
    }
    
    
}

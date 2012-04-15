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

import java.util.List;

/**
 * A thread that executes i/o operations. 
 * @author Mariusz Jakubowski
 *
 */
public class IOThread extends Thread {
    
    /** a queue with tasks to execute */
    private List<Runnable> queue;
    
    /** a time after this thread is marked as blocked */
    private long blockThreshold;

    /** a time when this thread signalled that is alive */
    private volatile long lastActionTime = 0;
    
    
    /**
     * Creates a new instance of an IOThread.
     * @param queue a queue with tasks
     * @param blockThreshold a time after this thread is marked as blocked [ms]
     */
    public IOThread(List<Runnable> queue, long blockThreshold) {
        super("IOThread");
        this.queue = queue;
        this.blockThreshold = blockThreshold;
    }
    
    
    
    @Override
    public void run() {
        
        while (!interrupted()) {
            lastActionTime = System.currentTimeMillis(); 
            while (queue.size() > 0) {
                Runnable task = queue.remove(0);
                try {
                    task.run();
                } catch (Exception e) {
                    AppLogger.fine("Caught exception", e);
                }
                lastActionTime = System.currentTimeMillis(); 
            }
            try {
                synchronized (this) {
                    wait(blockThreshold / 2);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
        
    }
    
    /**
     * Checks if current thread is blocked. This is done by checking if 
     * last action time is smaller than block threshold.
     * @return true if thread is running
     */
    public boolean isBlocked() {
        return (lastActionTime != 0) && (System.currentTimeMillis() - lastActionTime > blockThreshold); 
    }
    
}

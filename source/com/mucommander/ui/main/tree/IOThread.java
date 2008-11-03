package com.mucommander.ui.main.tree;

import java.util.List;

/**
 * A thread that executes i/o operations. 
 * @author Mariusz Jakubowski
 *
 */
public class IOThread extends Thread {
    
    /** a queue with tasks to execute */
    private List queue;
    
    /** a time after this thread is marked as blocked */
    private long blockThreshold;

    /** a time when this thread signalled that is alive */
    private volatile long lastActionTime = 0;
    
    
    /**
     * Creates a new instance of an IOThread.
     * @param queue a queue with tasks
     * @param blockThreshold a time after this thread is marked as blocked [ms]
     */
    public IOThread(List queue, long blockThreshold) {
        super("IOThread");
        this.queue = queue;
        this.blockThreshold = blockThreshold;
    }
    
    
    
    public void run() {
        
        while (!interrupted()) {
            lastActionTime = System.currentTimeMillis(); 
            while (queue.size() > 0) {
                Runnable task = (Runnable) queue.remove(0);
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
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

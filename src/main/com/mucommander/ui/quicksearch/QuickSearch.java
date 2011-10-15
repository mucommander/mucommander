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

package com.mucommander.ui.quicksearch;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;

/**
 * 
 * @author Arik Hadas
 */
public abstract class QuickSearch implements KeyListener, Runnable {

	/** Quick search string */
    protected String searchString;

    /** Timestamp of the last search string change, used when quick search is active */
    private long lastSearchStringChange;

    /** Thread that's responsible for canceling the quick search on timeout,
     * has a null value when quick search is not active */
    private Thread timeoutThread;

	/** Quick search timeout in ms */
    private final static int QUICK_SEARCH_TIMEOUT = 2000;

    /** Icon that is used to indicate in the status bar that quick search has failed */
    protected final static String QUICK_SEARCH_KO_ICON = "quick_search_ko.png";

    /** Icon that is used to indicate in the status bar that quick search has found a match */
    protected final static String QUICK_SEARCH_OK_ICON = "quick_search_ok.png";
    
    
    protected QuickSearch(JComponent compoenent) {
    	
    	// Listener to key events to start quick search or update search string when it is active
        compoenent.addKeyListener(this);
    }
    
    /**
     * Turns on quick search mode. This method has no effect if the quick search is already active.
     * {@link #isActive() isActive()} will return <code>true</code> after this call, and until the quick search has
     * timed out or has been cancelled by user.
     */
    protected synchronized void start() {
        if(!isActive()) {
            // Reset search string
            searchString = "";
            // Start the thread that's responsible for canceling the quick search on timeout
            timeoutThread = new Thread(this, "QuickSearch timeout thread");
            timeoutThread.start();
            lastSearchStringChange = System.currentTimeMillis();

            searchStarted();
        }
    }

    /**
     * Stops the current quick search. This method has no effect if the quick search is not currently active.
     */
    public synchronized void stop() {
        if(isActive()) {
            timeoutThread = null;

            searchStopped();
        }
    }

    /**
     * Returns <code>true</code> if a quick search is being performed.
     *
     * @return true if a quick search is being performed
     */
    public synchronized boolean isActive() {
        return timeoutThread != null;
    }


    /**
     * Returns <code>true</code> the current quick search string matches the given filename.
     * Always returns <code>false</code> when the quick search is inactive.
     *
     * @param filename the filename to test against the quick search string
     * @return true if the current quick search string matches the given filename
     */
    public boolean matches(String filename) {
        return isActive() && filename.toLowerCase().indexOf(searchString.toLowerCase())!=-1;
    }


    /**
     * Returns <code>true</code> if the given <code>KeyEvent</code> corresponds to a valid quick search input,
     * <code>false</code> in any of the following cases:
     *
     * <ul>
     *   <li>has any of the Alt, Ctrl or Meta modifier keys down (Shift is OK)</li>
     *   <li>is an ASCII control character (<32 or ==127)</li>
     *   <li>is not a valid Unicode character</li>
     * </ul>
     *
     * @param e the KeyEvent to test
     * @return true if the given <code>KeyEvent</code> corresponds to a valid quick search input
     */
    protected boolean isValidQuickSearchInput(KeyEvent e) {
        if((e.getModifiersEx()&(KeyEvent.ALT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK|KeyEvent.META_DOWN_MASK))!=0)
            return false;

        char keyChar = e.getKeyChar();
        return keyChar>=32 && keyChar!=127 && Character.isDefined(keyChar);
    }
    
    /**
     * Setter for the last search string change time
     * 
     * @param lastSearchStringChange - the time of the last change made to the search string
     */
	protected void setLastSearchStringChange(long lastSearchStringChange) {
		this.lastSearchStringChange = lastSearchStringChange;
	}

	//////////////////////
	// Abstract methods //
	//////////////////////

	/**
	 * Hook that is called after the search is started
	 */
	protected abstract void searchStarted();
	
	/**
	 * Hook that is called after the search is stopped
	 */
	protected abstract void searchStopped();

    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        do {
            try { Thread.sleep(100); }
            catch(InterruptedException e) {
                // No problemo
            }

            synchronized(this) {
                if(timeoutThread!=null && System.currentTimeMillis()-lastSearchStringChange >= QUICK_SEARCH_TIMEOUT) {
                    stop();
                }
            }
        }
        while(timeoutThread!=null);
    }
}

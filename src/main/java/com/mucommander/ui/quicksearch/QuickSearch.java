/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains 'quick search' common functionality - selection of rows that match
 * the user's keyboard input.
 * This class is abstract, and should be inherited by subclasses that define 'quick search' 
 * functionality for specific components. 
 * 
 * @author Arik Hadas
 */
public abstract class QuickSearch<T> extends KeyAdapter implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickSearch.class);
	
	/** Quick search string */
    private String searchString;

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
    
    private JComponent component;
    
    protected QuickSearch(JComponent component) {
    	this.component = component;
    	
    	// Listener to key events to start quick search or update search string when it is active
    	component.addKeyListener(this);
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
     * Returns <code>true</code> if the current quick search string matches the given string.
     * Always returns <code>false</code> when the quick search is inactive.
     *
     * @param string the string to test against the quick search string
     * @return true if the current quick search string matches the given string
     */
    public boolean matches(String string) {
        return isActive() && string.toLowerCase().indexOf(searchString.toLowerCase())!=-1;
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

	protected boolean isSearchStringEmpty() {
		return searchString.length() == 0;
	}
	
	protected void removeLastCharacterFromSearchString() {
		// Remove last character from the search string
        // Since the search string has been updated, match information has changed as well
        // and we need to repaint the table.
        // Note that we only repaint if the search string is not empty: if it's empty,
        // the cancel() method will be called, and repainting twice would result in an
        // unpleasant graphical artifact.
        searchString = searchString.substring(0, searchString.length()-1);
        if(searchString.length() != 0)
            component.repaint();
	}
	
	protected void appendCharacterToSearchString(char keyChar) {
		// Update search string with the key that has just been typed
        // Since the search string has been updated, match information has changed as well
        // and we need to repaint the table.
        searchString += keyChar;
        component.repaint();
	}
	
	/**
     * Finds a match (if any) for the current quick search string and selects the corresponding row.
     *
     * @param startRow first row to be tested
     * @param descending specifies whether rows should be tested in ascending or descending order
     * @param findBestMatch if <code>true</code>, all rows will be tested in the specified order, looking for the best match. If not, it will stop to the first match (not necessarily the best).
     */
    protected void findMatch(int startRow, boolean descending, boolean findBestMatch) {
        LOGGER.trace("startRow="+startRow+" descending="+descending+" findMatch="+findBestMatch);

        // If search string is empty, update status bar without any icon and return
        if(searchString.length()==0) {
            searchStringBecameEmpty(searchString);
        }
        else {
        	int bestMatch = getBestMatch(startRow, descending, findBestMatch);

            if (bestMatch != -1)
                matchFound(bestMatch, searchString);
            else
                matchNotFound(searchString);
        }
    }
	
	private int getBestMatch(int startRow, boolean descending, boolean findBestMatch) {
    	String searchStringLC = searchString.toLowerCase();
    	int searchStringLen = searchString.length();
        int startsWithCaseMatch = -1;
        int startsWithNoCaseMatch = -1;
        int containsCaseMatch = -1;
        int containsNoCaseMatch = -1;
        int nbRows = getNumOfItems();

        // Iterate on rows and look the first strings to match one of the following tests,
        // in the following order of importance :
        // - search string matches the beginning of the string with the same case
        // - search string matches the beginning of the string with a different case
        // - string contains search string with the same case
        // - string contains search string with a different case
        for(int i=startRow; descending?i<nbRows:i>=0; i=descending?i+1:i-1) {
            // if findBestMatch was not specified, stop to the first match
            if(!findBestMatch && (startsWithCaseMatch!=-1 || startsWithNoCaseMatch!=-1 || containsCaseMatch!=-1 || containsNoCaseMatch!=-1))
                break;

            String item = getItemString(i);
            int itemLen = item.length();

            // No need to compare strings if quick search string is longer than compared string,
            // they won't match
            if(itemLen<searchStringLen)
                continue;

            // Compare quick search string against
            if (item.startsWith(searchString)) {
                // We've got the best match we could ever have, let's get out of this loop!
                startsWithCaseMatch = i;
                break;
            }

            // If we already have a match on this test case, let's skip to the next string
            if(startsWithNoCaseMatch!=-1)
                continue;

            String itemLC = item.toLowerCase();
            if(itemLC.startsWith(searchStringLC)) {
                // We've got a match, let's see if we can find a better match on the next string
                startsWithNoCaseMatch = i;
            }

            // No need to check if the compared string contains search string if both size are equal,
            // in the case startsWith test yields the same result
            if(itemLen==searchStringLen)
                continue;

            // If we already have a match on this test case, let's skip to the next string
            if(containsCaseMatch!=-1)
                continue;

            if(item.indexOf(searchString)!=-1) {
                // We've got a match, let's see if we can find a better match on the next string
                containsCaseMatch = i;
                continue;
            }

            // If we already have a match on this test case, let's skip to the next string
            if(containsNoCaseMatch!=-1)
                continue;

            if(itemLC.indexOf(searchStringLC)!=-1) {
                // We've got a match, let's see if we can find a better match on the next string
                containsNoCaseMatch = i;
                continue;
            }
        }
    	
        // Determines what the best match is, based on all the matches we found
        int bestMatch = startsWithCaseMatch!=-1?startsWithCaseMatch
            :startsWithNoCaseMatch!=-1?startsWithNoCaseMatch
            :containsCaseMatch!=-1?containsCaseMatch
            :containsNoCaseMatch!=-1?containsNoCaseMatch
            :-1;
        
        LOGGER.trace("startsWithCaseMatch="+startsWithCaseMatch+" containsCaseMatch="+containsCaseMatch+" startsWithNoCaseMatch="+startsWithNoCaseMatch+" containsNoCaseMatch="+containsNoCaseMatch);
        LOGGER.trace("bestMatch="+bestMatch);
        
        return bestMatch;
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
	
	/**
	 * Return number of items to be searched in
	 * 
	 * @return number of items
	 */
	protected abstract int getNumOfItems();
	
	/**
	 * Return item at a given index as String
	 * 
	 * @param index - index of item
	 * @return item at index as String
	 */
	protected abstract String getItemString(int index);
	
	/**
	 * Hook that is called after a search was done for an empty string
	 * 
	 * @param searchString
	 */
	protected abstract void searchStringBecameEmpty(String searchString);
	
	/**
	 * Hook that is called after a search was done and an item was found
	 * 
	 * @param row - the row of the item that was found
	 * @param searchString - the string that was being searched
	 */
	protected abstract void matchFound(int row, String searchString);
	
	/**
	 * Hood that is called after a search was done and no item was found
	 * 
	 * @param searchString - the string that was being searched
	 */
	protected abstract void matchNotFound(String searchString);

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

    ///////////////////////////////
    // KeyAdapter implementation //
    ///////////////////////////////
    
    @Override
    public synchronized void keyReleased(KeyEvent e) {
        // Cancel quick search if backspace key has been pressed and search string is empty.
        // This check is done on key release, so that if backspace key is maintained pressed
        // to remove all the search string, it does not trigger the JComponent's back action 
    	// which is mapped on backspace too
        if(isActive() && e.getKeyCode()==KeyEvent.VK_BACK_SPACE && searchString.equals("")) {
            e.consume();
            stop();
        }
    }
}

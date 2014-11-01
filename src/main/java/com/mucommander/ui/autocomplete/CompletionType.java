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

package com.mucommander.ui.autocomplete;

import com.mucommander.ui.autocomplete.completers.Completer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * AutoCompletionType defines the behaviour of the auto-completion, such as: 
 * - The key(s) that will open the popup window
 * - When should the documentListener be attached to the text component
 * - etc..
 * This abstract class contains the common things to all CompleterType implementations.
 * 
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */

public abstract class CompletionType {
		
	private Completer completer;
	protected AutocompleterTextComponent autocompletedtextComp;
    protected DocumentListener documentListener;
    protected JList list = new JList(); 
    protected JPopupMenu popup = new JPopupMenu();
    protected ShowingThread showingThread;
    
    // Constants:
    protected final int VISIBLE_ROW_COUNT = 10;
    protected final int POPUP_DELAY_AT_TEXT_INSERTION = 1500;
    protected final int POPUP_DELAY_AT_TEXT_DELETION  = 500;
    protected final int POPUP_DELAY_AFTER_ACCEPTING_LIST_ITEM = 1500;
    
    /**
     * ShowingThread is an abstract class for threads that show auto-completion popup
     * window after a given delay.
     * Each implementation of ShowingThread should implements an abstract function
     * "showPopup" that contains the popup opening.
     * 
     * @author Arik Hadas
     */
    protected abstract class ShowingThread extends Thread {
    	
		protected boolean isStopped;
		protected int delayTime;
		
		public ShowingThread(int delayTime) {
			isStopped = false;
			this.delayTime = delayTime;
		}
		
		@Override
        public void run() {
			// Hide the auto-completion popup window.
			hideAutocompletionPopup();
			
			if (!autocompletedtextComp.isShowing() || !autocompletedtextComp.isEnabled())
				return;
			
			// Sleep for delayTime milieconds.
			delay(delayTime);
			
			// If this thread should stop, finish its execution.
			if (isStopped)
				return;
			
			// Show auto-completion popup window.
			showAutocompletionPopup();						
	    }
		
		/**
		 * Stop this thread execution.
		 */
		public void done() {
			isStopped = true;
		}
		
		/**
		 * Cause this thread sleep for the given time (in miliseconds).
		 */
		protected void delay(int miliseconds) {
	    	if (miliseconds > 0) {
				try {
					Thread.sleep(miliseconds);
				} catch (InterruptedException e1) { }
	    	}
	    }
		
		abstract void showAutocompletionPopup();
	}    
    
    public CompletionType(AutocompleterTextComponent comp, Completer completer) {
    	autocompletedtextComp = comp;
    	this.completer = completer;

//        JScrollPane scroll = new JScrollPane(list);
        // Disable horizontal scrolling because they would sometimes appear under Mac OS X even though there was
        // plenty of horizontal space to display the list.
        JScrollPane scroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
 
        list.setFocusable( false );
        popup.setFocusable( false );
        
        scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false ); 
 
        popup.setBorder(BorderFactory.createLineBorder(Color.black)); 
        popup.add(scroll);         
        
        createDocumentListener();

        addMouseListenerToList();
        
        list.setRequestFocusEnabled(false);
        
        autocompletedtextComp.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) { }

			public void focusLost(FocusEvent e) {
				hideAutocompletionPopup();
			}
        });
    }
    
    // abstract methods:
    /**
     * Start a new thread that implement ShowingThread with the given delay.
     */
    protected abstract void startNewShowingThread(int delay);
    
    /**
     * Hide the auto-completion popup window.
     */
    protected abstract void hideAutocompletionPopup();
    
    
    /**
     * update auto-completion popup's list model depending on the data in text component.
     * 
     * @param list - Auto-completion popup's list.
     * @return true if the list was updated successfully, false otherwise. 
     */
    protected boolean updateListData(JList list) {
    	return completer.updateListData(list, autocompletedtextComp);
    }
 
    /**
     * user has selected some item from the auto-completion popup list,
     * update text component accordingly.
     * 
     * @param selected - The selected item from the auto-completion popup's list.
     */
    protected void updateTextComponent(String selected) {
    	completer.updateTextComponent(selected, autocompletedtextComp);
    }
    
    /**
     * createNewShowingThread shows the auto-completion popup window after 
     * a non-blocking delay.
     * 
     * @param delay - The requested delay (in miliseconds) until the popup appear.
     */
    protected void createNewShowingThread(int delay) {
    	// stop current showing thread (if exist)
    	if (showingThread != null)
    		showingThread.done();
    	// start new showing thread
    	startNewShowingThread(delay);
    }
    
    /**
     * The function handles the case when an item of the auto-copmpletion popup was selected.
     * it ask the text component to be updated according to the selected item and create
     * a new thread that will open auto-completion popup windos after delay of
     * POPUP_DELAY_AFTER_ACCEPTING_LIST_ITEM seconds. 
     */
    protected void acceptListItem(String selected) {
    	updateTextComponent(selected);
    	createNewShowingThread(POPUP_DELAY_AFTER_ACCEPTING_LIST_ITEM);
    }    
    
    private void addMouseListenerToList() {
    	list.addMouseListener(new MouseListener() {
        		
			public void mouseClicked(MouseEvent e) {
				// If there was double click on item of the popup's list, 
				// select it, and update the text component.
				if (e.getClickCount() == 2) {
		             int index = list.locationToIndex(e.getPoint());
		             list.setSelectedIndex(index);
		             acceptListItem((String) list.getSelectedValue());
				}
			}

			public void mouseReleased(MouseEvent e) {}
			
			public void mouseEntered(MouseEvent e) {}

			public void mouseExited(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
        });
    }
    
    private void createDocumentListener() {    	
	    documentListener = new DocumentListener(){ 
	        public void insertUpdate(DocumentEvent e){
	        	// If text was inserted to the text component and carent is at the end of 
	        	// the text, then start a showingThread to open auto-completion popup.
	        	if (autocompletedtextComp.isCarentAtEndOfTextAtInsertion())
        			createNewShowingThread(popup.isVisible() ? 0 : POPUP_DELAY_AT_TEXT_INSERTION);	        	
	        }
	 
	        public void removeUpdate(DocumentEvent e){
	        	// If text was deleted from the text component and carent is at the end of 
	        	// the text, then start a showingThread to open auto-completion popup.
	        	if (autocompletedtextComp.isCarentAtEndOfTextAtRemoval())
	        		createNewShowingThread(popup.isVisible() ? 0 : POPUP_DELAY_AT_TEXT_DELETION); 
	        } 

	        public void changedUpdate(DocumentEvent e){ } 
	    };
    }
    
    /** 
     * Returns true if the auto-completion popup window is visible.
     */
    protected boolean isPopupListShowing() {
    	return popup.isShowing();
    }
    
    /** 
     * Returns true if there is a selected item at the auto-completion popup window.
     */
    protected boolean isItemSelectedAtPopupList() {
    	return popup.isShowing() && list.getSelectedIndex() >= 0;
    }

    /** 
     * Selects the first item in the list. 
     */
    protected void selectFirstValue() {
    	list.setSelectedIndex(0); 
        list.ensureIndexIsVisible(0);
    }
    
    /** 
     * Selects the last item in the list. 
     */
    protected void selectLastValue() {
    	int lastIndex = list.getModel().getSize() - 1;
    	
    	list.setSelectedIndex(lastIndex); 
        list.ensureIndexIsVisible(lastIndex);
    }
    
    /** 
     * Selects the item at (VISIBLE_ROW_COUNT - 1) places after the currently
     * selected item in the list. 
     */
    protected void selectNextPage() {
    	int si = list.getSelectedIndex();
    	
    	int nextIndex = 0;
    	if (si >= 0)
    		nextIndex = Math.min(si + VISIBLE_ROW_COUNT - 1, list.getModel().getSize() - 1);
    	
    	list.setSelectedIndex(nextIndex); 
        list.ensureIndexIsVisible(nextIndex); 
    }
    
    /** 
     * Selects the item at (VISIBLE_ROW_COUNT - 1) places before the currently
     * selected item in the list. 
     */
    protected void selectPreviousPage() {
    	int si = list.getSelectedIndex(); 
    	 
        if(si > 0){ 
        	int nextIndex = Math.max(si - (VISIBLE_ROW_COUNT - 1), 0);
            list.setSelectedIndex(nextIndex); 
            list.ensureIndexIsVisible(nextIndex); 
        } 
    }
    
    /** 
     * Selects the next item in the list.  It won't change the selection if the 
     * currently selected item is already the last item. 
     */ 
    protected void selectNextPossibleValue(){ 
        int si = list.getSelectedIndex(); 
 
        if(si < list.getModel().getSize() - 1){
        	int nextIndex = si + 1;
            list.setSelectedIndex(nextIndex); 
            list.ensureIndexIsVisible(nextIndex); 
        } 
    } 
 
    /** 
     * Selects the previous item in the list.  It won't change the selection if the 
     * currently selected item is already the first item. 
     */ 
    protected void selectPreviousPossibleValue(){ 
        int si = list.getSelectedIndex(); 
 
        if(si > 0){ 
        	int nextIndex = si - 1;
            list.setSelectedIndex(nextIndex); 
            list.ensureIndexIsVisible(nextIndex); 
        } 
    }
}

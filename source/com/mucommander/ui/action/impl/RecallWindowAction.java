/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.action.impl;

import com.mucommander.AppLogger;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.KeyStroke;

/**
 * Brings a MainFrame window to the front. The window number must be specified in the
 * {@link #WINDOW_NUMBER_PROPERTY_KEY} property, and must exist (i.e. must refer to an existing window number).
 *
 * @see com.mucommander.ui.main.WindowManager
 * @author Maxence Bernard
 */
public class RecallWindowAction extends MuAction implements PropertyChangeListener {

    public final static String WINDOW_NUMBER_PROPERTY_KEY = "window_number";
    

    public RecallWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Set label in case the window number was set in the initial properties
        int windowNumber = getWindowNumber();
        if(windowNumber!=-1)
            updateLabel(windowNumber);

        // Listen to window number property change
        addPropertyChangeListener(this);
    }


    public void performAction() {
        Vector mainFrames = WindowManager.getMainFrames();

        // Checks that the window number currently exists
        int windowNumber = getWindowNumber();
        if(windowNumber<=0 || windowNumber>mainFrames.size()) {
            AppLogger.fine("Specified window does not exist: "+getValue(WINDOW_NUMBER_PROPERTY_KEY));
            return;
        }

        // Brings the MainFrame to front
        ((MainFrame)mainFrames.elementAt(windowNumber-1)).toFront();
    }


    /**
     * Returns the window number contained by the {@link #WINDOW_NUMBER_PROPERTY_KEY} property or -1 if the property
     * doesn't contain any value, or a value that cannot be parsed as an int.
     *
     * @return the window number's property value or -1 if the property doesn't contain any value, or a value that cannot be parsed as an int.
     */
    private int getWindowNumber() {
        try {
            Object windowNumberValue = getValue(WINDOW_NUMBER_PROPERTY_KEY);
            if(windowNumberValue==null || !(windowNumberValue instanceof String))
                return -1;
            
            return Integer.parseInt((String)windowNumberValue);
        }
        catch(Exception e) {
            return -1;
        }
    }


    /**
     * Updates the label using the given window number.
     *
     * @param windowNumber the window number to be used in the label
     */
    private void updateLabel(int windowNumber) {
        // Update the action's label
        setLabel(Translator.get(getStandardLabelKey(), ""+windowNumber));
    }


    ///////////////////////////////////////////
    // PropertyChangeListener implementation //
    ///////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if(propertyChangeEvent.getPropertyName().equals(WINDOW_NUMBER_PROPERTY_KEY)) {
            int windowNumber = getWindowNumber();
            if(windowNumber==-1) {
                AppLogger.fine("Invalid "+WINDOW_NUMBER_PROPERTY_KEY+" property="+getValue(WINDOW_NUMBER_PROPERTY_KEY));
            }
            else {
                updateLabel(windowNumber);
            }
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new RecallWindowAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "RecallWindow";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return null; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}

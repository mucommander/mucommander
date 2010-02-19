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

package com.mucommander.ui.action.impl;

import com.mucommander.AppLogger;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Brings a {@link }MainFrame} window to the front. This action operates on a specific window number specified in the
 * constructor, either as a constructor parameter, or in the {@link #WINDOW_NUMBER_PROPERTY_KEY} property.
 *
 * @see com.mucommander.ui.main.WindowManager
 * @author Maxence Bernard
 */
public class RecallWindowAction extends MuAction {

    /** Window number this action operates on */
    private int windowNumber;

    /** Key of the property that holds the window number */
    public final static String WINDOW_NUMBER_PROPERTY_KEY = "window_number";


    public RecallWindowAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        Object windowNumberValue = getValue(WINDOW_NUMBER_PROPERTY_KEY);
        if(windowNumberValue==null || !(windowNumberValue instanceof String))
            throw new IllegalArgumentException(WINDOW_NUMBER_PROPERTY_KEY+" ("+windowNumberValue+")");

        windowNumber = Integer.parseInt((String)windowNumberValue);

        if(windowNumber<=0)
            throw new IllegalArgumentException(WINDOW_NUMBER_PROPERTY_KEY+" ("+windowNumberValue+")");
    }

    public RecallWindowAction(MainFrame mainFrame, Hashtable<String,Object> properties, int windowNumber) {
        super(mainFrame, properties);

        this.windowNumber = windowNumber;
        if(windowNumber<=0)
            throw new IllegalArgumentException("windowNumber ("+windowNumber+")");
    }

    @Override
    public void performAction() {
        Vector<MainFrame> mainFrames = WindowManager.getMainFrames();

        // Checks that the window number currently exists
        if(windowNumber<=0 || windowNumber>mainFrames.size()) {
            AppLogger.fine("Window number "+windowNumber+" does not exist");
            return;
        }

        // Brings the MainFrame to front
        mainFrames.elementAt(windowNumber-1).toFront();
    }

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new RecallWindowAction(mainFrame, properties);
		}
    }

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "RecallWindow";

        private int windowNumber;

        public Descriptor() {
            this(-1);
        }

        protected Descriptor(int windowNumber) {
            this.windowNumber = windowNumber;
        }

		public String getId() { return ACTION_ID+(windowNumber==-1?"":""+windowNumber); }

		public ActionCategory getCategory() { return ActionCategories.WINDOW; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() {
            if(windowNumber<=0 || windowNumber>10)
                return null;

            return KeyStroke.getKeyStroke(Character.forDigit(windowNumber==10 ? 0 : windowNumber, 10), KeyEvent.CTRL_DOWN_MASK);
        }

        @Override
        public String getLabel() {
            return Translator.get(getLabelKey(), windowNumber==-1?"?":""+windowNumber);
        }

        @Override
        public boolean isParameterized() {
            return windowNumber==-1;
        }
    }
}

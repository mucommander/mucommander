/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * Brings a {@link MainFrame} window to the front. This action operates on a specific window number specified in the
 * constructor, either as a constructor parameter, or in the {@link #WINDOW_NUMBER_PROPERTY_KEY} property.
 *
 * @see com.mucommander.ui.main.WindowManager
 * @author Maxence Bernard
 */
public class RecallWindowAction extends MuAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(RecallWindowAction.class);
	
    /** Window number this action operates on */
    private int windowNumber;

    /** Key of the property that holds the window number */
    public final static String WINDOW_NUMBER_PROPERTY_KEY = "window_number";


    public RecallWindowAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        Object windowNumberValue = getValue(WINDOW_NUMBER_PROPERTY_KEY);
        if(windowNumberValue==null || !(windowNumberValue instanceof String))
            throw new IllegalArgumentException(WINDOW_NUMBER_PROPERTY_KEY+" ("+windowNumberValue+")");

        windowNumber = Integer.parseInt((String)windowNumberValue);

        if(windowNumber<=0)
            throw new IllegalArgumentException(WINDOW_NUMBER_PROPERTY_KEY+" ("+windowNumberValue+")");
    }

    public RecallWindowAction(MainFrame mainFrame, Map<String,Object> properties, int windowNumber) {
        super(mainFrame, properties);

        this.windowNumber = windowNumber;
        if(windowNumber<=0)
            throw new IllegalArgumentException("windowNumber ("+windowNumber+")");
    }

    @Override
    public void performAction() {
        List<MainFrame> mainFrames = WindowManager.getMainFrames();

        // Checks that the window number currently exists
        if(windowNumber<=0 || windowNumber>mainFrames.size()) {
            LOGGER.debug("Window number "+windowNumber+" does not exist");
            return;
        }

        // Brings the MainFrame to front
        mainFrames.get(windowNumber-1).toFront();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
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

		public ActionCategory getCategory() { return ActionCategory.WINDOW; }

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

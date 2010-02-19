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

package com.mucommander.ui.button;

import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.impl.GoToDocumentationAction;
import com.mucommander.ui.main.MainFrame;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.util.Hashtable;

/**
 * This is a contextual 'Help' button to be used wherever help is available or needed. When clicked, it opens
 * muCommander's online documentation in the system's default browser. If a specified help topic is passed to the
 * constructor, the browser will open the said topic. If no topic is specified, the base documentation URL will be opened.
 *
 * <p>Unless explicitely set, this button has a standard help icon but no text label. A tooltip is displayed when
 * hovering over the button.</p>
 *
 * @see com.mucommander.ui.action.impl.GoToDocumentationAction
 * @author Maxence Bernard
 */
public class HelpButton extends JButton {

    /**
     * Creates a new HelpButton with no initial topic.
     *
     * @param mainFrame the MainFrame this button is associated with
     */
    public HelpButton(MainFrame mainFrame) {
        this(mainFrame, null);
    }

    /**
     * Creates a new HelpButton with the specified topic.
     *
     * @param mainFrame the MainFrame this button is associated with
     * @param helpTopic the help topic this button will open when clicked, <code>null</code> to open the base documentation URL
     */
    public HelpButton(MainFrame mainFrame, String helpTopic) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();

        GoToDocumentationAction action = new GoToDocumentationAction(mainFrame, properties);
        setAction(action);

        if(helpTopic!=null)
            setHelpTopic(helpTopic);

        // Note: the button's text and icon must be set after the action otherwise they'll be replaced by the action's

        // Remove the action's label from this button's text
        setText(null);

        // Use the action's label as a tooltip
        setToolTipText(action.getLabel());

        if(OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher()) {
            // If running Mac OS X 10.5 (and up), use the special client property to have a standard help button.
            putClientProperty("JButton.buttonType", "help");

            // Remove the action's icon
            setIcon(null);
        }
        else {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        setFocusable(false);
    }

    /**
     * Returns the help topic this button will open when clicked, <code>null</code> if there is none.
     *
     * @return the help topic this button will open when clicked, <code>null</code> if there is none
     */
    public String getHelpTopic() {
        return (String)getAction().getValue(GoToDocumentationAction.TOPIC_PROPERTY_KEY);
    }
    
    /**
     * Sets the help topic this button will open when clicked, <code>null</code> to open the base documentation URL.
     *
     * @param helpTopic the help topic this button will open when clicked, <code>null</code> to open the base documentation URL
     */
    public void setHelpTopic(String helpTopic) {
        getAction().putValue(GoToDocumentationAction.TOPIC_PROPERTY_KEY, helpTopic);
    }
}

/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.dialog.pref;

import com.mucommander.conf.ConfigurationManager;

import javax.swing.*;

/**
 * Abstract preferences panel.
 * @author Maxence Bernard
 */
public abstract class PreferencesPanel extends JPanel {
    protected PreferencesDialog parent;
    protected String            title;
	
    public PreferencesPanel(PreferencesDialog parent, String title) {
        this.title  = title;
        this.parent = parent;
    }

    public String getTitle() {return title;}
	
	
    /**
     * Convenience method, returns a configuration variable's value.
     */
    protected static String getPref(String variable) {
        String val = ConfigurationManager.getVariable(variable);
		
        // Replace null values by empty string to avoid having to check for null values 
        if(val==null)
            val = "";
		
        return val;	
    }

    /**
     * Convenience method, returns a configuration variable's value using the provided default value
     * if it didn't have any value.
     */
    protected static String getPref(String variable, String defaultValue) {return ConfigurationManager.getVariable(variable, defaultValue);}

    /**
     * Convenience method, sets a configuration variable's value and return true if the variable has been changed.
     */
    protected static boolean setPref(String variable, String value) {return ConfigurationManager.setVariable(variable, value);}

    /**
     * This method is called by PreferencesDialog after the user pressed 'OK'
     * to save new preferences.
     */
    protected abstract void commit();
    protected boolean checkCommit() {return true;}
}

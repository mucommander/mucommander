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

import javax.swing.*;

/**
 * Abstract preferences panel.
 * @author Maxence Bernard
 */
public abstract class PreferencesPanel extends JPanel {
    /** Preferences dialog that contains this panel. */
    protected PreferencesDialog parent;
    /** Panel's title. */
    protected String            title;

    /**
     * Creates a new preferences panel.
     * @param parent dialog that contains this panel.
     * @param title  panel's title.
     */
    public PreferencesPanel(PreferencesDialog parent, String title) {
        this.title  = title;
        this.parent = parent;
    }

    /**
     * Returns the panel's title.
     * @return the panel's title.
     */
    public String getTitle() {return title;}
	
    /**
     * This method is called by PreferencesDialog after the user pressed 'OK'
     * to save new preferences.
     */
    protected abstract void commit();

    /**
     * Checks whether this panel's data can be commited or whether it contains an error.
     * @return <code>true</code> if the panel's data can be commited, <code>false</code> otherwise.
     */
    protected boolean checkCommit() {return true;}
}

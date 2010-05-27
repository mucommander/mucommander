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

package com.mucommander.ui.dialog.pref.component;

import com.mucommander.ui.dialog.pref.PreferencesDialog;

/**
 * Interface for components in preferences panels in which changes can be trackered.
 * 
 * @author Arik Hadas
 */
public interface PrefComponent {
	
	/**
	 * 
	 * @param dialog - parent dialog of the component parent panel.
	 */
	public void addDialogListener(final PreferencesDialog dialog);
	
	/**
	 * This function checks if the component's value was changed from the value that is saved
	 * in MuConfiguration.
	 * 
	 * @return true if component's value differ from the value at MuConfiguration, else otherwise.
	 */
	public boolean hasChanged();
}

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

package com.mucommander.ui.action;

import com.mucommander.text.Translator;

/**
 * ActionCategory represent category that can be associated with MuAction action.
 * 
 * @author Arik Hadas
 */
public class ActionCategory implements Comparable<ActionCategory> {
	
	/** The category's label key in the dictionary file */
	private String descriptionKey;
	 
	protected ActionCategory(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}

	public String getDescriptionKey() { return descriptionKey; } 

	public String getDescription() { return Translator.get(descriptionKey); }
	
	public boolean contains(String actionId) {
		ActionCategory actionCategory = ActionProperties.getActionCategory(actionId);
		if (actionCategory != null)
			return descriptionKey.equals(actionCategory.getDescriptionKey());
		return false;
	}
	
	public String toString() { 
		String description = getDescription();
		if (description != null)
			return description;
		return getDescriptionKey();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ActionCategory)
			return descriptionKey.equals(((ActionCategory) obj).descriptionKey);
		return false;
	}

	public int compareTo(ActionCategory actionCategory) {
        return getDescription().compareTo(actionCategory.getDescription());
	}
}

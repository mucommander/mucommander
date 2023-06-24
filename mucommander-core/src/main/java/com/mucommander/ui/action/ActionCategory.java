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

package com.mucommander.ui.action;

import com.mucommander.text.Translator;

/**
 * ActionCategory represent category that can be associated with MuAction action.
 * 
 * @author Arik Hadas
 */
public enum ActionCategory {
	ALL("action_categories.all") {
		@Override
        public boolean contains(ActionId actionId) {
            return true;
		}
	},
	NAVIGATION("action_categories.navigation"),
	SELECTION("action_categories.selection"),
	VIEW("action_categories.view"),
	FILES("action_categories.file_operations"),
	WINDOW("action_categories.windows"),
	TAB("action_categories.tabs"),
	MISC("action_categories.misc"),
	COMMANDS("action_categories.commands"),
	TERMINAL("action_categories.terminal"),
	;

	/** The category's label key in the dictionary file */
	private String descriptionKey;
	 
	ActionCategory(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}

	public String getDescriptionKey() { return descriptionKey; } 

	public String getDescription() { return Translator.get(descriptionKey); }
	
	public boolean contains(ActionId actionId) {
		ActionCategory actionCategory = ActionProperties.getActionCategory(actionId);
		return actionCategory != null && descriptionKey.equals(actionCategory.getDescriptionKey());
	}

	@Override
	public String toString() { 
		String description = getDescription();
		return description != null ? description : getDescriptionKey();
	}
}

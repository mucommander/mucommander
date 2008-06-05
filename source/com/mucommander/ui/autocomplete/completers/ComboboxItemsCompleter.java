/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.autocomplete.completers;

import com.mucommander.ui.autocomplete.AutocompleterEditableCombobox;
import com.mucommander.ui.autocomplete.AutocompleterTextComponent;
import com.mucommander.ui.autocomplete.Completer;
import com.mucommander.ui.autocomplete.PrefixFilter;

import javax.swing.*;
import java.util.Vector;

/**
 * ComboboxItemsCompleter is a Completer based on the items of combo-box.
 * 
 * @author Arik Hadas
 */

public class ComboboxItemsCompleter implements Completer {

	public ComboboxItemsCompleter() {	}

	public boolean updateListData(final JList list, AutocompleterTextComponent comp) {
		if (!(comp instanceof AutocompleterTextComponent))
			return false;
		
		String[] items = ((AutocompleterEditableCombobox) comp).getItemsNames();
		
		final String value = comp.getText();
    	
    	Vector filteredFiles = new Vector();
    	
    	PrefixFilter filter = PrefixFilter.createPrefixFilter(value);
    	
    	filteredFiles.addAll(filter.filter(items));    	
    	
    	if (filteredFiles.size() == 1)
			filteredFiles.remove(value);
    	
    	list.setListData(filteredFiles);
    	
		return true;
	}
	
	public void updateTextComponent(final String selected, AutocompleterTextComponent comp) {
		comp.setText(selected);
	}
}

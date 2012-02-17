/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.ui.autocomplete.AutocompleterTextComponent;
import com.mucommander.ui.autocomplete.completers.services.PrefixFilter;

import java.util.Vector;

/**
 * ComboboxOptionsCompleter is a Completer based on the items of combo-box.
 * 
 * @author Arik Hadas
 */

public class ComboboxOptionsCompleter extends Completer {

	public ComboboxOptionsCompleter() {	}

	@Override
    protected Vector<String> getUpdatedSuggestions(AutocompleterTextComponent component) {
    	return PrefixFilter.createPrefixFilter(component.getText()).filter(component.getItemNames());
	}
	
	@Override
    public void updateTextComponent(final String selected, AutocompleterTextComponent comp) {
		comp.setText(selected);
	}
}

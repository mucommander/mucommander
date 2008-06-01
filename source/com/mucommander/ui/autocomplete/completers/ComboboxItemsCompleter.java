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

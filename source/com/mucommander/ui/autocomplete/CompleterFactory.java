package com.mucommander.ui.autocomplete;

import com.mucommander.ui.autocomplete.completers.ComboboxItemsCompleter;
import com.mucommander.ui.autocomplete.completers.FileCompleter;
import com.mucommander.ui.autocomplete.completers.LocationCompleter;

public class CompleterFactory {
	public static Completer getComboboxOptionsCompleter() {
		return new ComboboxItemsCompleter();
	}
	
	public static Completer getFileCompleter() {
		return new FileCompleter();
	}
	
	public static Completer getLocationCompleter() {
		return new LocationCompleter();
	}
}

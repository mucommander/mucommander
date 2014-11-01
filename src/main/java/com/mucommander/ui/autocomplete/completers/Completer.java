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

import com.mucommander.commons.file.FileURL;
import com.mucommander.ui.autocomplete.AutocompleterTextComponent;
import com.mucommander.ui.autocomplete.completers.services.CompletionService;

import javax.swing.*;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Interface that each type of completion must implement.
 * It defines 2 methods:
 * * <ul>
 *   <li>updateListData - Update what the auto-completion popup shows, depending on the data in text component</li>
 *   <li>updateTextComponent - Update the text component's text according to the selected item from the auto-completion popup</li>
 * </ul>
 *  
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */

public abstract class Completer {
	private Set<CompletionService> services;
	
	public Completer() {
		services = new LinkedHashSet<CompletionService>();
	}
	
	/**
	 * This function gets an AutocompleterTextComponent and returns a Vector of suggestions for
	 * completion, for the component's value.
	 * 
	 * @param component - an AutocompleterTextComponent.
	 * @return Vector of suggestions for completion.
	 */
	protected abstract Vector<String> getUpdatedSuggestions(AutocompleterTextComponent component);
    
	/**
	 * update list model depending on the data in text component
	 * 
	 * @param list - auto-completion popup's list that should be updated.
	 * @param comp - text component
	 * @return true if an auto-completion popup with the updated list should be shown, false otherwise.
	 */
    public boolean updateListData(final JList list, AutocompleterTextComponent comp) {
    	list.setListData(getUpdatedSuggestions(comp));

    	if (list.getModel().getSize() == 1) {
    		try {
				String typedFilename = FileURL.getFileURL(comp.getText()).getFilename();

				// in case the suggestions-list contains only one suggestion and it 
				// match the typed path - do not show an auto-completion popup.
				if (typedFilename==null || typedFilename.equalsIgnoreCase((String) list.getModel().getElementAt(0)))
					return false;
			} catch (MalformedURLException e) { }
    	}
    	
    	return list.getModel().getSize() > 0;
    }
 
    /**
     * update text component according to the given string.
     * 
     * @param selected - selected item from auto-completion popup list.
     * @param comp - text component.
     */
    public abstract void updateTextComponent(final String selected, AutocompleterTextComponent comp);
    
    /**
	 * Add service to this completer.
	 * 
	 * The order in which the services is being registered is important, 
	 * see: <code>tryToCompleteFromServices<code>.
	 * 
	 * @param service - Service to be added. 
	 */
	protected void registerService(CompletionService service) {
		services.add(service);
	}
	
	/**
	 * Gather the possible completions for the given path from
	 * all the services registered to this completer.
	 * 
	 * @param path - The path to be completed.
	 * @return Vector that contain all the possible completions
	 * 			which were retured from the registered services.
	 */
	protected Vector<String> getPossibleCompletionsFromServices(String path) {
		Vector<String> result = new Vector<String>();
        for (CompletionService service : services)
            result.addAll(service.getPossibleCompletions(path));
		return result;
	}
	
	/**
	 * Given the selected string (from the auto-completion's list), try to 
	 * get a completion from the registered services.
	 * 
	 * The first completion that found will be returned, thus the order in which
	 * the services are registered is significant. 
	 * 
	 * @param selectedString - selected string (from the auto-completion's list).
	 * @return null if could not found any completion for the given string 
	 * from the registered services, otherwise the founded completion is returned.
	 */
	protected String tryToCompleteFromServices(String selectedString) {
		String location = null;
        for (CompletionService service : services)
            if ((location = (service).complete(selectedString)) != null)
                break;
		return location;
	}
}

package com.mucommander.ui.autocomplete;

import javax.swing.*;

/**
 * Interface that each implementation of completion must implement.
 * It defines 2 methods:
 * * <ul>
 *   <li>updateListData - Update what the auto-completion popup shows, depending on the data in text component</li>
 *   <li>updateTextComponent - Update the text component's text according to the selected item from the auto-completion popup</li>
 * </ul>
 *  
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */

public interface Completer {
	/**
	 * update list model depending on the data in text component
	 * 
	 * @param list - auto-completion popup's list.
	 * @param comp - text component
	 * @return true if list model was updated successfully, false otherwise.
	 */
    public boolean updateListData(final JList list, AutocompleterTextComponent comp); 
 
    /**
     * update text component according to the given string.
     * 
     * @param selected - selected item from auto-completion popup list.
     * @param comp - text component.
     */
    public void updateTextComponent(final String selected, AutocompleterTextComponent comp);
}

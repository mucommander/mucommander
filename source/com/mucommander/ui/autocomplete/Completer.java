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

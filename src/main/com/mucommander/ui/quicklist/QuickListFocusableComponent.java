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

package com.mucommander.ui.quicklist;

import com.mucommander.ui.main.table.FileTable;

/**
 * Components which request focus in this package should implement this interface,
 * and return their invoker FileTable which is the next component, after them,
 * in the focus traversal. 
 * 
 * @author Arik Hadas
 */

public interface QuickListFocusableComponent {
	
	/**
	 * This function should return the invoker FileTable which is the next component
	 * in the focus traversal.
	 * 
	 * @return invoker FileTable
	 */
	public FileTable getInvokerFileTable();
}

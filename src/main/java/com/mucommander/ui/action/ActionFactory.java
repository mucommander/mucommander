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

package com.mucommander.ui.action;

import com.mucommander.ui.main.MainFrame;

import java.util.Map;

/**
 *  Each MuAction's factory should implement this interface.
 *
 * @author Arik Hadas
 */
public interface ActionFactory {
	
	/**
	 * This is an initiation method that returns an instance of MuAction subclass.
	 * 
	 * @param mainFrame - MainFrame.
	 * @param properties - a hashtable of arguments for the action. 
	 * @return an instance of MuAction subclass.
	 */
	public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties);
}

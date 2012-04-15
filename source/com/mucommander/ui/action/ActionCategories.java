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

package com.mucommander.ui.action;

/**
 * This class contains instances of MuAction's categories.
 * 
 * @author Arik Hadas
 */
public class ActionCategories {
	public static final ActionCategory ALL        = new AllCategory();
	public static final ActionCategory NAVIGATION = new ActionCategory("action_categories" + ".navigation");
	public static final ActionCategory SELECTION  = new ActionCategory("action_categories" + ".selection");
	public static final ActionCategory VIEW       = new ActionCategory("action_categories" + ".view");
	public static final ActionCategory FILES      = new ActionCategory("action_categories" + ".file_operations");
	public static final ActionCategory WINDOW     = new ActionCategory("action_categories" + ".windows");
	public static final ActionCategory MISC       = new ActionCategory("action_categories" + ".misc");
	
	/**
	 * Category that contains all actions.
	 */
	private static class AllCategory extends ActionCategory {

		protected AllCategory() {
			super("action_categories" + ".all");
		}
		
		@Override
        public boolean contains(String actionId) {
			return true;
		}
	}
}

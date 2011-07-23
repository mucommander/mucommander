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

package com.mucommander.ui.main.tabs;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.tabs.Tab;

/**
 * Properties of a presented tab.
 *
 * @author Arik Hadas
 */
public class FileTableTab implements Tab {

	/** The location presented in this tab */
     private AbstractFile location;
     
// 	 private boolean isLocked;

     /**
      * Factory method that validates the input before initiating FileTableTab instance
      * 
      * @param location - the location that would be presented in the tab
      */
 	 public static FileTableTab create(AbstractFile location) {
 		 if (location == null)
 			 throw new RuntimeException("Invalid location");
 		 
 		 return new FileTableTab(location);
 	 }
 	 
 	 /**
 	  * Private constructor
 	  * 
 	  * @param location - the location that would be presented in the tab
 	  */
	 private FileTableTab(AbstractFile location) {
		 setLocation(location);
	 }

	 public void setLocation(AbstractFile location) {
		 this.location = location;
	 }

	 public AbstractFile getLocation() {
		 return location;
	 }
	 
	 @Override
	 public boolean equals(Object obj) {
		 if (obj instanceof FileTableTab)
			 return location.getAbsolutePath().equals(((FileTableTab) obj).getLocation().getAbsolutePath());
		 return false;
	 }
	 
	 @Override
	 public int hashCode() {
	    return location.hashCode();
	}
}

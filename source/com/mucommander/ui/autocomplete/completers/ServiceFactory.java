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

package com.mucommander.ui.autocomplete.completers;

import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.ui.autocomplete.completers.services.*;

/**
 * A factory class to produce completion-services.
 * 
 * @author Arik Hadas
 */

public class ServiceFactory {
	
	public static CompletionService getAllFilesService() {
		return new AllFilesService();
	}
	
	public static CompletionService getBrowsableFilesService() {
		return new FilteredFilesService(new AttributeFileFilter(AttributeFileFilter.BROWSABLE));
	}
	
	public static CompletionService getVolumesService() {
		return new VolumesService();
	}
	
	public static CompletionService getBookmarksService() {
		return new BookmarksService();
	}
	
	public static CompletionService getSystemVariablesService() {
		return new SystemVariablesService();
	}
}

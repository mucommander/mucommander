/*
 * This file is part of muCommander, http://www.mucommander.com
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

package dev.barebones.commander.ui.autocomplete.completers;

import dev.barebones.commander.commons.file.filter.AttributeFileFilter;
import dev.barebones.commander.commons.file.filter.AttributeFileFilter.FileAttribute;
import dev.barebones.commander.ui.autocomplete.completers.services.AllFilesService;
import dev.barebones.commander.ui.autocomplete.completers.services.BookmarksService;
import dev.barebones.commander.ui.autocomplete.completers.services.CompletionService;
import dev.barebones.commander.ui.autocomplete.completers.services.FilteredFilesService;
import dev.barebones.commander.ui.autocomplete.completers.services.SystemVariablesService;
import dev.barebones.commander.ui.autocomplete.completers.services.VolumesService;

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
        return new FilteredFilesService(new AttributeFileFilter(FileAttribute.BROWSABLE));
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

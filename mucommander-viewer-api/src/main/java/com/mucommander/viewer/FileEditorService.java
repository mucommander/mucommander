/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.viewer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for file editor service.
 */
@ParametersAreNonnullByDefault
public interface FileEditorService extends FileOpenService {

    /**
     * Returns a new instance of {@link FileEditor}.
     *
     * @param fromSearchWithContent whether opened file is from File Search with Content
     * @return a new instance of {@link FileEditor}.
     */
    @Nonnull
    FileEditor createFileEditor(boolean fromSearchWithContent);
}

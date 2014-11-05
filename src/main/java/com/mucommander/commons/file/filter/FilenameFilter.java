/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * <code>FilenameFilter</code> is a {@link FileFilter} that operates on filenames.
 *
 * <p>A <code>FilenameFilter</code> can be passed to {@link AbstractFile#ls(FilenameFilter)} to filter out some of the
 * files contained by a folder without creating the associated <code>AbstractFile</code> instances.</p>
 *
 * @see AbstractFilenameFilter
 * @author Maxence Bernard
 */
public interface FilenameFilter extends StringCriterionFilter {
}

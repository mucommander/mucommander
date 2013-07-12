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

package com.mucommander.ui.main;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.AbstractFileFilter;

/**
 * Filter used to filter out system files and folders that should not be displayed to inexperienced users.
 *
 * <p>At the moment, this filter only supports Mac OS X top-level system folders (those hidden by Finder)
 * and thus this filter should only be used under Mac OS X.
 *
 * @author Maxence Bernard
 */
public class SystemFileFilter extends AbstractFileFilter {

    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return file.isSystem();
    }
}

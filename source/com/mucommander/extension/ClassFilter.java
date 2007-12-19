/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.extension;

/**
 * Used to filter classes.
 * <p>
 * <code>ClassFilter</code> implementations are meant to be used in conjonction with {@link ClassFinder}.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ClassFilter {
    /**
     * Returns <code>true</code> if the specified class must be used.
     * @return <code>true</code> if the specified class must be used, <code>false</code> otherwise.
     */
    public boolean accept(Class c);
}

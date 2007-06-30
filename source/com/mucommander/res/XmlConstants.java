/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.res;

/**
 * Describes the format of a resource list description file.
 * @author Nicolas Rinaudo
 */
interface XmlConstants {
    /** Label of the XML root element. */
    public static final String ROOT_ELEMENT   = "res";
    /** Label of file elements. */
    public static final String FILE_ELEMENT   = "file";
    /** Label of the path attribute. */
    public static final String PATH_ATTRIBUTE = "path";
}

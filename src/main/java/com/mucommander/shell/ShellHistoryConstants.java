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

package com.mucommander.shell;

/**
 * Defines the structure of the shell history XML file.
 * @author Nicolas Rinaudo
 */
interface ShellHistoryConstants {
    /** Name of the XML file's root element. */
    static final String ROOT_ELEMENT    = "history";
    /** Name of a command element in the XML file. */
    static final String COMMAND_ELEMENT = "command";
    /** Name of the root element's attribute containing the muCommander version that was used to create the shell history file */
    static final String ATTRIBUTE_VERSION      = "version";
}

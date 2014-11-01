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

package com.mucommander.command;

/**
 * Defines the structure of a custom commands XML file.
 * <p>
 * This interface is only meant as a convenient way of sharing the XML
 * file format between the {@link com.mucommander.command.CommandWriter}
 * and {@link CommandReader}. It will be removed
 * at bytecode optimisation time.
 * </p>
 * <p>
 * Commands XML files must match the following DTD:
 * <pre>
 * &lt;!ELEMENT commands (association*)&gt;
 * 
 * &lt;!ELEMENT command EMPTY&gt;
 * &lt;!ATTLIST command value   CDATA              #REQUIRED&gt;
 * &lt;!ATTLIST command alias   CDATA              #REQUIRED&gt;
 * &lt;!ATTLIST command type    (system|invisible) #IMPLIED&gt;
 * &lt;!ATTLIST command display CDATA              #IMPLIED&gt;
 * </pre>
 * Where:
 * <ul>
 *  <li><i>value</i> is the command's value, in a format that can be understood by the {@link CommandReader}.</li>
 *  <li><i>alias</i> is the name under which the command will be known throughout muCommander.</li>
 *  <li><i>type</i> is the command's type (<i>system</i>, <i>invisible</i> or <i>normal</i>). See {@link Command} for more information.</li>
 * </ul>
 * </p>
 * @see CommandReader
 * @see CommandWriter
 * @author Nicolas Rinaudo
 */
interface CommandsXmlConstants {
    // - XML elements ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Root element. */
    public static final String ELEMENT_ROOT     = "commands";
    /** Custom command definition element. */
    public static final String ELEMENT_COMMAND  = "command";



    // - Custom command structure ----------------------------------------------
    // -------------------------------------------------------------------------
    /** Name of the attribute containing a command's display name. */
    public static final String ATTRIBUTE_DISPLAY = "display";
    /** Name of the attribute containing a command's alias. */
    public static final String ATTRIBUTE_ALIAS   = "alias";
    /** Name of the attribute containing a command's value. */
    public static final String ATTRIBUTE_VALUE   = "value";
    /** Name of the attribute containing a command's type. */
    public static final String ATTRIBUTE_TYPE    = "type";
    /** Describes <i>system</i> commands. */
    public static final String VALUE_SYSTEM      = "system";
    /** Describes <i>invisible</i> commands. */
    public static final String VALUE_INVISIBLE   = "invisible";
}

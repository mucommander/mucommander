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
 * Types of compiled shell commands.
 *
 * @author Arik Hadas
 */
public enum CommandType {
	/** Describes <i>normal</i> commands. */
    NORMAL_COMMAND,
    /** Describes <i>system</i> commands. */
    SYSTEM_COMMAND(CommandsXmlConstants.VALUE_SYSTEM),
    /** Describes <i>invisible</i> commands. */
    INVISIBLE_COMMAND(CommandsXmlConstants.VALUE_INVISIBLE);

    private String value;

    CommandType() {
    }

    CommandType(String value) {
    	this();
    	this.value = value;
    }

    /**
     * Returns the CommandType value according to specified String representation.
     * <p>
     * Note that this method is not strict in the arguments it receives:
     * <ul>
     *   <li>If <code>type</code> equals {CommandsXmlConstants#VALUE_SYSTEM}, {@link Command#SYSTEM_COMMAND} will be returned.</li>
     *   <li>If <code>type</code> equals {CommandsXmlConstants#VALUE_INVISIBLE}, {@link Command#INVISIBLE_COMMAND} will be returned.</li>
     *   <li>In any other case, {@link Command#NORMAL_COMMAND} will be returned.</li>
     * </ul>
     * </p>
     * @param  type String representation of type to analyze.
     * @return <code>type</code>'s integer equivalent.
     */
    public static CommandType parseCommandType(String value) {
    	if (value == null)
    		return NORMAL_COMMAND;

    	for (CommandType type : CommandType.values())
    		if (value.equals(type.value))
    			return type;

    	return NORMAL_COMMAND;
    }

    @Override
    public String toString() {
    	return value;
    }
}

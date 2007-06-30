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

package com.mucommander.command;

/**
 * Used to explore a list of commands in a format independant fashion.
 * <p>
 * Different classes, such as {@link CommandManager} or {@link CommandReader}, know how to
 * generate a list of commands - from instances loaded in memory or from a file, for example.
 * Implementing <code>CommandBuilder</code> allows classes to query these lists regardless of
 * their source.
 * </p>
 * <p>
 * Instances of <code>CommandBuilder</code> can rely on their methods to be called in the proper order,
 * and on both their {@link #startBuilding()} and {@link #endBuilding()} methods to be called. Classes that
 * interact with such instances must make sure this contract is respected.
 * </p>
 * <p>
 * A (fairly useless) implemention might look like:
 * <pre>
 * public class CommandPrinter implements CommandBuilder {
 *    public void startBuilding() {System.out.println("Begining command list building...");}
 *
 *    public void endBuilding() {System.out.println("Done.");}
 *
 *    public void addCommand(Command command) throws CommandException {
 *        System.out.println(" - creating command '" + command.getCommand() + "' with alias '" + command.getAlias() + "'");
 *    }
 * }
 * </pre>
 * Passing an instance of <code>CommandPrinter</code> to {@link CommandManager#buildCommands(CommandBuilder)}
 * will result in something like:
 * <pre>
 * Begining command list building...
 * - creating command 'open $f' with alias 'open'
 * - creating command 'open -a Safari $f' with alias 'openURL'
 * Done.
 * </pre>
 * </p>
 * @author Nicolas Rinaudo
 * @see    CommandReader
 * @see    CommandManager#buildCommands(CommandBuilder)
 */
public interface CommandBuilder {
    /**
     * Notifies the builder that command building is about to start.
     * @throws CommandException if an error occurs.
     */
    public void startBuilding();

    /**
     * Notifies the builder that command building is finished.
     * @throws CommandException if an error occurs.
     */
    public void endBuilding();

    /**
     * Notifies the builder that a new command has been found.
     * </p>
     * @param  command          command that has been found.
     * @throws CommandException if an error occurs.
     */
    public void addCommand(Command command) throws CommandException;
}

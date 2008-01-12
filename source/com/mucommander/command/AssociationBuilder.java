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
 * Receive notification of the logical structure of a custom association list.
 * @author Nicolas Rinaudo
 */
public interface AssociationBuilder {
    /**
     * Notifies the builder that association building is about to start.
     * @throws CommandException if an error occurs.
     */
    public void startBuilding() throws CommandException;

    /**
     * Notifies the builder that association building is finished.
     * @throws CommandException if an error occurs.
     */
    public void endBuilding() throws CommandException;

    /**
     * Notifies the builder that a new association declaration is starting.
     * @param  command          command to call when the association is matched.
     * @throws CommandException if an error occurs.
     */
    public void startAssociation(String command) throws CommandException;

    /**
     * Notifies the builder that the current association declaration is finished.
     * @throws CommandException if an error ocurs.
     */
    public void endAssociation() throws CommandException;

    /**
     * Adds a mask to the current association.
     * @param  mask             regular expression that a file name must match in order to match the association.
     * @param  isCaseSensitive  whether the regular expression is case sensitive.
     * @throws CommandException if an error occurs.
     */
    public void setMask(String mask, boolean isCaseSensitive) throws CommandException;

    /**
     * Adds a <i>symlink</i> filter on the current association.
     * @param  isSymlink        whether symbolic links must be refused or accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public void setIsSymlink(boolean isSymlink) throws CommandException;

    /**
     * Adds a <i>hidden</i> filter on the current association.
     * @param  isHidden         whether hidden files must be refused or accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public void setIsHidden(boolean isHidden) throws CommandException;

    /**
     * Adds a <i>readable</i> filter on the current association.
     * @param  isReadable       whether readable files must be refused or accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public void setIsReadable(boolean isReadable) throws CommandException;

    /**
     * Adds a <i>writable</i> filter on the current association.
     * @param  isWritable       whether writable files must be refused or accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public void setIsWritable(boolean isWritable) throws CommandException;

    /**
     * Adds a <i>executable</i> filter on the current association.
     * @param  isExecutable     whether executable files must be refused or accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public void setIsExecutable(boolean isExecutable) throws CommandException;
}

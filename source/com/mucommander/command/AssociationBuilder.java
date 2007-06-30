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

    public void startAssociation(String command) throws CommandException;
    public void endAssociation() throws CommandException;
    public void setMask(String mask, boolean isCaseSensitive) throws CommandException;
    public void setIsSymlink(boolean isSymlink) throws CommandException;
    public void setIsHidden(boolean isHidden) throws CommandException;
    public void setIsReadable(boolean isReadable) throws CommandException;
    public void setIsWritable(boolean isWritable) throws CommandException;
    public void setIsExecutable(boolean isExecutable) throws CommandException;
}

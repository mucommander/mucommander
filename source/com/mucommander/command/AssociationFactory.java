/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.command;

import com.mucommander.file.filter.*;

/**
 * @author Nicolas Rinaudo
 */
class AssociationFactory implements AssociationBuilder {
    private AndFileFilter filter;
    private String        command;

    public void startBuilding() {}
    public void endBuilding() {}

    public void startAssociation(String command) {
        filter       = new AndFileFilter();
        this.command = command;
    }

    public void endAssociation() throws CommandException {CommandManager.registerAssociation(command, filter);}

    public void setMask(String mask, boolean isCaseSensitive) {filter.addFileFilter(new RegexpFilenameFilter(mask, isCaseSensitive));}
    public void setIsDir(boolean isDir) {filter.addFileFilter(new TypeFileFilter(TypeFileFilter.DIRECTORY, isDir));}
    public void setIsSymlink(boolean isSymlink) {filter.addFileFilter(new TypeFileFilter(TypeFileFilter.SYMLINK, isSymlink));}
    public void setIsHidden(boolean isHidden) {filter.addFileFilter(new TypeFileFilter(TypeFileFilter.HIDDEN, isHidden));}
    public void setIsReadable(boolean isReadable) {filter.addFileFilter(new PermissionsFileFilter(PermissionsFileFilter.READ_PERMISSION, isReadable));}
    public void setIsWritable(boolean isWritable) {filter.addFileFilter(new PermissionsFileFilter(PermissionsFileFilter.WRITE_PERMISSION, isWritable));}
    public void setIsExecutable(boolean isExecutable) {filter.addFileFilter(new PermissionsFileFilter(PermissionsFileFilter.EXECUTE_PERMISSION, isExecutable));}
}

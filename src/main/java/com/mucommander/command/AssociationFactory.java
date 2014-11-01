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

import com.mucommander.commons.file.PermissionTypes;
import com.mucommander.commons.file.filter.AndFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;

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

    public void endAssociation() throws CommandException {
        // Skip empty file filters as they will break the whole
        // association mechanism.
        if(!filter.isEmpty())
            CommandManager.registerAssociation(command, filter);
    }

    public void setMask(String mask, boolean isCaseSensitive) {filter.addFileFilter(new RegexpFilenameFilter(mask, isCaseSensitive));}
    public void setIsDir(boolean isDir) {filter.addFileFilter(new AttributeFileFilter(FileAttribute.DIRECTORY, isDir));}
    public void setIsSymlink(boolean isSymlink) {filter.addFileFilter(new AttributeFileFilter(FileAttribute.SYMLINK, isSymlink));}
    public void setIsHidden(boolean isHidden) {filter.addFileFilter(new AttributeFileFilter(FileAttribute.HIDDEN, isHidden));}
    public void setIsReadable(boolean isReadable) {filter.addFileFilter(new PermissionsFileFilter(PermissionTypes.READ_PERMISSION, isReadable));}
    public void setIsWritable(boolean isWritable) {filter.addFileFilter(new PermissionsFileFilter(PermissionTypes.WRITE_PERMISSION, isWritable));}
    public void setIsExecutable(boolean isExecutable) {filter.addFileFilter(new PermissionsFileFilter(PermissionTypes.EXECUTE_PERMISSION, isExecutable));}
}

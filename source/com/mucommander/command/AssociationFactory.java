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

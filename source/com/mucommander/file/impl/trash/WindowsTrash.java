/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.file.impl.trash;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractTrash;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.impl.local.SpecialWindowsLocation;
import com.mucommander.file.util.Shell32API;


/**
 * WindowsTrash is an <code>AbstractTrash</code> implementation for the <i>Microsoft Windows' Recycle Bin</i>.
 * Native methods in the Shell32 DLL are used to access the Recycle Bin.
 *
 * @author Maxence Bernard
 */
public class WindowsTrash extends AbstractTrash {

    //////////////////////////////////
    // AbstractTrash implementation //
    //////////////////////////////////

    /**
     * Implementation notes: returns <code>true</code> only for local files that are not archive entries.
     */
    public boolean canMoveToTrash(AbstractFile file) {
        return file.getTopAncestor() instanceof LocalFile;
    }

    public boolean moveToTrash(AbstractFile file) {
        Shell32API.SHFILEOPSTRUCT fileop = new Shell32API.SHFILEOPSTRUCT();

        fileop.wFunc = Shell32API.FO_DELETE;
        fileop.fFlags = Shell32API.FOF_ALLOWUNDO| Shell32API.FOF_NOCONFIRMATION| Shell32API.FOF_SILENT;
        // Directories (and regular files) must not end with a trailing slash or the operation will fail.
        // The encodePaths method takes care of encoding the path in a special way.
        fileop.pFrom = fileop.encodePaths(new String[]{file.getAbsolutePath(false)}); 

        // Note: if needed, SHFileOperation is able to move a list of files to the trash instead of just one.                                

        return Shell32API.INSTANCE.SHFileOperation(fileop) == 0;
    }

    /**
     * Implementation notes: always returns <code>true</code>: {@link #empty()} is implemented.
     */
    public boolean canEmpty() {
        return true;
    }

    public boolean empty() {
        return Shell32API.INSTANCE.SHEmptyRecycleBin(null, null, Shell32API.SHERB_NOCONFIRMATION) == 0;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean isTrashFile(AbstractFile file) {
        // Quote from http://en.wikipedia.org/wiki/Recycle_Bin_(Windows):
        // "The actual location of the Recycle Bin varies depending on the operating system and filesystem. On the older
        // FAT filesystems (typically Windows 98 and prior), it is located in Drive:\RECYCLED. In the NTFS filesystem
        // (Windows 2000, XP, NT) it can be found in Drive:\RECYCLER, with the exception of Windows Vista which stores
        // it in the Drive:\$Recycle.Bin folder."

        // => for the test to be accurate, we'd have to go thru the trouble of testing the kind of filesystem
        // (FAT or NTFS) and the Windows version. It's a lot of work for little added value.

        return false;
    }

    /**
     * Implementation notes: returns the number of items for all Recycle Bins on all drives. This information is not
     * available on certain versions of Windows such as <i>Windows 2000</i>.
     */
    public int getItemCount() {
        Shell32API.SHQUERYRBINFO queryRbInfo = new Shell32API.SHQUERYRBINFO();

        // pszRootPath is null to retrieve the information for all Recycle Bins on all drives. Microsoft's documentation
        // states that this fails on certain versions of Windows such as Windows 2000. If it does, we simply return -1.

        int ret = Shell32API.INSTANCE.SHQueryRecycleBin(
            null,
            queryRbInfo
        );

        return ret==0?(int)queryRbInfo.i64NumItems:-1;
    }

    /**
     * Implementation notes: always returns <code>true</code>: {@link #open()} is implemented.
     */
    public boolean canOpen() {
        return true;
    }

    public void open() {
        PlatformManager.openInFileManager(SpecialWindowsLocation.RECYCLE_BIN);
    }

    /**
     * Implementation notes: this method is implemented as a no-op.
     */
    public void waitForPendingOperations() {
    }
}

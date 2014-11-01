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

package com.mucommander.desktop.windows;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.DesktopManager;

/**
 * @author Nicolas Rinaudo
 */
class WindowsDesktopAdapter extends DefaultDesktopAdapter {
    protected static final String EXPLORER_NAME = "Explorer";

    public String toString() {return "Windows Desktop";}

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        // The Windows trash requires access to the Shell32 DLL, register the provider only if the Shell32 DLL
        // is available on the current runtime environment.
        if(WindowsTrashProvider.isAvailable())
            DesktopManager.setTrashProvider(new WindowsTrashProvider());
    }

    @Override
    public boolean isAvailable() {
        return OsFamily.getCurrent().equals(OsFamily.WINDOWS);
    }

    /**
     * Returns <code>true</code> for regular files (not directories) with an <code>exe</code> extension
     * (case-insensitive comparison).
     *
     * @param file the file to test
     * @return <code>true</code> for regular files (not directories) with an <code>exe</code> extension
     * (case-insensitive comparison).
     */
    @Override
    public boolean isApplication(AbstractFile file) {
        String extension = file.getExtension();

        // the isDirectory() test comes last as it is I/O bound
        return extension!=null && extension.equalsIgnoreCase("exe") && !file.isDirectory();
    }
}

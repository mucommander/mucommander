/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.file.util.Shell32;
import com.mucommander.runtime.OsFamily;

/**
 * @author Nicolas Rinaudo
 */
class WindowsDesktopAdapter extends DefaultDesktopAdapter {
    protected static final String EXPLORER_NAME = "Explorer";

    public String toString() {return "Windows Desktop";}

    public void init(boolean install) throws DesktopInitialisationException {
        // The Windows trash requires access to the Shell32 DLL, register the provider only if the Shell32 DLL
        // is available on the current runtime environment.
        if(Shell32.isAvailable())
            DesktopManager.setTrashProvider(new WindowsTrashProvider());
    }

    public boolean isAvailable() {return OsFamily.getCurrent().equals(OsFamily.WINDOWS);}
}

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

package com.mucommander.desktop.openvms;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.desktop.DefaultDesktopAdapter;

/**
 * A desktop adapter for OpenVMS.
 *
 * <p>This adapter currently doesn't bring any improvement over {@link com.mucommander.desktop.DefaultDesktopAdapter} 
 * -- its purpose is simply to bypass other desktop adapters tests, some of which are costly.</p>
 *
 * @author Maxence Bernard
 */
public class OpenVMSDesktopAdapter extends DefaultDesktopAdapter {

    @Override
    public boolean isAvailable() {
        return OsFamily.OPENVMS.isCurrent();
    }
}

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

package com.mucommander.desktop.kde;

import com.mucommander.runtime.JavaVersion;

/**
 * 'Configured' desktop adapter for KDE 3. The availability of this desktop depends on the presence of the
 * <code>KDE_FULL_SESSION</code> environment variable.
 *
 * @author Nicolas Rinaudo
 */
public class ConfiguredKde3DesktopAdapter extends Kde3DesktopAdapter {

    private static final String KDE_FULL_SESSION_VAR = "KDE_FULL_SESSION";

    protected String getConfiguredEnvVariable(String name) {
        return JavaVersion.JAVA_1_4.isCurrentOrLower()?System.getProperty(name):System.getenv(name);
    }

    public String toString() {
        return "KDE 3 Desktop";
    }

    public boolean isAvailable() {
        String var = getConfiguredEnvVariable(KDE_FULL_SESSION_VAR);
        return var != null && !var.trim().equals("");
    }


}
